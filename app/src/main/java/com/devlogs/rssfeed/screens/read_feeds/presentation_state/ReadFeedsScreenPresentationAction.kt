package com.devlogs.rssfeed.screens.read_feeds.presentation_state

import com.devlogs.rssfeed.screens.common.presentation_state.PresentationAction
import com.devlogs.rssfeed.screens.read_feeds.presentable_model.FeedPresentableModel
import com.devlogs.rssfeed.screens.read_feeds.presentable_model.RssChannelPresentableModel
import java.util.*

sealed class ReadFeedsScreenPresentationAction : PresentationAction {
    data class InitialLoadAction(val channelId: String) : ReadFeedsScreenPresentationAction()
    data class InitialLoadFailedAction (val message: String) : ReadFeedsScreenPresentationAction()
    data class InitialLoadSuccessAction (val feeds: TreeSet<FeedPresentableModel>, val channel: RssChannelPresentableModel, val userAvatar: String) : ReadFeedsScreenPresentationAction()
    data class UserSelectChannelAction (val channelId: String) : ReadFeedsScreenPresentationAction()
    class EmptyChannelAction  : ReadFeedsScreenPresentationAction()
    // we consider the both unFollow and follow is the FollowProcess
    class FollowProcessSuccessAction () : ReadFeedsScreenPresentationAction()
    data class FollowProcessFailedAction (val errorMessage: String) : ReadFeedsScreenPresentationAction()
    class UnFollowProcessSuccessAction () : ReadFeedsScreenPresentationAction()
    data class UnFollowProcessFailedAction (val errorMessage: String) : ReadFeedsScreenPresentationAction()
    class ReloadActionFailed (val message: String): ReadFeedsScreenPresentationAction()
    class ReloadActionSuccess (): ReadFeedsScreenPresentationAction()
    class LoadMoreSuccessAction (val feeds: TreeSet<FeedPresentableModel>) : ReadFeedsScreenPresentationAction()
    class LoadMoreFailedAction (val errorMessage: String) : ReadFeedsScreenPresentationAction()
    class NewFeedsAction (val feeds: TreeSet<FeedPresentableModel>) : ReadFeedsScreenPresentationAction ()
}