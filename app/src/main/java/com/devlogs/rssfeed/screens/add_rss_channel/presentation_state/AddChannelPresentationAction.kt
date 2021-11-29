package com.devlogs.rssfeed.screens.add_rss_channel.presentation_state

import com.devlogs.rssfeed.screens.add_rss_channel.presentable_model.RssChannelResultPresentableModel
import com.devlogs.rssfeed.screens.common.presentation_state.PresentationAction

sealed class AddChannelPresentationAction : PresentationAction {
    data class SearchSuccessAction (val channel: RssChannelResultPresentableModel?): AddChannelPresentationAction()
    data class SearchFailedAction (val errorMessage: String): AddChannelPresentationAction()
    data class SearchAction (val url: String): AddChannelPresentationAction()
    data class AddFailedAction (val errorMessage: String) : AddChannelPresentationAction()
    class AddSuccessAction () : AddChannelPresentationAction()
}