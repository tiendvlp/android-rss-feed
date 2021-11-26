package com.devlogs.rssfeed.rss_channels

import com.devlogs.rssfeed.common.background_dispatcher.BackgroundDispatcher
import com.devlogs.rssfeed.domain.entities.RssChannelEntity
import com.devlogs.rssfeed.rss.RssUrlFinder
import com.devlogs.rssfeed.rss_parser.RssParser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.withContext
import javax.inject.Inject


class FindRssChannelByUrlUseCaseSync @Inject constructor(
    private val fireStore: FirebaseFirestore,
    private val rssUrlFinder: RssUrlFinder,
    private val rssParser: RssParser
) {
    sealed class Result {
        data class Found (val url: String, val rssUrl:String, val title: String, val description: String, val imageUrl: String): Result()
        data class AlreadyAdded (val channel: RssChannelEntity) : Result()
        class NotFound () : Result()
        class NetworkError () : Result()
        data class GeneralError (val errorMessage: String?) : Result()
    }

    suspend fun executes (url: String) : Result = withContext(BackgroundDispatcher) {

        // check on firestore
        val findResult = rssUrlFinder.find(url)

        if (findResult is RssUrlFinder.Result.RssNotFound) {
            return@withContext Result.NotFound()
        }
        if (findResult is RssUrlFinder.Result.NetworkError) {
            return@withContext Result.NetworkError()
        }
        if (findResult is RssUrlFinder.Result.Success) {
            val getRssChannelResult = rssParser.parse(findResult.rssUrl)

            if (getRssChannelResult is RssParser.Result.GeneralError) {
                return@withContext Result.GeneralError("Found rss url (${findResult.rssUrl} but can not parse the rss channel")
            }

            if (getRssChannelResult is RssParser.Result.Success) {
                val rssObject = getRssChannelResult.rssObject
                val channel = rssObject.channel
                return@withContext Result.Found(channel.link, channel.url,channel.title, channel.description, channel.image)
            }
        }

        Result.GeneralError("")
    }

}