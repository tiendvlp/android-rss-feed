package com.devlogs.rssfeed.rss_channels

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.devlogs.rssfeed.authentication.GetLoggedInUserUseCaseSync
import com.devlogs.rssfeed.common.background_dispatcher.BackgroundDispatcher
import com.devlogs.rssfeed.common.helper.LogTarget
import com.devlogs.rssfeed.common.helper.errorLog
import com.devlogs.rssfeed.common.helper.normalLog
import com.devlogs.rssfeed.common.helper.warningLog
import com.devlogs.rssfeed.domain.entities.FeedEntity
import com.devlogs.rssfeed.domain.entities.RssChannelEntity
import com.devlogs.rssfeed.encrypt.UrlEncrypt
import com.devlogs.rssfeed.rss_parser.RSSObject
import com.devlogs.rssfeed.rss_parser.RssFeed
import com.devlogs.rssfeed.rss_parser.RssParser
import com.google.firebase.FirebaseException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.gildor.coroutines.okhttp.await
import java.lang.NumberFormatException
import java.lang.RuntimeException
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class AddNewRssChannelByRssUrlUseCaseSync @Inject constructor(
    private val fireStore: FirebaseFirestore,
    private val rssParser: RssParser,
    private val client: OkHttpClient,
    private val getCurrentLoggedInUserUseCaseSync: GetLoggedInUserUseCaseSync
) : LogTarget {
    sealed class Result {
        data class Success (val rssChannel: RssChannelEntity): Result()
        data class GeneralError(val errorMessage: String?) : Result()
        class UnAuthorized: Result()
    }

    suspend fun executes(rssUrl: String) = withContext(BackgroundDispatcher) {
        val getRssChannelResult = rssParser.parse(rssUrl)
        if (getRssChannelResult is RssParser.Result.ConnectionError ) {
            Log.e("AddNewRssUseCase", "General error due to Found rss content (${rssUrl} but can not parse the rss channel")
            return@withContext Result.GeneralError("Connection error please check your internet")
        }
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
            return@withContext saveToFireStore(rssObject)
        }
            throw RuntimeException("UnHandle result")
    }

    private suspend fun addToUserCollection (channel: RssChannelEntity) : Result{
        val result = getCurrentLoggedInUserUseCaseSync.executes()
        if (result is GetLoggedInUserUseCaseSync.Result.InValidLogin) {
            return Result.UnAuthorized()
        } else if (result is GetLoggedInUserUseCaseSync.Result.Success) {
            try {
                fireStore.collection("Users")
                    .document(result.user.email)
                    .collection("AddedChannels")
                    .document(channel.id).set(mapOf("channelId" to channel.id))
                    .await()
                return Result.Success(channel)
            } catch (ex : Exception) {
                Log.e("AddNewRssUseCase", "GeneralError due to Add to user collection failed")
                return Result.GeneralError("Add to user collection failed")
            }
        }

        throw RuntimeException("UnHandle result")
    }

    private suspend fun isUpdated (channelId: String, since: Long) : Boolean {
        normalLog("Check channel {$channelId} is updated or not")
        val channel = fireStore.collection("RssChannels")
            .document(channelId).get().await()
        if (channel.exists()) {
            val latestUpdate = channel.getLong("latestUpdate")!!
            return since <= latestUpdate
        }
        return false
    }

    @SuppressLint("NewApi")
    private suspend fun saveToFireStore (rssObject: RSSObject) : Result {
        val rssChannel = rssObject.channel

        try {
            var id = UrlEncrypt.encode(rssChannel.url)
            var rssUrl = rssChannel.url
            var link = rssChannel.link
            if (rssUrl[rssUrl.length-1].equals('/')) {
                rssUrl = rssUrl.substring(0, rssUrl.length-1)
            }
            if (link[link.length-1].equals('/')) {
                link = link.substring(0, link.length-1)
            }

            var title = rssChannel.title

            title = title.replace("rss", "", true).trim()

            val channelEntity = RssChannelEntity(
                id,
                link,
                rssUrl,
                title,
                rssChannel.description,
                rssChannel.image
            )

            val latestPubDate = getFeedPubDate(rssObject.feeds.first().pubDate)
            normalLog("The newest item in ${rssChannel.title} channel is: ${rssObject.feeds.first().title}")
//            if (isUpdated(id, latestPubDate.time)) {
//                normalLog("The channel already updated")
//                return Result.Success(channelEntity)
//            }
            normalLog("Channel is not updated")
            normalLog("Write channel to firestore")
            fireStore.collection("RssChannels").document(channelEntity.id)
                .set(mapOf(
                    "id" to channelEntity.id,
                    "url" to channelEntity.url,
                    "rssUrl" to channelEntity.rssUrl,
                    "title" to channelEntity.title,
                    "description" to channelEntity.description,
                    "imageUrl" to channelEntity.imageUrl,
                    "latestUpdate" to System.currentTimeMillis()
                ))
            normalLog("Saving channel")
            return saveChannelFeeds(channelEntity, rssObject.feeds)
        } catch (ex: FirebaseException) {
            Log.e("AddNewRssUseCase", "GeneralError due to exception when check the duplication of channel: ${ex.message}")
            return Result.GeneralError(ex.message)
        }

    }

    private fun getFeedPubDate (pubDate: String) : Date {
        val pubDateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
        pubDateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return pubDateFormat.parse(pubDate)
    }

    @SuppressLint("NewApi")
    private suspend fun saveChannelFeeds (channel: RssChannelEntity, feeds: List<RssFeed>) : Result = withContext(BackgroundDispatcher) {
        try {
            val startTime = System.currentTimeMillis()
            val defered = feeds.map {
                async {
                    val startTime = System.currentTimeMillis()
                    val pubDate: Date = getFeedPubDate(it.pubDate)
                    val id = UrlEncrypt.encode(it.link)
                    Log.d("AddNewRssUseCase", "save id: ${id} and link is ${it.link}")
                    var imgUrl: String = it.thumbnail
                    if (imgUrl.isNullOrBlank()) {
                        normalLog("Image not found, start finding in content")
                        imgUrl = getImageUrlInContentTag(it.content)
                    }
                    if (imgUrl.isNullOrBlank()) {
                        normalLog("Image not found, start finding in feed source")
                        imgUrl = getImageUrlInContent(it.link)
                    }
                    val entity = FeedEntity(
                        id,
                        channel.id,
                        channel.title,
                        it.title,
                        it.description,
                        pubDate.time,
                        it.link,
                        it.author,
                        it.content,
                        imgUrl
                    )
                    fireStore
                        .collection("Feeds")
                        .document(entity.id)
                        .set(entity)
                        .await()
                    normalLog("ParseTime: ${pubDate.time}")
                    val endTime = System.currentTimeMillis()
                    normalLog("Total saving time single feed: ${(endTime.toDouble() - startTime.toDouble())/1000} seconds")
                }

            }.toTypedArray()

           awaitAll(*defered)
            val endTime = System.currentTimeMillis()
            normalLog("Total saving time: ${(endTime.toDouble() - startTime.toDouble())/1000} seconds")
            return@withContext addToUserCollection(channel)
        } catch (e: Exception) {
            e.message?.let { m -> Log.e("AddNewRssUseCase", m) }
            return@withContext Result.GeneralError(e.message)
        }
    }

    private fun getImageUrlInContentTag (content: String) : String {
            val searchTarget = content.replace("\\s+", "")
            val imageTagIndex = searchTarget.indexOf("<img", 0)
            if (imageTagIndex == -1) {
                normalLog("There is no img tag in content")
                return ""
            }

            val srcPropIndex = searchTarget.indexOf("src", imageTagIndex)
            Log.d("AddNewRssUseCase", srcPropIndex.toString())

            if (srcPropIndex == -1) {
                return ""
            }

            val quoteIndex = srcPropIndex + 3 + 1
            val quoteChar = searchTarget[quoteIndex]
            // make sure it find the right quote
            Log.d("AddNewRssUseCase", "quote: ${quoteChar}")
            Log.d("AddNewRssUseCase", "hello: ${searchTarget.indexOf(quoteChar, quoteIndex + 1)}")

            return searchTarget.substring(
                quoteIndex + 1,
                searchTarget.indexOf(quoteChar, quoteIndex + 1)
            )
    }

    private suspend fun getImageUrlInContent(feedUrl: String): String {
        normalLog("Chay toi day roi nha 0")
        val request = Request.Builder()
            .url(feedUrl)
            .get()
            .build()
            val response = client.newCall(request).await()
            // remove all white space
            var searchTarget = response.body!!.string().replace("\\s+", "")
            // find the header tag, it's really important, because it prevent us to not get the image that not related to the topic
            var startIndex = searchTarget.indexOf("</header>")
            if (startIndex == -1) {
                return ""
            }
            normalLog("Chay toi day roi nha 1")
            startIndex = searchTarget.indexOf("<img", startIndex)
            if (startIndex == -1) {
                return ""
            }
            val endIndex = searchTarget.indexOf(">", startIndex)
            searchTarget = searchTarget.substring(startIndex, endIndex)
            if (startIndex == -1) {
                return ""
            }
            normalLog("Chay toi day roi nha 2")
            var imageUrl = getPropValue("srcset", searchTarget, {
                val width = getPropValue("width", searchTarget).toDoubleOrNull()
                var height: Double? = null
                if (width == null) {
                    height = getPropValue("height", searchTarget).toDoubleOrNull()
                }
                it.startsWith("http", ignoreCase = true) && ((width == null || width > 200) && (height == null || height > 100))
            })
            if (imageUrl.isNullOrBlank()) {
                imageUrl = getPropValue("src", searchTarget, {
                    val width = getPropValue("width", searchTarget).toDoubleOrNull()
                    var height: Double? = null
                    if (width == null) {
                        height = getPropValue("height", searchTarget).toDoubleOrNull()
                    }
                    it.startsWith(
                        "http",
                        ignoreCase = true
                    ) && ((width == null || width > 200) && (height == null || height!! > 100))
                })
            }
            normalLog("Chay toi day roi nha 3")
            if (imageUrl.isNotEmpty()) {
                normalLog("found image for feed {$feedUrl}: $imageUrl")
                return imageUrl
            } else {
                normalLog("Not Found image for feed {$feedUrl}")
            }
            return "";
    }

    private fun getPropValue (propName: String, searchTarget: String, valueValidation: ((String) -> Boolean)? = null):  String {
        var contentOfSrcProp = "";
        var startIndex = 0
        startIndex = searchTarget.indexOf(propName, startIndex )
        while (startIndex != -1) {
            val quoteIndex = startIndex + propName.length + 1
            if (quoteIndex >= searchTarget.length) {
                return ""
            }
            val quoteChar = searchTarget[quoteIndex]

            // value of properties
            contentOfSrcProp = searchTarget.substring(
                quoteIndex + 1,
                searchTarget.indexOf(quoteChar, quoteIndex + 1)
            )

            if (valueValidation != null) {
                if (valueValidation.invoke(contentOfSrcProp) == true) {
                    return contentOfSrcProp
                }
            } else {
                return contentOfSrcProp
            }
            startIndex = searchTarget.indexOf(propName, startIndex + 1)
        }
        return "";
    }

    data class Size (val width : Double?, val height: Double? )
}