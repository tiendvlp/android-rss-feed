package com.devlogs.rssfeed.receive_channel_update

import com.devlogs.rssfeed.common.background_dispatcher.BackgroundDispatcher
import com.devlogs.rssfeed.common.helper.LogTarget
import com.devlogs.rssfeed.common.helper.normalLog
import com.devlogs.rssfeed.update_rss_channels.GetFollowedChannelIdUseCaseSync
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.withContext
import java.lang.RuntimeException
import javax.inject.Inject

class SubscribeFollowedChannelsNotificationUseCaseSync @Inject constructor(
    private val firebaseMessaging: FirebaseMessaging,
    private val getFollowedChannelIdUseCaseSync: GetFollowedChannelIdUseCaseSync
) : LogTarget {
    sealed class Result {
        class Success() : Result()
        data class GeneralError(val errorMessage: String? = null) : Result()
        class UnAuthorized() : Result()
    }

    suspend fun executes() = withContext(BackgroundDispatcher) {
        val channelIds: List<String> = getFollowedChannelIdUseCaseSync.executes().let { result ->
            if (result is GetFollowedChannelIdUseCaseSync.Result.Success) {
                return@let result.channels
            } else if (result is GetFollowedChannelIdUseCaseSync.Result.GeneralError) {
                return@withContext Result.GeneralError()
            } else if (result is GetFollowedChannelIdUseCaseSync.Result.UnAuthorized) {
                return@withContext Result.UnAuthorized()
            } else {
                throw RuntimeException("Invalid state")
            }
        }
        channelIds.forEach { id ->
            val topicId = id.replace("|", "~").replace("=", "%")
            firebaseMessaging.subscribeToTopic(topicId)
                .addOnSuccessListener {
                    normalLog("subscribe to topic: $topicId success")
                }
                .addOnFailureListener {
                    normalLog("subscribe to topic: $topicId failed due to $it.")
                }.addOnCanceledListener {
                    normalLog("subscribe to topic: $topicId is cancel")
                }
        }
        return@withContext Result.Success()
    }
}

