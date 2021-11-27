package com.devlogs.rssfeed.screens.splash_screen.controller

import android.content.ComponentName
import android.content.ServiceConnection
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.devlogs.rssfeed.R
import com.devlogs.rssfeed.android_services.RssChannelTrackingService
import com.devlogs.rssfeed.authentication.ValidateLoginUseCaseSync
import com.devlogs.rssfeed.common.background_dispatcher.BackgroundDispatcher
import com.devlogs.rssfeed.common.shared_context.AppConfig.SharedPreferencesKey.USER_EMAIL
import com.devlogs.rssfeed.common.shared_context.AppConfig.SharedPreferencesKey.USER_NAME
import com.devlogs.rssfeed.domain.entities.FeedEntity
import com.devlogs.rssfeed.rss_channels.AddNewRssChannelByRssUrlUseCaseSync
import com.devlogs.rssfeed.rss_channels.FindRssChannelByUrlUseCaseSync
import com.devlogs.rssfeed.rss_channels.GetUserRssChannelsUseCaseSync
import com.devlogs.rssfeed.screens.login.controller.LoginActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity(), ServiceConnection, RssChannelTrackingService.Listener {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_splash)

        RssChannelTrackingService.bind(this, this)

        CoroutineScope(BackgroundDispatcher).launch {
            withContext(Dispatchers.Main.immediate) {
//                val result = addNewRssChannelByRssUrlUseCaseSync.executes("https://vatvostudio.vn/feed/")
//                Log.d("Add Rss result", result.javaClass.simpleName)
                if (validateLoginUseCaseSync.executes() is ValidateLoginUseCaseSync.Result.InValid) {
                    LoginActivity.start(this@SplashActivity)
                    finish()
                } else {
                    Toast.makeText(this@SplashActivity, "Welcome", Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as? RssChannelTrackingService.LocalBinder
        binder?.service?.register("https:\\\\vatvostudio.vn\\feed", this)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        TODO("Not yet implemented")
    }

    override fun onNewFeed(feed: FeedEntity) {
        Log.d("SplashActivity", feed.id)
    }
}