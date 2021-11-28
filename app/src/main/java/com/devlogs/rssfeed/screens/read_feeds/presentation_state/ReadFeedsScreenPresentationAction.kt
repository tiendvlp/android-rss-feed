package com.devlogs.rssfeed.screens.read_feeds.presentation_state

import com.devlogs.rssfeed.screens.common.presentation_state.PresentationAction
import com.devlogs.rssfeed.screens.read_feeds.presentable_model.FeedPresentableModel
import com.devlogs.rssfeed.screens.read_feeds.presentable_model.RssChannelPresentableModel
import java.util.*

sealed class ReadFeedsScreenPresentationAction : PresentationAction {
    data class InitialLoadAction(val channelId: String) : ReadFeedsScreenPresentationAction()
    data class InitialLoadFailedAction (val message: String) : ReadFeedsScreenPresentationAction()
    data class InitialLoadSuccessAction (val feeds: TreeSet<FeedPresentableModel>, val channel: RssChannelPresentableModel, val userAvatar: String) : ReadFeedsScreenPresentationAction()
    class LoadMoreAction () : ReadFeedsScreenPresentationAction()
    class ReloadAction (): ReadFeedsScreenPresentationAction()
    class LoadMoreSuccessAction (val feeds: TreeSet<FeedPresentableModel>) : ReadFeedsScreenPresentationAction()
    class LoadMoreFailedAction (val errorMessage: String) : ReadFeedsScreenPresentationAction()
    class NewFeedsAction (val feeds: TreeSet<FeedPresentableModel>) : ReadFeedsScreenPresentationAction ()
}