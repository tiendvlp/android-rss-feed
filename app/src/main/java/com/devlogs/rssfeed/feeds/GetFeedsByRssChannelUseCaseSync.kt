package com.devlogs.rssfeed.feeds

import android.util.Log
import com.devlogs.rssfeed.common.background_dispatcher.BackgroundDispatcher
import com.devlogs.rssfeed.domain.entities.RssChannelEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception
import javax.inject.Inject

class GetFeedsByRssChannelUseCaseSync @Inject constructor(private val fireStore: FirebaseFirestore) {

    private val TAG = "GetFeedByRssChannel"

    sealed class Result {
        data class Success (val rssChannel: List<RssChannelEntity>) : Result()
        class GeneralError (val errorMessage: String? = null) : Result()
    }

    suspend fun executes (rssChannelId: String, since: Long, count: Long) : Result = withContext(BackgroundDispatcher) {

        try {
            val snapshot = fireStore
                .document(rssChannelId)
                .collection("Feeds")
                .whereEqualTo("rssChannelId", rssChannelId)
                .whereLessThan("pubDate", since)
                .limit(count)
                .get()
                .await()

            val channels = ArrayList<RssChannelEntity>()
            snapshot.documents.forEach { doc ->
                channels.add(
                    RssChannelEntity(
                        doc["id"].toString(),
                        doc["url"].toString(),
                        doc["rssUrl"].toString(),
                        doc["title"].toString(),
                        doc["description"].toString(),
                        doc["imageUrl"].toString()
                    )
                )
            }
            return@withContext Result.Success(channels)
        } catch (ex: Exception) {
            ex.message?.let { Log.e(TAG, it) }
            return@withContext Result.GeneralError(ex.message)
        }


    }

}