package com.devlogs.rssfeed.android_services

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.devlogs.rssfeed.common.helper.LogTarget
import com.devlogs.rssfeed.common.helper.errorLog
import com.devlogs.rssfeed.common.helper.normalLog
import com.devlogs.rssfeed.rss_channels.ReloadRssChannelUseCaseSync
import com.devlogs.rssfeed.update_rss_channels.GetFollowAndOutDatedChannelsUseCaseSync
import com.google.firebase.firestore.FirebaseFirestore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.lang.RuntimeException

@HiltWorker
class RssTrackingWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val reloadRssChannelUseCaseSync: ReloadRssChannelUseCaseSync,
    private val getFollowAndOutDatedChannelsUseCaseSync: GetFollowAndOutDatedChannelsUseCaseSync,
) : CoroutineWorker(appContext, workerParams), LogTarget {

    override suspend fun doWork(): Result {
        val startTime = System.currentTimeMillis()
        val channelsIds: List<String> =
            getFollowAndOutDatedChannelsUseCaseSync.executes(10).let { result ->
                if (result is GetFollowAndOutDatedChannelsUseCaseSync.Result.Success) {
                    normalLog("There are ${result.channels.size} need to be update")
                    return@let result.channels
                } else {
                    if (result is GetFollowAndOutDatedChannelsUseCaseSync.Result.GeneralError) {
                        errorLog("Get channel failed due to GeneralError: ${result.message}")
                        return@doWork Result.failure()
                    } else if (result is GetFollowAndOutDatedChannelsUseCaseSync.Result.UnAuthorized) {
                        errorLog("Get channel failed due to UnAuthorized error, required user to reloggin")
                        return@doWork Result.failure()
                    } else {
                        throw RuntimeException("UnExpected result: ${result.javaClass}")
                    }
                }
            }

        for (id in channelsIds) {
            reloadRssChannelUseCaseSync.executes(id).let { result ->
                if (result is ReloadRssChannelUseCaseSync.Result.GeneralError) {
                    errorLog("Reload channel $id failed due to GeneralError: ${result.errorMessage}")
                } else if (result is ReloadRssChannelUseCaseSync.Result.NotFound) {
                    errorLog("Reload failed: channel $id does not exist")
                } else if (result is ReloadRssChannelUseCaseSync.Result.UnAuthorized) {
                    errorLog("Reload channel failed due to UnAuthorized error, required user to reloggin")
                } else if (result is ReloadRssChannelUseCaseSync.Result.Success) {
                    normalLog("Update channel $id success")
                } else {
                    throw RuntimeException("UnExpected result: ${result.javaClass}")
                }
            }
        }
        val endTime = System.currentTimeMillis()
        val executionTime: Double = (endTime - startTime) / (1000.toDouble())
        normalLog("Excution take: $executionTime} seconds")
        return Result.success()
    }
}