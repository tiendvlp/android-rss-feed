package com.devlogs.rssfeed.android_services

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.devlogs.rssfeed.authentication.GetLoggedInUserUseCaseSync
import com.google.firebase.firestore.FirebaseFirestore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class RssTrackingWorker @AssistedInject constructor (
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val fireStore: FirebaseFirestore,
    private val getLoggedInUserUseCaseSync: GetLoggedInUserUseCaseSync): Worker(appContext, workerParams) {

    override fun doWork(): Result {
        Log.d("Worker","I'm doing work")
        return Result.success()
    }
}