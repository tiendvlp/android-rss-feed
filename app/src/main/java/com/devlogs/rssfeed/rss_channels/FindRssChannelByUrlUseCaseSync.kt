package com.devlogs.rssfeed.rss_channels

import android.util.Log
import com.devlogs.rssfeed.common.background_dispatcher.BackgroundDispatcher
import com.devlogs.rssfeed.domain.entities.RssChannelEntity
import com.devlogs.rssfeed.rss.RssUrlFinder
import com.devlogs.rssfeed.rss_parser.RssParser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.RuntimeException
import javax.inject.Inject


class FindRssChannelByUrlUseCaseSync @Inject constructor(
    private val fireStore: FirebaseFirestore,
    private val rssUrlFinder: RssUrlFinder,
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

        data class AlreadyAdded(val channel: RssChannelEntity) : Result()
        class NotFound() : Result()
        data class GeneralError (val errorMessage: String) : Result()
    }

    suspend fun executes(url: String): Result = withContext(BackgroundDispatcher) {
        Log.d("FindRssChannel", "Find: " + url)
        val findResult = rssUrlFinder.find(url)

        if (findResult is RssUrlFinder.Result.IllegalUrl) {
            Log.e("FindRssChannel", "Illegal url: $url")
            return@withContext Result.GeneralError("Illegal Url")
        }

        if (findResult is RssUrlFinder.Result.RssNotFound) {
            // check maybe it's self is a rss url
            Log.w("FindRssChannel", "RssChannel notfound")
            return@withContext parseXml(url)
        }
        if (findResult is RssUrlFinder.Result.Success) {
            Log.d("FindRssChannel", "Success")
            return@withContext parseXml(findResult.rssUrl)
        }

        throw RuntimeException("Unexpected result exception")
    }

    private suspend fun parseXml(rssUrl: String): Result {
        val getRssChannelResult = rssParser.parse(rssUrl)

        if (getRssChannelResult is RssParser.Result.Success) {
            val rssObject = getRssChannelResult.rssObject
            val channel = rssObject.channel
            var comparedUrl = channel.url
            if (comparedUrl[comparedUrl.length-1].equals('/')) {
                comparedUrl = comparedUrl.substring(0, comparedUrl.length -1)
            }
            Log.d("FindRssU", "Compare: $comparedUrl")
            val snapshot =
                fireStore.collection("RssChannels").whereEqualTo("rssUrl", comparedUrl).get()
                    .await()
            if (!snapshot.isEmpty) {
                val document = snapshot.documents.first()
                val addedChannel = RssChannelEntity(
                    document["id"].toString(),
                    document["url"].toString(),
                    document["rssUrl"].toString(),
                    document["title"].toString(),
                    document["description"].toString(),
                    document["imageUrl"].toString(),
                )
                return Result.AlreadyAdded(addedChannel)
            }
            return Result.Found(
                channel.link,
                channel.url,
                channel.title,
                channel.description,
                channel.image
            )
        } else {
            return Result.NotFound()
        }
    }
}