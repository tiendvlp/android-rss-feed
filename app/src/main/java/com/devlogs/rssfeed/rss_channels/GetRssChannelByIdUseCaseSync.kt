package com.devlogs.rssfeed.rss_channels

import com.devlogs.rssfeed.common.background_dispatcher.BackgroundDispatcher
import com.devlogs.rssfeed.domain.entities.RssChannelEntity
import com.devlogs.rssfeed.rss_channels.GetRssChannelByIdUseCaseSync.Result.GeneralError
import com.devlogs.rssfeed.rss_channels.GetRssChannelByIdUseCaseSync.Result.Success
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception
import javax.inject.Inject

class GetRssChannelByIdUseCaseSync @Inject constructor(private val fireStore: FirebaseFirestore) {

    sealed class Result {
        data class Success (val channel: RssChannelEntity) : Result ()
        class NotFound () : Result()
        class GeneralError (message: String?) : Result ()
    }

    suspend fun executes (id: String) : Result = withContext(BackgroundDispatcher) {
        try {
          val document = fireStore
              .collection("RssChannels")
              .document(id)
              .get()
              .await()

            return@withContext Success(RssChannelEntity(
                    document["id"].toString(),
                    document["url"].toString(),
                    document["rssUrl"].toString(),
                    document["title"].toString(),
                    document["description"].toString(),
                    document["image"].toString(),))

        } catch (ex: Exception) {
            return@withContext GeneralError(ex.message)
        }
    }
}