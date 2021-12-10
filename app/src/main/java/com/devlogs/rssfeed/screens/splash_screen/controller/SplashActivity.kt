package com.devlogs.rssfeed.screens.splash_screen.controller

import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.devlogs.rssfeed.R
import com.devlogs.rssfeed.android_services.RssTrackingWorker
import com.devlogs.rssfeed.application.ApplicationStateManager
import com.devlogs.rssfeed.authentication.ValidateLoginUseCaseSync
import com.devlogs.rssfeed.common.background_dispatcher.BackgroundDispatcher
import com.devlogs.rssfeed.common.helper.LogTarget
import com.devlogs.rssfeed.common.helper.normalLog
import com.devlogs.rssfeed.feeds.GetFeedsByRssChannelUseCaseSync
import com.devlogs.rssfeed.rss_channels.AddNewRssChannelByRssUrlUseCaseSync
import com.devlogs.rssfeed.rss_channels.FindRssChannelByUrlUseCaseSync
import com.devlogs.rssfeed.rss_channels.GetUserRssChannelsUseCaseSync
import com.devlogs.rssfeed.screens.login.controller.LoginActivity
import com.devlogs.rssfeed.screens.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity(), LogTarget {

    companion object {
        fun start (context: Context) {
            context.startActivity(Intent(context, SplashActivity::class.java))
        }
    }

    @Inject
    protected lateinit var sharedPreferences: SharedPreferences;
    @Inject
    protected lateinit var validateLoginUseCaseSync: ValidateLoginUseCaseSync
    @Inject
    protected lateinit var findRssChannelByUrlUseCaseSync: FindRssChannelByUrlUseCaseSync
    @Inject
    protected lateinit var addNewRssChannelByRssUrlUseCaseSync: AddNewRssChannelByRssUrlUseCaseSync
    @Inject
    protected lateinit var getUserRssChannelUseCaseSync: GetUserRssChannelsUseCaseSync
    @Inject
    protected lateinit var getFeedsByRssChannelUseCaseSync: GetFeedsByRssChannelUseCaseSync
    @Inject
    protected lateinit var applicationStateManager : ApplicationStateManager

    private lateinit var btnTestSerivce: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_splash)


        CoroutineScope(BackgroundDispatcher).launch {
            withContext(Dispatchers.Main.immediate) {
                delay(1500)
                if (applicationStateManager.user == null) {
                    LoginActivity.start(this@SplashActivity)
                } else {
                    Toast.makeText(this@SplashActivity, "Welcome", Toast.LENGTH_LONG).show()
                    if (applicationStateManager.selectedChannelId == null) {
                        Log.w("SplashActivity", "The default selected channel is null")
                        val getChannelResult = getUserRssChannelUseCaseSync.executes()
                        if (getChannelResult is GetUserRssChannelsUseCaseSync.Result.Success) {
                            if (getChannelResult.channels.isNotEmpty()) {
                                applicationStateManager.selectedChannelId = getChannelResult.channels.elementAt(0).id
                            }
                        } else {
                            Log.e("SplashActivity", "Error happen when initial the default channel, ${getChannelResult.javaClass.simpleName}")
                        }
                    }
                    MainActivity.start(this@SplashActivity)
                }
                finish()
            }
        }

        btnTestSerivce = findViewById(R.id.btnTestService)
        btnTestSerivce.setOnClickListener {
            Log.d("BtnTestService", "Clicked")
            val doWorkerRequest = OneTimeWorkRequestBuilder<RssTrackingWorker>().build()
            WorkManager.getInstance(applicationContext)
                .enqueue(doWorkerRequest)
        }
    }



}