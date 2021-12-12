package com.devlogs.rssfeed.android_services

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.devlogs.rssfeed.common.helper.LogTarget
import com.devlogs.rssfeed.common.helper.errorLog
import com.devlogs.rssfeed.common.helper.normalLog
import com.devlogs.rssfeed.rss_channels.ReloadRssChannelUseCaseSync
import com.devlogs.rssfeed.update_rss_channels.GetFollowAndOutDatedChannelsUseCaseSync
import com.devlogs.rssfeed.update_rss_channels.GetOutDatedChannelsUseCaseSync
import com.google.firebase.firestore.FirebaseFirestore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.lang.RuntimeException
import java.util.concurrent.TimeUnit

@HiltWorker
class TrackingAllChannelWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val reloadRssChannelUseCaseSync: ReloadRssChannelUseCaseSync,
    private val getOutDatedChannelsUseCaseSync: GetOutDatedChannelsUseCaseSync,
) : CoroutineWorker(appContext, workerParams), LogTarget {
    companion object {
        const val TAG = "RssTrackingAllWorker"
        fun startWhenCharging (context: Context) {
            val doWorkerRequest = PeriodicWorkRequestBuilder<TrackingAllChannelWorker>(20, TimeUnit.MINUTES)
                .setConstraints(Constraints.Builder()
                    .setRequiresCharging(true)
                    .build())
                .build()
            WorkManager.getInstance(context.applicationContext)
                .enqueueUniquePeriodicWork(TAG, ExistingPeriodicWorkPolicy.REPLACE ,doWorkerRequest)
        }

        fun stop (context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(TAG)
        }
    }
    override suspend fun doWork(): Result {
        val startTime = System.currentTimeMillis()
        val channelsIds: List<String> =
            getOutDatedChannelsUseCaseSync.executes(40).let { result ->
                if (result is GetOutDatedChannelsUseCaseSync.Result.Success) {
                    normalLog("There are ${result.channels.size} need to be update")
                    return@let result.channels
                } else {
                    if (result is GetOutDatedChannelsUseCaseSync.Result.GeneralError) {
                        errorLog("Get channel failed due to GeneralError: ${result.message}")
                        return@doWork Result.failure()
                    }  else {
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
        normalLog("Exceution take: $executionTime} seconds")
        return Result.success()
    }
}