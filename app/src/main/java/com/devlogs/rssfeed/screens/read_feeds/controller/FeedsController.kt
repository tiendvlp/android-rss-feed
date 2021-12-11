package com.devlogs.rssfeed.screens.read_feeds.controller

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.devlogs.rssfeed.authentication.GetLoggedInUserUseCaseSync
import com.devlogs.rssfeed.common.helper.isSameDate
import com.devlogs.rssfeed.common.shared_context.AppConfig.DaggerNamed.FRAGMENT_SCOPE
import com.devlogs.rssfeed.domain.entities.FeedEntity
import com.devlogs.rssfeed.feeds.GetFeedsByRssChannelUseCaseSync
import com.devlogs.rssfeed.rss_channels.GetRssChannelByIdUseCaseSync
import com.devlogs.rssfeed.rss_channels.ReloadRssChannelUseCaseSync
import com.devlogs.rssfeed.screens.common.presentation_state.PresentationStateManager
import com.devlogs.rssfeed.screens.read_feeds.presentable_model.FeedPresentableModel
import com.devlogs.rssfeed.screens.read_feeds.presentable_model.RssChannelPresentableModel
import com.devlogs.rssfeed.screens.read_feeds.presentation_state.ReadFeedsScreenPresentationAction.*
import com.devlogs.rssfeed.screens.read_feeds.presentation_state.ReadFeedsScreenPresentationState.DisplayState
import com.devlogs.rssfeed.screens.read_feeds.presentation_state.ReadFeedsScreenPresentationState.InitialLoadingState
import kotlinx.coroutines.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class FeedsController @Inject constructor(@Named(FRAGMENT_SCOPE) private val stateManager: PresentationStateManager,
                                          private val getFeedsByRssChannelUseCaseSync: GetFeedsByRssChannelUseCaseSync,
                                          private val getRssChannelByIdUseCaseSync: GetRssChannelByIdUseCaseSync,
                                          private val reloadRssChannelUseCaseSync: ReloadRssChannelUseCaseSync,
                                          private val getLoggedInUserUseCaseSync: GetLoggedInUserUseCaseSync  ) {

    private val coroutine = CoroutineScope(Dispatchers.Main.immediate)

    @RequiresApi(Build.VERSION_CODES.O)
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
                    getChannelResult.isFollowed
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
            if (stateManager.currentState is InitialLoadingState) {
                stateManager.consumeAction(InitialLoadSuccessAction(feeds, channel, avatarUrl!!))
            } else {
                Log.e("FeedsController", "Invalid state happen ")
            }
        }
    }

    fun cancel () {
//        coroutine.coroutineContext.cancelChildren()
    }

    @RequiresApi(Build.VERSION_CODES.O)
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun feedEntityToPresentableModel (feedEntity: FeedEntity): FeedPresentableModel {

        val today = LocalDateTime.now()
        val yesterday = LocalDateTime.now().minusDays(1)
        ZoneId.SHORT_IDS.forEach {
            Log.d("ZoneIds", "${it.key} : ${it.value}")
        }
        val localPubDate = Instant.ofEpochMilli(feedEntity.pubDate).atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDateTime()

        val isToday = localPubDate.isSameDate(today)
        val isYesterday = localPubDate.isSameDate(yesterday)

        val localDateFormater = DateTimeFormatter.ofPattern("dd/mm/yy")
        val localHourFormater = DateTimeFormatter.ofPattern("HH:mm")

        var pubDateInString : String

        when {
            isToday -> {
                pubDateInString = "Today"
            }
            isYesterday -> {
                pubDateInString = "Yesterday"
            }
            else -> {
                pubDateInString = localPubDate.format(localDateFormater)
            }
        }

        pubDateInString += " at ${localPubDate.format(localHourFormater)}"

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

    fun reload() {
        Log.d("FeedsController", "Reload start")
        if (stateManager.currentState !is DisplayState) {
            throw RuntimeException("Invalid state, reload only run with DisplayState but ${stateManager.currentState.javaClass.canonicalName} is found")
        }
        val displayState = stateManager.currentState as DisplayState
        val channelId = displayState.channelPresentableModel.id

        coroutine.launch {
            val result = reloadRssChannelUseCaseSync.executes(channelId)
            if (result is ReloadRssChannelUseCaseSync.Result.Success) {
                Log.d("FeedsController", "Reload success")
                stateManager.consumeAction(ReloadActionSuccess())
            } else {
                // don't care about the failed type
                Log.e("FeedsController", "Reload failed: ${result.javaClass.simpleName}")
            }
        }
    }
}