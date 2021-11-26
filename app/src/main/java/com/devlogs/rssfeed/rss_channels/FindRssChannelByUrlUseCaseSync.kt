package com.devlogs.rssfeed.rss_channels

import com.devlogs.rssfeed.common.background_dispatcher.BackgroundDispatcher
import com.devlogs.rssfeed.domain.entities.RssChannelEntity
import com.devlogs.rssfeed.rss.RssUrlFinder
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.withContext
import javax.inject.Inject


class FindRssChannelByUrlUseCaseSync @Inject constructor(
    private val fireStore: FirebaseFirestore,
    private val rssUrlFinder: RssUrlFinder
) {
    sealed class Result {
        data class Success (val channel: RssChannelEntity, val isAdded:Boolean) : Result()
        data class GeneralError (val errorMessage: String?) : Result()
    }

    suspend fun executes (url: String) : Result = withContext(BackgroundDispatcher) {

        val findResult = rssUrlFinder.find(url)

        Result.GeneralError("")
    }

}