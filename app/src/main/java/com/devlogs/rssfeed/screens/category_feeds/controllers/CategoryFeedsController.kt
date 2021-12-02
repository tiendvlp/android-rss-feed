package com.devlogs.rssfeed.screens.category_feeds.controllers

import android.util.Log
import com.devlogs.rssfeed.common.helper.isSameDate
import com.devlogs.rssfeed.domain.entities.FeedEntity
import com.devlogs.rssfeed.feeds.GetFeedsByCategoryUseCaseSync
import com.devlogs.rssfeed.screens.category_feeds.mvc_view.CategoryFeedsMvcView
import com.devlogs.rssfeed.screens.category_feeds.presentable_model.FeedPresentableModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class CategoryFeedsController @Inject constructor(private val getFeedsByCategoryUseCaseSync: GetFeedsByCategoryUseCaseSync) {
    
    private lateinit var mvcView: CategoryFeedsMvcView
    private val coroutine = CoroutineScope(Dispatchers.Main.immediate)
    
    fun setMvcView (mvcView: CategoryFeedsMvcView) {
        this.mvcView = mvcView
    }
    
    fun getCategories (categoryTitle: String) {
        mvcView.setTitle(categoryTitle)
        mvcView.loading()
        coroutine.launch {
            val result = getFeedsByCategoryUseCaseSync.executes(categoryTitle, Long.MAX_VALUE)
            Log.d("CategoryFeedsController", result.javaClass.canonicalName)
            if (result is GetFeedsByCategoryUseCaseSync.Result.UnAuthorized) {
                mvcView.toast("Internal server error")
            }

            if (result is GetFeedsByCategoryUseCaseSync.Result.GeneralError) {
                mvcView.toast("Internal server error: ${result.message}")
            }

            if (result is GetFeedsByCategoryUseCaseSync.Result.Success) {
                val feeds = TreeSet<FeedPresentableModel>()
                result.feeds.forEach {
                    feeds.add(feedEntityToPresentableModel(it))
                }
                mvcView.setFeeds(feeds)
            }
        }
    }

    private fun feedEntityToPresentableModel (feedEntity: FeedEntity): FeedPresentableModel {

        val today = Date()

        val yesterday = Date()
        yesterday.date = yesterday.date - 1

        val pubDate = Date(feedEntity.pubDate)

        val isToday = pubDate.isSameDate(today)
        val isYesterday = pubDate.isSameDate(yesterday)

        val formater = SimpleDateFormat("dd/MM/yy")
        val hourFormater = SimpleDateFormat("HH:mm")
        var pubDateInString : String

        when {
            isToday -> {
                pubDateInString = "Today"
            }
            isYesterday -> {
                pubDateInString = "Yesterday"
            }
            else -> {
                pubDateInString = formater.format(pubDate)
            }
        }

        pubDateInString += " at ${hourFormater.format(pubDate)}"

        return FeedPresentableModel(
            feedEntity.id,
            feedEntity.rssChannelId,
            feedEntity.channelTitle,
            feedEntity.title,
            feedEntity.pubDate,
            feedEntity.content,
            pubDateInString,
            feedEntity.url,
            feedEntity.author,
            feedEntity.imageUrl
        )
    }
    
}