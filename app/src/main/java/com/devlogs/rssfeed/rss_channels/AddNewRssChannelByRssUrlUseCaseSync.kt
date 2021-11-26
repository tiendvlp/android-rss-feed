package com.devlogs.rssfeed.rss_channels

import android.util.Log
import com.devlogs.rssfeed.common.background_dispatcher.BackgroundDispatcher
import com.devlogs.rssfeed.domain.entities.RssChannelEntity
import com.devlogs.rssfeed.rss_parser.RssParser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.RuntimeException
import java.util.*
import javax.inject.Inject

class AddNewRssChannelByRssUrlUseCaseSync @Inject constructor(
    private val fireStore: FirebaseFirestore,
    private val rssParser: RssParser
) {
    sealed class Result {
        data class Found(
            val url: String,
            val rssUrl: String,
            val title: String,
            val description: String,
            val imageUrl: String
        ) : Result()

        class Success : Result()
        data class GeneralError(val errorMessage: String?) : Result()
    }

    suspend fun executes(rssUrl: String) = withContext(BackgroundDispatcher) {
        val getRssChannelResult = rssParser.parse(rssUrl)

        if (getRssChannelResult is RssParser.Result.GeneralError) {
            Log.e("AddNewRssUseCase", "General error due to Found rss content (${rssUrl} but can not parse the rss channel")
            return@withContext Result.GeneralError("Found rss content (${rssUrl} but can not parse the rss channel")
        }
        if (getRssChannelResult is RssParser.Result.InvalidFormat) {
            Log.e("AddNewRssUseCase", "General error due to Parser general error")
            return@withContext Result.GeneralError("Parser general error")
        }
        if (getRssChannelResult is RssParser.Result.Success) {
            val rssObject = getRssChannelResult.rssObject
            val channel = rssObject.channel
            val snapshot =
                fireStore.collection("RssChannels").whereEqualTo("rssUrl", channel.link).get()
                    .await()
            if (snapshot.isEmpty) {
                val addedChannel = RssChannelEntity(
                    UUID.randomUUID().toString().substring(0,8),
                    channel.link,
                    channel.url,
                    channel.title,
                    channel.description,
                    channel.image
                )
                try {
                    fireStore.collection("RssChannels").document(addedChannel.id).set(addedChannel).await()
                } catch (e: Exception) {
                    e.message?.let { Log.e("AddNewRssUseCase", it) }
                    return@withContext Result.GeneralError(e.message)
                }
            } else {
                try{
                fireStore.collection("RssChannels").document(snapshot.first()["id"].toString())
                    .set(hashMapOf(
                        "id" to snapshot.first()["id"].toString(),
                        "title" to channel.title,
                        "url" to channel.link,
                        "rssUrl" to channel.url,
                        "description" to channel.description,
                        "imageUrl" to channel.image,
                        "id" to UUID.randomUUID().toString().substring(0,8),
                    )).await()
            } catch (e: Exception) {
                e.message?.let { Log.e("AddNewRssUseCase", it) }
                return@withContext Result.GeneralError(e.message)
            }
            }
            return@withContext Result.Success()
        }
            throw RuntimeException("UnHandle result")
    }

}