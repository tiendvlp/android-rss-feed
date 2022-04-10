package com.devlogs.rssfeed.rss_channels

import com.devlogs.rssfeed.common.background_dispatcher.BackgroundDispatcher
import com.devlogs.rssfeed.domain.entities.RssChannelEntity
import com.devlogs.rssfeed.rss_channels.ReloadRssChannelUseCaseSync.Result.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.RuntimeException
import java.util.*
import javax.inject.Inject

class ReloadRssChannelUseCaseSync @Inject constructor(
    private val fireStore: FirebaseFirestore,
    private val addNewRssChannelByRssUrlUseCaseSync: AddNewRssChannelByRssUrlUseCaseSync,
) {
    sealed class Result {
        class Success: Result()
        class NotFound: Result()
        data class GeneralError(val errorMessage: String?) : Result()
        class UnAuthorized: Result()
    }

    suspend fun executes(channelId: String) = withContext(BackgroundDispatcher) {
        val document = fireStore.collection("RssChannels").document(channelId).get().await()

        if (!document.exists()) {
            return@withContext NotFound()
        }

        val channel = RssChannelEntity(
            document["id"].toString(),
            document["url"].toString(),
            document["rssUrl"].toString(),
            document["title"].toString(),
            document["description"].toString(),
            document["image"].toString(),)

        // reload actually is override an existing one
        val result = addNewRssChannelByRssUrlUseCaseSync.executes(channel.rssUrl)

        if (result is AddNewRssChannelByRssUrlUseCaseSync.Result.GeneralError) {
            return@withContext GeneralError(result.errorMessage)
        }

        if (result is AddNewRssChannelByRssUrlUseCaseSync.Result.UnAuthorized) {
            return@withContext UnAuthorized()
        }

        if (result is AddNewRssChannelByRssUrlUseCaseSync.Result.Success) {
            return@withContext Success()
        }

        throw RuntimeException("UnHandle result")
    }
}