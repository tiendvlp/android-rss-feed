package com.devlogs.rssfeed.rss_channels

import android.util.Log
import com.devlogs.rssfeed.authentication.GetLoggedInUserUseCaseSync
import com.devlogs.rssfeed.common.background_dispatcher.BackgroundDispatcher
import com.devlogs.rssfeed.domain.entities.RssChannelEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetUserRssChannelsUseCaseSync @Inject constructor(private val getLoggedInUserUseCaseSync: GetLoggedInUserUseCaseSync, private val fireStore: FirebaseFirestore) {
    sealed class Result {
        data class Success (val channels: Set<RssChannelEntity>) : Result()
        data class GeneralError (val errorMessage: String? = null) : Result()
        class UnAuthorized : Result()
    }

    suspend fun executes () = withContext(BackgroundDispatcher) {
        Log.d("GetUserRssChannel", "RUnnn")
        val getLoggedInUserResult = getLoggedInUserUseCaseSync.executes()

        if (getLoggedInUserResult is GetLoggedInUserUseCaseSync.Result.InValidLogin) {
            Log.e("GetUserRssChannel", "UnAuthorizied")
            return@withContext Result.UnAuthorized()
        }

        if (getLoggedInUserResult is GetLoggedInUserUseCaseSync.Result.Success) {
           val querySnapshot = fireStore.collection("Users").document(getLoggedInUserResult.user.email).collection("AddedChannels").get().await()
            val addedChannels: HashSet<RssChannelEntity>  = HashSet()
            try {
                querySnapshot.forEach {
                    Log.d("GetUserRssChannel", "add: " + it["channelId"].toString())
                    val channelId = it["channelId"].toString()
                    val channelDocument = fireStore.collection("RssChannels").document(channelId).get().await()
                    val channel = RssChannelEntity(
                        channelId,
                        channelDocument["url"].toString(),
                        channelDocument["rssUrl"].toString(),
                        channelDocument["title"].toString(),
                        channelDocument["description"].toString(),
                        channelDocument["imageUrl"].toString()
                    )
                    addedChannels.add(channel)
                    Log.d("GetUserRssChannel", "get: " + channel.title)
                }
                Log.d("GetUserRssChannel", "get: " + addedChannels.elementAt(0).title)
                return@withContext Result.Success(addedChannels)
            } catch (ex: Exception) {
                ex.message?.let { Log.e("GetUserRssChannel", it) }
                return@withContext Result.GeneralError()
            }
        }

        throw RuntimeException("UnHandel result")

    }
}