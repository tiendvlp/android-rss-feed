package com.devlogs.rssfeed.screens.add_rss_channel.controller

import com.devlogs.rssfeed.common.shared_context.AppConfig.DaggerNamed.FRAGMENT_SCOPE
import com.devlogs.rssfeed.domain.entities.RssChannelEntity
import com.devlogs.rssfeed.rss_channels.AddNewRssChannelByRssUrlUseCaseSync
import com.devlogs.rssfeed.rss_channels.FindRssChannelByUrlUseCaseSync
import com.devlogs.rssfeed.screens.add_rss_channel.presentable_model.RssChannelResultPresentableModel
import com.devlogs.rssfeed.screens.add_rss_channel.presentation_state.AddChannelPresentationAction
import com.devlogs.rssfeed.screens.add_rss_channel.presentation_state.AddChannelPresentationAction.SearchFailedAction
import com.devlogs.rssfeed.screens.add_rss_channel.presentation_state.AddChannelPresentationAction.SearchSuccessAction
import com.devlogs.rssfeed.screens.common.presentation_state.PresentationStateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

class AddRssChannelController @Inject constructor (
    @Named(FRAGMENT_SCOPE) private val stateManager: PresentationStateManager,
    private val findRssChannelByUrlUseCaseSync: FindRssChannelByUrlUseCaseSync,
    private val addNewRssChannelByRssUrlUseCaseSync: AddNewRssChannelByRssUrlUseCaseSync
    ) {

    private val coroutine = CoroutineScope(Dispatchers.Main.immediate)

    fun search(url: String) {
        coroutine.launch {
            val findResult = findRssChannelByUrlUseCaseSync.executes(url)

            if (findResult is FindRssChannelByUrlUseCaseSync.Result.NotFound) {
                stateManager.consumeAction(SearchSuccessAction(null))
            }

            if (findResult is FindRssChannelByUrlUseCaseSync.Result.GeneralError) {
                stateManager.consumeAction(SearchFailedAction("Internal error"))
            }

            if (findResult is FindRssChannelByUrlUseCaseSync.Result.AlreadyAdded) {
                stateManager.consumeAction(SearchSuccessAction(convertChannelEntityToPm(findResult.channel, true)))
            }

            if (findResult is FindRssChannelByUrlUseCaseSync.Result.Found) {
                stateManager.consumeAction(SearchSuccessAction(RssChannelResultPresentableModel(
                    null,
                    findResult.url,
                    findResult.rssUrl,
                    findResult.title,
                    findResult.imageUrl,
                    false
                )))
            }
        }

    }

    private fun convertChannelEntityToPm (entity: RssChannelEntity, isAdded: Boolean) : RssChannelResultPresentableModel {
        return RssChannelResultPresentableModel(
            entity.id,
            entity.url,
            entity.rssUrl,
            entity.title,
            entity.imageUrl,
            isAdded
        )
    }


}