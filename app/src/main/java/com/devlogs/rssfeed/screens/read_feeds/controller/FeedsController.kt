package com.devlogs.rssfeed.screens.read_feeds.controller

import com.devlogs.rssfeed.authentication.GetLoggedInUserUseCaseSync
import com.devlogs.rssfeed.feeds.GetFeedsByRssChannelUseCaseSync
import com.devlogs.rssfeed.rss_channels.GetRssChannelByIdUseCaseSync
import com.devlogs.rssfeed.screens.common.presentation_state.PresentationStateManager
import com.devlogs.rssfeed.screens.read_feeds.presentable_model.FeedPresentableModel
import com.devlogs.rssfeed.screens.read_feeds.presentable_model.RssChannelPresentableModel
import com.devlogs.rssfeed.screens.read_feeds.presentation_state.ReadFeedsScreenPresentationAction.InitialLoadFailedAction
import com.devlogs.rssfeed.screens.read_feeds.presentation_state.ReadFeedsScreenPresentationAction.InitialLoadSuccessAction
import com.devlogs.rssfeed.screens.read_feeds.presentation_state.ReadFeedsScreenPresentationState.InitialLoadingState
import kotlinx.coroutines.*
import java.lang.RuntimeException
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
                feeds.addAll(getFeedsResult.rssChannel.map { FeedPresentableModel(
                    it.id,
                    it.rssChannelId,
                    it.channelTitle,
                    it.title,
                    it.pubDate, it.pubDate.toString(),
                    it.url,
                    it.author,
                    it.imageUrl
                )})
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
}