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
        data class Success (val channels: List<RssChannelEntity>) : Result()
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
            val addedChannels: ArrayList<RssChannelEntity>  = ArrayList()
            try {
                querySnapshot.forEach {
                    Log.d("GetUserRssChannel", "add: " + it["channelId"].toString())
                    val channelId = it["channelId"].toString()
                    val channelDocument = fireStore.collection("RssChannels").document(channelId).get().await()
                    addedChannels.add(RssChannelEntity(
                        channelId,
                        channelDocument["url"].toString(),
                        channelDocument["rssUrl"].toString(),
                        channelDocument["title"].toString(),
                        channelDocument["description"].toString(),
                        channelDocument["imageUrl"].toString()
                        ))
                }
            } catch (ex: Exception) {
                ex.message?.let { Log.e("GetUserRssChannel", it) }
                return@withContext Result.GeneralError()
            }
            return@withContext Result.Success(addedChannels)
        }

        throw RuntimeException("UnHandel result")

    }
}