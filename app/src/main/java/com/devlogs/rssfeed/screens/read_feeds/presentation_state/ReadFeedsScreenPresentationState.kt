package com.devlogs.rssfeed.screens.read_feeds.presentation_state

import com.devlogs.chatty.screen.common.presentationstate.CommonPresentationAction
import com.devlogs.chatty.screen.common.presentationstate.CommonPresentationAction.InitAction
import com.devlogs.rssfeed.screens.common.presentation_state.CauseAndEffect
import com.devlogs.rssfeed.screens.common.presentation_state.PresentationAction
import com.devlogs.rssfeed.screens.common.presentation_state.PresentationState
import com.devlogs.rssfeed.screens.read_feeds.presentable_model.FeedPresentableModel
import com.devlogs.rssfeed.screens.read_feeds.presentable_model.RssChannelPresentableModel
import com.devlogs.rssfeed.screens.read_feeds.presentation_state.ReadFeedsScreenPresentationAction.*
import java.util.*

sealed class ReadFeedsScreenPresentationState : PresentationState {

    data class DisplayState (val feeds : TreeSet<FeedPresentableModel>, val channelPresentableModel: RssChannelPresentableModel, val avatarUrl: String) : ReadFeedsScreenPresentationState () {
        override val allowSave: Boolean
            get() = true

        override fun consumeAction(
            previousState: PresentationState,
            action: PresentationAction
        ): CauseAndEffect {
            when (action) {
                is NewFeedsAction -> return CauseAndEffect(action, copy(feeds = appendFeeds(action.feeds)))
                is ReloadActionFailed -> return CauseAndEffect(action, copy())
                is LoadMoreSuccessAction -> return CauseAndEffect(action, copy(feeds = appendFeeds(action.feeds)))
                is LoadMoreFailedAction -> return CauseAndEffect(action, copy())
                is ReloadActionSuccess -> return CauseAndEffect(action, copy())
            }
            return super.consumeAction(previousState, action)
        }

        private fun appendFeeds(feeds: TreeSet<FeedPresentableModel>): TreeSet<FeedPresentableModel> {
            val newChannels = TreeSet<FeedPresentableModel>()
            newChannels.addAll(this.feeds)
            newChannels.addAll(feeds)
            return newChannels
        }
    }

    data class InitialLoadingState (val channelId: String) : ReadFeedsScreenPresentationState () {
        override val allowSave: Boolean
            get() = false

        override fun consumeAction(
            previousState: PresentationState,
            action: PresentationAction
        ): CauseAndEffect {

            when (action) {
                is InitAction -> return CauseAndEffect(action, copy())
                is InitialLoadFailedAction -> return CauseAndEffect(action, InitialLoadFailedState(action.message))
                is InitialLoadSuccessAction -> return CauseAndEffect(action, DisplayState(action.feeds, action.channel, action.userAvatar))
            }

            return super.consumeAction(previousState, action)
        }
    }

    data class InitialLoadFailedState (val message: String) : ReadFeedsScreenPresentationState() {
        override val allowSave: Boolean
            get() = true

        override fun consumeAction(
            previousState: PresentationState,
            action: PresentationAction
        ): CauseAndEffect {
            when(action) {
                is InitialLoadAction -> CauseAndEffect(action, InitialLoadingState(action.channelId))
            }
            return super.consumeAction(previousState, action)
        }
    }
}