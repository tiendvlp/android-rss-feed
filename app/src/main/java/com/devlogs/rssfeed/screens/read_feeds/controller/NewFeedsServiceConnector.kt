package com.devlogs.rssfeed.screens.read_feeds.controller

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.devlogs.rssfeed.android_services.RssChangeListenerService
import com.devlogs.rssfeed.common.helper.isSameDate
import com.devlogs.rssfeed.common.shared_context.AppConfig.DaggerNamed.FRAGMENT_SCOPE
import com.devlogs.rssfeed.domain.entities.FeedEntity
import com.devlogs.rssfeed.screens.common.presentation_state.PresentationStateManager
import com.devlogs.rssfeed.screens.read_feeds.presentable_model.FeedPresentableModel
import com.devlogs.rssfeed.screens.read_feeds.presentation_state.ReadFeedsScreenPresentationAction.NewFeedsAction
import com.devlogs.rssfeed.screens.read_feeds.presentation_state.ReadFeedsScreenPresentationState.DisplayState
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class NewFeedsServiceConnector @Inject constructor (@Named(FRAGMENT_SCOPE) private val stateManager: PresentationStateManager) : ServiceConnection, RssChangeListenerService.Listener {

    private var service : RssChangeListenerService? = null

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        if (stateManager.currentState !is DisplayState) {
            throw RuntimeException("Invalid state, serviceConnected require DisplayState but ${stateManager.currentState.javaClass.simpleName} is found")
        }
        val displayState = stateManager.currentState as DisplayState
        val binder = service as RssChangeListenerService.LocalBinder
        this.service = binder.service
        Log.d("OnNewFeedConnector", "Reregister: ${displayState.channelPresentableModel.title}")
        this.service!!.register(displayState.channelPresentableModel.id, this)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        if (stateManager.currentState !is DisplayState) {
            throw RuntimeException("Invalid state, serviceDisConnect require DisplayState but ${stateManager.currentState.javaClass.simpleName} is found")
        }
        val displayState = stateManager.currentState as DisplayState
        service!!.unRegister(displayState.channelPresentableModel.id, this)
        service = null
    }

    override fun onNewFeed(feeds: TreeSet<FeedEntity>) {
        Log.d("OnNewFeedConnector", "Feed comming: ${feeds.size}")
        if (stateManager.currentState is DisplayState) {
            val result = TreeSet<FeedPresentableModel>()
            feeds.forEach {
                result.add(feedEntityToPresentableModel(it))
            }
            if (feeds.isNotEmpty()) {
                stateManager.consumeAction(NewFeedsAction(result))
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