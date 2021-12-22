package com.devlogs.rssfeed.screens.read_feeds.presentation_state

import com.devlogs.chatty.screen.common.presentationstate.CommonPresentationAction
import com.devlogs.chatty.screen.common.presentationstate.CommonPresentationAction.InitAction
import com.devlogs.chatty.screen.common.presentationstate.CommonPresentationAction.RestoreAction
import com.devlogs.rssfeed.screens.common.presentation_state.CauseAndEffect
import com.devlogs.rssfeed.screens.common.presentation_state.PresentationAction
import com.devlogs.rssfeed.screens.common.presentation_state.PresentationState
import com.devlogs.rssfeed.screens.read_feeds.presentable_model.FeedPresentableModel
import com.devlogs.rssfeed.screens.read_feeds.presentable_model.ReadFeedPresentableModel
import com.devlogs.rssfeed.screens.read_feeds.presentable_model.RssChannelPresentableModel
import com.devlogs.rssfeed.screens.read_feeds.presentation_state.ReadFeedsScreenPresentationAction.*
import java.util.*

sealed class ReadFeedsScreenPresentationState : PresentationState {

    data class DisplayState (val model: ReadFeedPresentableModel) : ReadFeedsScreenPresentationState () {
        override val allowSave: Boolean
            get() = true

        override fun consumeAction(
            previousState: PresentationState,
            action: PresentationAction
        ): CauseAndEffect {
            when (action) {
                is RestoreAction -> return CauseAndEffect(action, this)
                is UserSelectChannelAction -> return CauseAndEffect(action, InitialLoadingState(action.channelId))
                is FollowProcessFailedAction -> return CauseAndEffect(action, copy())
                is UnFollowProcessFailedAction -> return CauseAndEffect(action, copy())
                is FollowProcessSuccessAction ->
                {
                    val currentChannelButFollow = model.channelPresentableModel.copy(isFollowed = true)
                    return CauseAndEffect(action, copy(model.copy(channelPresentableModel = currentChannelButFollow)))
                }
                is UnFollowProcessSuccessAction ->
                {
                    val currentChannelButUnFollow = model.channelPresentableModel.copy(isFollowed = false)
                    return CauseAndEffect(action, copy(model.copy(channelPresentableModel = currentChannelButUnFollow)))
                }
                is NewFeedsAction -> return CauseAndEffect(action, copy(model.copy(feeds = appendFeeds(action.feeds))))
                is ReloadActionFailed -> return CauseAndEffect(action, copy())
                is LoadMoreSuccessAction -> return CauseAndEffect(action, copy(model.copy(feeds = appendFeeds(action.feeds))))
                is LoadMoreFailedAction -> return CauseAndEffect(action, copy())
                is ReloadActionSuccess -> return CauseAndEffect(action, copy())
            }
            return super.consumeAction(previousState, action)
        }

        private fun appendFeeds(feeds: TreeSet<FeedPresentableModel>): TreeSet<FeedPresentableModel> {
            val newChannels = TreeSet<FeedPresentableModel>()
            newChannels.addAll(this.model.feeds)
            newChannels.addAll(feeds)
            return newChannels
        }
    }

    class EmptyState : ReadFeedsScreenPresentationState() {
        override val allowSave: Boolean
            get() = true

        override fun consumeAction(
            previousState: PresentationState,
            action: PresentationAction
        ): CauseAndEffect {
            when (action) {
                is InitAction -> return CauseAndEffect(action, EmptyState())
                is UserSelectChannelAction -> return CauseAndEffect(action, InitialLoadingState(action.channelId))
            }
            return super.consumeAction(previousState, action)
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
                is UserSelectChannelAction -> return CauseAndEffect(action, InitialLoadingState(action.channelId))
                is InitAction -> return CauseAndEffect(action, copy())
                is InitialLoadFailedAction -> return CauseAndEffect(action, InitialLoadFailedState(action.message))
                is InitialLoadSuccessAction -> return CauseAndEffect(action, DisplayState(ReadFeedPresentableModel(action.feeds, action.channel, action.userAvatar)))
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
                is UserSelectChannelAction -> return CauseAndEffect(action, InitialLoadingState(action.channelId))
                is InitialLoadAction -> CauseAndEffect(action, InitialLoadingState(action.channelId))
            }
            return super.consumeAction(previousState, action)
        }
    }
}