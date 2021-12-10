package com.devlogs.rssfeed.update_rss_channels

import com.devlogs.rssfeed.authentication.GetLoggedInUserUseCaseSync
import com.devlogs.rssfeed.common.background_dispatcher.BackgroundDispatcher
import com.devlogs.rssfeed.common.helper.LogTarget
import com.devlogs.rssfeed.common.helper.errorLog
import com.devlogs.rssfeed.common.helper.normalLog
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.lang.RuntimeException
import javax.inject.Inject

class GetFollowedChannelIdUseCaseSync @Inject constructor(private val getLoggedInUserUseCaseSync: GetLoggedInUserUseCaseSync, private val fireStore: FirebaseFirestore): LogTarget {
    sealed class Result {
        class Success (val channels: List<String>) : Result()
        class UnAuthorized () : Result()
        data class GeneralError (val message: String?) : Result()
    }

    suspend fun executes () : Result = withContext(BackgroundDispatcher) {

        val getUserResult = getLoggedInUserUseCaseSync.executes()

        if (getUserResult is GetLoggedInUserUseCaseSync.Result.InValidLogin) {
            normalLog("UnAuthorized")
            return@withContext Result.UnAuthorized()
        }

        if (getUserResult is GetLoggedInUserUseCaseSync.Result.Success) {
            try {
               val snapShot =  fireStore
                    .collection("Users")
                    .document(getUserResult.user.email)
                    .collection("FollowedChannels")
                    .get()
                    .await()

                val channels = ArrayList<String>()
                if (snapShot.isEmpty) {
                    normalLog("User FollowedChannels is empty")
                    return@withContext Result.Success(channels)
                }

                snapShot.documents.forEach { doc ->
                    channels.add(doc.getString("channelId")!!)
                }
                normalLog("Success with count: ${channels.size}")
                return@withContext Result.Success(channels)
            } catch (ex : Exception) {
                ex.message?.let { errorLog(it) }
                return@withContext Result.GeneralError(ex.message)
            }
        }

        throw RuntimeException("UnHandled Result")
    }

}