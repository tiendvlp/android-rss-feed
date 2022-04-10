package com.devlogs.rssfeed.rss_channels

import com.devlogs.rssfeed.authentication.GetLoggedInUserUseCaseSync
import com.devlogs.rssfeed.common.background_dispatcher.BackgroundDispatcher
import com.devlogs.rssfeed.domain.entities.RssChannelEntity
import com.devlogs.rssfeed.rss_channels.GetRssChannelByIdUseCaseSync.Result.GeneralError
import com.devlogs.rssfeed.rss_channels.GetRssChannelByIdUseCaseSync.Result.Success
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.lang.RuntimeException
import javax.inject.Inject

class GetRssChannelByIdUseCaseSync @Inject constructor(private val fireStore: FirebaseFirestore, private val getLoggedInUserUseCaseSync: GetLoggedInUserUseCaseSync) {

    sealed class Result {
        data class Success (val channel: RssChannelEntity, val isFollowed: Boolean) : Result ()
        class NotFound () : Result()
        class GeneralError (message: String?) : Result ()
        class UnAuthorized : Result ()
    }

    suspend fun executes (id: String) : Result = withContext(BackgroundDispatcher) {
        try {
          val document = fireStore
              .collection("RssChannels")
              .document(id)
              .get()
              .await()

            val getUserResult = getLoggedInUserUseCaseSync.executes()

            if (getUserResult is GetLoggedInUserUseCaseSync.Result.InValidLogin) {
                return@withContext Result.UnAuthorized()
            }

            if (getUserResult is GetLoggedInUserUseCaseSync.Result.Success) {

            val isFollowed = !fireStore.collection("Users")
                .document(getUserResult.user
                    .email)
                .collection("FollowedChannels")
                .whereEqualTo("channelId", id)
                .get()
                .await().isEmpty

            return@withContext Success(RssChannelEntity(
                    document["id"].toString(),
                    document["url"].toString(),
                    document["rssUrl"].toString(),
                    document["title"].toString(),
                    document["description"].toString(),
                    document["image"].toString(),), isFollowed)
            }
            throw RuntimeException("UnExpected Result : ${getUserResult.javaClass}")
        } catch (ex: Exception) {
            return@withContext GeneralError(ex.message)
        }
    }
}