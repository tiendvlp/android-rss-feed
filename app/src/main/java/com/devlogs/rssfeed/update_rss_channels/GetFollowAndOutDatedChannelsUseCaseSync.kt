package com.devlogs.rssfeed.update_rss_channels

import com.devlogs.rssfeed.common.background_dispatcher.BackgroundDispatcher
import com.devlogs.rssfeed.common.helper.LogTarget
import com.devlogs.rssfeed.common.helper.errorLog
import com.devlogs.rssfeed.common.helper.warningLog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.lang.RuntimeException
import javax.inject.Inject

class GetFollowAndOutDatedChannelsUseCaseSync @Inject constructor(private val getFollowedChannelIdUseCaseSync: GetFollowedChannelIdUseCaseSync,
                                                                  private val fireStore: FirebaseFirestore): LogTarget {
    sealed class Result {
        class Success (val channels: List<String>) : Result()
        class UnAuthorized () : Result()
        data class GeneralError (val message: String?) : Result()
    }

    private val thirtyMin = 1000 * 60 * 30

    suspend fun executes (count: Long =  -1) : Result = withContext(BackgroundDispatcher) {

        val getFollowedChannelIdResult = getFollowedChannelIdUseCaseSync.executes()

        if (getFollowedChannelIdResult is GetFollowedChannelIdUseCaseSync.Result.UnAuthorized) {
            errorLog("UnAuthorized")
            return@withContext Result.UnAuthorized()
        }
        if (getFollowedChannelIdResult is GetFollowedChannelIdUseCaseSync.Result.GeneralError) {
            val errorMessage = getFollowedChannelIdResult.message!!
            errorLog("GeneralError: $errorMessage")
            return@withContext Result.GeneralError(errorMessage)
        }

        if (getFollowedChannelIdResult is GetFollowedChannelIdUseCaseSync.Result.Success) {
            val followedChannelId = getFollowedChannelIdResult.channels
            val channelsId = ArrayList<String> ()

            if (followedChannelId.isEmpty()) {
                warningLog("User does not follow any channel")
                return@withContext Result.Success(channelsId)
            }

            var query = fireStore.collection("RssChannels")
                .whereIn("id", followedChannelId)
                .whereLessThanOrEqualTo("latestUpdate", thirtyMin)
                .orderBy("latestUpdate", Query.Direction.ASCENDING)

            if (count > 0) {
                query = query.limit(count)
            }

            try {
                val snapShot = query.get().await()
                if (snapShot.isEmpty) {
                    warningLog("There is no channel need to update")
                    return@withContext Result.Success(channelsId)
                }
                snapShot.documents.forEach { document ->
                    channelsId.add(document.getString("channelId")!!)
                }
                return@withContext Result.Success(channelsId)
            } catch (ex : Exception) {
                return@withContext Result.GeneralError(ex.message)
            }
        }

        throw RuntimeException("UnHandled Result")
    }

}