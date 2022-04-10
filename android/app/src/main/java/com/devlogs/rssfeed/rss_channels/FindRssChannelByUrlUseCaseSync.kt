package com.devlogs.rssfeed.rss_channels

import android.util.Log
import com.devlogs.rssfeed.authentication.GetLoggedInUserUseCaseSync
import com.devlogs.rssfeed.common.background_dispatcher.BackgroundDispatcher
import com.devlogs.rssfeed.common.helper.LogTarget
import com.devlogs.rssfeed.common.helper.warningLog
import com.devlogs.rssfeed.domain.entities.RssChannelEntity
import com.devlogs.rssfeed.encrypt.UrlEncrypt
import com.devlogs.rssfeed.rss.RssUrlFinder
import com.devlogs.rssfeed.rss_parser.RSSObject
import com.devlogs.rssfeed.rss_parser.RssParser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.RuntimeException
import javax.inject.Inject


class FindRssChannelByUrlUseCaseSync @Inject constructor(
    private val fireStore: FirebaseFirestore,
    private val rssUrlFinder: RssUrlFinder,
    private val rssParser: RssParser,
    private val getLoggedInUserUseCaseSync: GetLoggedInUserUseCaseSync
): LogTarget {
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
    /** There are high chance that user enter the rss url directly, and because the parse rss is belong to external server
            * So it won't take long, that's mean in case user don't enter the rss url, it will not effect the perfomance
            * And another reason is find rss url in html page is a heavy task, it take too long, so it worth to bring
            * the parse rss task first
            *
     * */
    suspend fun executes(url: String): Result = withContext(BackgroundDispatcher) {
        Log.d("FindRssChannel", "Find: " + url)

        val parseResult = rssParser.parse(url)

        if (parseResult is RssParser.Result.ConnectionError) {
            return@withContext Result.GeneralError("Can not connect to server, please check internet connection")
        }
        else if (parseResult is RssParser.Result.GeneralError || parseResult is RssParser.Result.InvalidFormat) {
            // try to find the rss url
            val findResult = rssUrlFinder.find(url)
            if (findResult is RssUrlFinder.Result.IllegalUrl) {
                Log.e("FindRssChannel", "Illegal url: $url")
                return@withContext Result.GeneralError("Illegal Url")
            }

            if (findResult is RssUrlFinder.Result.RssNotFound) {
                // check maybe it's self is a rss url
                Log.w("FindRssChannel", "RssChannel notfound $url")
                return@withContext Result.NotFound()
            }

            if (findResult is RssUrlFinder.Result.Success) {
                Log.d("FindRssChannel", "Success")
                return@withContext executes(findResult.rssUrl)
            }
        }
        else if (parseResult is RssParser.Result.Success) {
            Log.d("FindRssChannel", "Success, $url is an rss url")
            return@withContext parseXml(parseResult.rssObject)
        }

        throw RuntimeException("Unexpected result exception")
    }

    private suspend fun parseXml(rssObject: RSSObject): Result {
            val channel = rssObject.channel
            var comparedUrl = channel.url
            if (comparedUrl[comparedUrl.length-1].equals('/')) {
                comparedUrl = comparedUrl.substring(0, comparedUrl.length -1)
            }
            Log.d("FindRssU", "Compare: ${UrlEncrypt.encode(comparedUrl)}")
            val getUserResult = getLoggedInUserUseCaseSync.executes() as GetLoggedInUserUseCaseSync.Result.Success

            val addedChannelId =
                fireStore.collection("Users").document(getUserResult.user.email).collection("AddedChannels").document(UrlEncrypt.encode(comparedUrl)).get()
                    .await()
            if (addedChannelId.exists()) {
                val document = fireStore.collection("RssChannels").document(addedChannelId.get("channelId").toString()).get().await()
                if (document.exists()) {
                    val addedChannel = RssChannelEntity(
                        document["id"].toString(),
                        document["url"].toString(),
                        document["rssUrl"].toString(),
                        document["title"].toString(),
                        document["description"].toString(),
                        document["imageUrl"].toString(),
                    )
                    return Result.AlreadyAdded(addedChannel)
                } else {
                    warningLog("The channel is not exist but user added, let's remove from user list")
                    fireStore.collection("Users")
                        .document(getUserResult.user.email)
                        .collection("AddedChannels")
                        .document(UrlEncrypt.encode(comparedUrl))
                        .delete().await()
                }
            }
            return Result.Found(
                channel.link,
                channel.url,
                channel.title,
                channel.description,
                channel.image
            )
    }
}