package com.devlogs.rssfeed.screens.read_feeds.controller

import android.text.format.DateUtils
import com.devlogs.rssfeed.authentication.GetLoggedInUserUseCaseSync
import com.devlogs.rssfeed.common.helper.isSameDate
import com.devlogs.rssfeed.domain.entities.FeedEntity
import com.devlogs.rssfeed.feeds.GetFeedsByRssChannelUseCaseSync
import com.devlogs.rssfeed.rss_channels.GetRssChannelByIdUseCaseSync
import com.devlogs.rssfeed.screens.common.presentation_state.PresentationStateManager
import com.devlogs.rssfeed.screens.read_feeds.presentable_model.FeedPresentableModel
import com.devlogs.rssfeed.screens.read_feeds.presentable_model.RssChannelPresentableModel
import com.devlogs.rssfeed.screens.read_feeds.presentation_state.ReadFeedsScreenPresentationAction
import com.devlogs.rssfeed.screens.read_feeds.presentation_state.ReadFeedsScreenPresentationAction.*
import com.devlogs.rssfeed.screens.read_feeds.presentation_state.ReadFeedsScreenPresentationState
import com.devlogs.rssfeed.screens.read_feeds.presentation_state.ReadFeedsScreenPresentationState.DisplayState
import com.devlogs.rssfeed.screens.read_feeds.presentation_state.ReadFeedsScreenPresentationState.InitialLoadingState
import kotlinx.coroutines.*
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class FeedsController @Inject constructor(private val stateManager: PresentationStateManager,
                                          private val getFeedsByRssChannelUseCaseSync: GetFeedsByRssChannelUseCaseSync,
                                          private val getRssChannelByIdUseCaseSync: GetRssChannelByIdUseCaseSync,
                                          private val getLoggedInUserUseCaseSync: GetLoggedInUserUseCaseSync  ) {

    private val coroutine = CoroutineScope(Dispatchers.Main.immediate)

    fun initialLoad() {
        if (stateManager.currentState !is InitialLoadingState) {
            throw RuntimeException("Invalid state, initialLoad only run with InitialLoadingState but ${stateManager.currentState.javaClass.simpleName} is found")
        }
        val loadingState = stateManager.currentState as InitialLoadingState

        coroutine.launch {
            val deferredList = listOf(
                coroutine.async {
                    getFeedsByRssChannelUseCaseSync.executes(loadingState.channelId, System.currentTimeMillis(), 20)
                },
                coroutine.async {
                    getRssChannelByIdUseCaseSync.executes(loadingState.channelId)
                },
                coroutine.async {
                    getLoggedInUserUseCaseSync.executes()
                })

            val deferredResult = deferredList.awaitAll()

            val getFeedsResult = deferredResult[0] as GetFeedsByRssChannelUseCaseSync.Result
            val getChannelResult = deferredResult[1] as GetRssChannelByIdUseCaseSync.Result
            val getUserResult = deferredResult[2] as GetLoggedInUserUseCaseSync.Result

            val feeds = TreeSet<FeedPresentableModel>()
            var avatarUrl : String? = null
            var channel : RssChannelPresentableModel? = null

            if (getFeedsResult is GetFeedsByRssChannelUseCaseSync.Result.GeneralError) {
                stateManager.consumeAction(InitialLoadFailedAction(getFeedsResult.errorMessage ?: "Internal error"))
            }

            if (getFeedsResult is GetFeedsByRssChannelUseCaseSync.Result.Success) {
                feeds.addAll(getFeedsResult.rssChannel.map { feedEntityToPresentableModel(it) })
            } else {
                throw RuntimeException("UnExpected result from getFeedsUseCase ${getFeedsResult.javaClass.simpleName}")
            }

            // now we get Feeds success, next is check the user and channel
            if (getChannelResult is GetRssChannelByIdUseCaseSync.Result.GeneralError) {
                stateManager.consumeAction(InitialLoadFailedAction("Internal error while getting rss channel info"))
            }

            if (getChannelResult is GetRssChannelByIdUseCaseSync.Result.NotFound) {
                stateManager.consumeAction(InitialLoadFailedAction("Rss channel not found"))
            }

            if (getChannelResult is GetRssChannelByIdUseCaseSync.Result.Success) {
                channel = RssChannelPresentableModel(
                    getChannelResult.channel.id,
                    getChannelResult.channel.url,
                    getChannelResult.channel.rssUrl,
                    getChannelResult.channel.title,
                )
            } else {
                throw RuntimeException("UnExpected result from GetRssChannelByIdUseCaseSync ${getChannelResult.javaClass.simpleName}")
            }

            // let's check the user
            if (getUserResult is GetLoggedInUserUseCaseSync.Result.InValidLogin) {
                // TODO ("Require user to login again")
            }

            if (getUserResult is GetLoggedInUserUseCaseSync.Result.Success) {
                avatarUrl = getUserResult.user.avatar
            }

            // success let's init the LoadingActionSuccess
            stateManager.consumeAction(InitialLoadSuccessAction(feeds, channel, avatarUrl!!))

        }
    }

    fun loadMore() {
        if (stateManager.currentState !is DisplayState) {
            throw RuntimeException("Invalid state, loadMore only run with DisplayState but ${stateManager.currentState.javaClass.simpleName} is found")
        }
        val displayState = stateManager.currentState as DisplayState
        val channelId = displayState.channelPresentableModel.id
        val oldest = displayState.feeds.last().pubDate
        coroutine.launch {
            val getFeedsResult = getFeedsByRssChannelUseCaseSync.executes(channelId
            , oldest, 20)

            if (getFeedsResult is GetFeedsByRssChannelUseCaseSync.Result.GeneralError) {
                stateManager.consumeAction(LoadMoreFailedAction(getFeedsResult.errorMessage ?: "Internal error"))
            }

            val feeds = TreeSet<FeedPresentableModel>()
            if (getFeedsResult is GetFeedsByRssChannelUseCaseSync.Result.Success) {
                feeds.addAll(getFeedsResult.rssChannel.map { feedEntityToPresentableModel(it) })
                stateManager.consumeAction(LoadMoreSuccessAction(feeds))
            } else {
                throw RuntimeException("UnExpected result from getFeedsUseCase ${getFeedsResult.javaClass.simpleName}")
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
            pubDateInString,
            feedEntity.url,
            feedEntity.author,
            feedEntity.imageUrl
        )
    }
}