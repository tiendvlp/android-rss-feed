package com.devlogs.rssfeed.rss_channels

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.devlogs.rssfeed.authentication.GetLoggedInUserUseCaseSync
import com.devlogs.rssfeed.common.background_dispatcher.BackgroundDispatcher
import com.devlogs.rssfeed.domain.entities.FeedEntity
import com.devlogs.rssfeed.domain.entities.RssChannelEntity
import com.devlogs.rssfeed.encrypt.UrlEncrypt
import com.devlogs.rssfeed.rss_parser.RSSObject
import com.devlogs.rssfeed.rss_parser.RssFeed
import com.devlogs.rssfeed.rss_parser.RssParser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class AddNewRssChannelByRssUrlUseCaseSync @Inject constructor(
    private val fireStore: FirebaseFirestore,
    private val rssParser: RssParser,
    private val getCurrentLoggedInUserUseCaseSync: GetLoggedInUserUseCaseSync
) {
    sealed class Result {
        data class Success (val rssChannel: RssChannelEntity): Result()
        data class GeneralError(val errorMessage: String?) : Result()
        class UnAuthorized: Result()
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

    @SuppressLint("NewApi")
    private suspend fun saveToFireStore (rssObject: RSSObject) : Result {
        val rssChannel = rssObject.channel
        try {
            var id = UrlEncrypt.encode(rssChannel.url)

            val channelEntity = RssChannelEntity(
                id,
                rssChannel.link,
                rssChannel.url,
                rssChannel.title,
                rssChannel.description,
                rssChannel.image
            )
            fireStore.collection("RssChannels").document(channelEntity.id).set(channelEntity).await()
                return saveChannelFeeds(channelEntity, rssObject.feeds)
        } catch (ex: Exception) {
            Log.e("AddNewRssUseCase", "GeneralError due to exception when check the duplication of channel: ${ex.message}")
            return Result.GeneralError(ex.message)
        }

    }

    @SuppressLint("NewApi")
    private suspend fun saveChannelFeeds (channel: RssChannelEntity, feeds: List<RssFeed>) : Result {
        try {
            feeds.forEach {
                Log.d("AddNewRssUseCase", "save: ${it.title}")
                val pubDate: Date = SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(it.pubDate)
                val id = UrlEncrypt.encode(it.guid)
                Log.d("AddNewRssUseCase", "save id: ${id}")
                val imgUrl: String = getImageUrlInContent(it.content)
                val entity = FeedEntity(id, channel.id, channel.title, it.title, it.description, pubDate.time, it.guid, it.author, it.content, imgUrl)
                fireStore
                    .collection("Feeds")
                    .document(entity.id)
                    .set(entity)
                    .await()
            }
            return addToUserCollection(channel)
        } catch (e: Exception) {
            e.message?.let { m -> Log.e("AddNewRssUseCase", m) }
            return Result.GeneralError(e.message)
        }
    }

    private fun getImageUrlInContent(content: String): String {
        val searchTarget = content.replace("\\s+","")
        val imageTagIndex = searchTarget.indexOf("<img")
        Log.d("AddNewRssUseCase", imageTagIndex.toString())
        if (imageTagIndex == -1) {
            return ""
        }

        val srcPropIndex = searchTarget.indexOf("src", imageTagIndex)
        Log.d("AddNewRssUseCase", srcPropIndex.toString())

        if (srcPropIndex == -1 ) {
            return ""
        }

        val quoteIndex = srcPropIndex+3+1
        val quoteChar = searchTarget[quoteIndex]
        // make sure it find the right quote
        Log.d("AddNewRssUseCase", "quote: ${quoteChar}")
        Log.d("AddNewRssUseCase", "hello: ${searchTarget.indexOf(quoteChar, quoteIndex + 1)}")

        return searchTarget.substring(quoteIndex + 1, searchTarget.indexOf(quoteChar, quoteIndex + 1))
    }

}