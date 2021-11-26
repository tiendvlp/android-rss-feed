package com.devlogs.rssfeed.rss_channels

import android.util.Log
import com.devlogs.rssfeed.authentication.GetLoggedInUserUseCaseSync
import com.devlogs.rssfeed.common.background_dispatcher.BackgroundDispatcher
import com.devlogs.rssfeed.domain.entities.RssChannelEntity
import com.devlogs.rssfeed.rss_parser.RSSObject
import com.devlogs.rssfeed.rss_parser.RssChannel
import com.devlogs.rssfeed.rss_parser.RssParser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.RuntimeException
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

    private suspend fun saveToFireStore (rssObject: RSSObject) : Result {
        val channel = rssObject.channel
        try {
            val snapshot =
                fireStore.collection("RssChannels").whereEqualTo("rssUrl", channel.url).get()
                    .await()
            if (snapshot.isEmpty) {
                return createNewChannel(channel)
            } else {
                return updateCurrent(channel, snapshot)
            }
        } catch (ex: Exception) {
            Log.e("AddNewRssUseCase", "GeneralError due to exception when check the duplication of channel: ${ex.message}")
            return Result.GeneralError(ex.message)
        }

    }

    private suspend fun updateCurrent (channel:RssChannel, snapshot: QuerySnapshot) : Result {
        try{
            val updatedChannel = RssChannelEntity(
                snapshot.first()["id"].toString(),
                channel.link,
                channel.url,
                channel.title,
                channel.description,
                channel.image
            )
            fireStore.collection("RssChannels").document(updatedChannel.id)
                .set(updatedChannel).await()
            return addToUserCollection(updatedChannel)
        } catch (e: Exception) {
            e.message?.let { Log.e("AddNewRssUseCase", it) }
            return Result.GeneralError(e.message)
        }
    }

    private suspend fun createNewChannel (channel: RssChannel) : Result {
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
            return addToUserCollection(addedChannel)
        } catch (e: Exception) {
            e.message?.let { Log.e("AddNewRssUseCase", it) }
            return Result.GeneralError(e.message)
        }
    }

}