package com.devlogs.rssfeed.feeds

import android.util.Log
import com.devlogs.rssfeed.common.background_dispatcher.BackgroundDispatcher
import com.devlogs.rssfeed.common.helper.LogTarget
import com.devlogs.rssfeed.common.helper.errorLog
import com.devlogs.rssfeed.common.helper.normalLog
import com.devlogs.rssfeed.domain.entities.FeedEntity
import com.devlogs.rssfeed.domain.entities.RssChannelEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception
import javax.inject.Inject

class GetFeedsByRssChannelUseCaseSync @Inject constructor(private val fireStore: FirebaseFirestore): LogTarget {

    private val TAG = "GetFeedByRssChannel"

    sealed class Result {
        data class Success (val rssChannel: List<FeedEntity>) : Result()
        class GeneralError (val errorMessage: String? = null) : Result()
    }

    suspend fun executes (rssChannelId: String, since: Long, count: Long) : Result = withContext(BackgroundDispatcher) {
        try {
            normalLog("channelId: ${rssChannelId}")
            val snapshot = fireStore
                .collection("Feeds")
                .whereEqualTo("rssChannelId", rssChannelId)
                .whereLessThan("pubDate", since)
                .orderBy("pubDate",Query.Direction.DESCENDING)
                .limit(count)
                .get()
                .await()
            normalLog("fetch channel (id:${rssChannelId}) successfully with ${snapshot.documents.size}")
            val channels = ArrayList<FeedEntity>()
            snapshot.documents.forEachIndexed {index, doc ->
                normalLog("converting $index.channel (id: ${doc.getString("id")!!}) to channelEntity")
                channels.add(
                    FeedEntity(
                        doc.getString("id")!!,
                        doc.getString("rssChannelId")!!,
                        doc.getString("channelTitle")!!,
                        doc.getString("title")!!,
                        doc.getString("description")!!,
                        doc.getLong("pubDate")!!,
                        doc.getString("url")!!,
                        doc.getString("author")!!,
                        doc.getString("content")!!,
                        doc.getString("imageUrl")
                    )
                )
            }
            return@withContext Result.Success(channels)
        } catch (ex: Exception) {
            errorLog(ex.message ?: "Unknown Exception Message")
            return@withContext Result.GeneralError(ex.message)
        }
    }

}