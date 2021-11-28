package com.devlogs.rssfeed.screens.splash_screen.controller

import android.content.ComponentName
import android.content.ServiceConnection
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.text.Html
import android.util.Log
import android.webkit.WebView
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.devlogs.rssfeed.R
import com.devlogs.rssfeed.android_services.RssChannelTrackingService
import com.devlogs.rssfeed.application.ApplicationStateManager
import com.devlogs.rssfeed.authentication.ValidateLoginUseCaseSync
import com.devlogs.rssfeed.common.background_dispatcher.BackgroundDispatcher
import com.devlogs.rssfeed.common.shared_context.AppConfig.SharedPreferencesKey.USER_EMAIL
import com.devlogs.rssfeed.common.shared_context.AppConfig.SharedPreferencesKey.USER_NAME
import com.devlogs.rssfeed.domain.entities.FeedEntity
import com.devlogs.rssfeed.encrypt.UrlEncrypt
import com.devlogs.rssfeed.feeds.GetFeedsByRssChannelUseCaseSync
import com.devlogs.rssfeed.rss_channels.AddNewRssChannelByRssUrlUseCaseSync
import com.devlogs.rssfeed.rss_channels.FindRssChannelByUrlUseCaseSync
import com.devlogs.rssfeed.rss_channels.GetUserRssChannelsUseCaseSync
import com.devlogs.rssfeed.screens.login.controller.LoginActivity
import com.devlogs.rssfeed.screens.main.MainActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter
import org.sufficientlysecure.htmltextview.HtmlResImageGetter
import org.sufficientlysecure.htmltextview.HtmlTextView
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
    @Inject
    protected lateinit var getFeedsByRssChannelUseCaseSync: GetFeedsByRssChannelUseCaseSync
    @Inject
    protected lateinit var applicationStateManager : ApplicationStateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_splash)

        CoroutineScope(BackgroundDispatcher).launch {
            withContext(Dispatchers.Main.immediate) {
                if (applicationStateManager.user == null) {
                    LoginActivity.start(this@SplashActivity)
                } else {
                    Toast.makeText(this@SplashActivity, "Welcome", Toast.LENGTH_LONG).show()
                    if (applicationStateManager.selectedChannelId == null) {
                        Log.w("SplashActivity", "The default selected channel is null")
                        val getChannelResult = getUserRssChannelUseCaseSync.executes()
                        if (getChannelResult is GetUserRssChannelsUseCaseSync.Result.Success) {
                            Log.d("SplashActivity", "The default selected channel set to ${getChannelResult.channels.elementAt(0).title}")
                            applicationStateManager.selectedChannelId = getChannelResult.channels.elementAt(0).id
                        } else {
                            Log.e("SplashActivity", "Error happen when initial the default channel, ${getChannelResult.javaClass.simpleName}")
                        }
                    }
                    MainActivity.start(this@SplashActivity)
                }
                finish()
            }
        }

    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as? RssChannelTrackingService.LocalBinder
        binder?.service?.registerAllChannel( this)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        TODO("Not yet implemented")
    }

    override fun onNewFeed(feed: FeedEntity) {
        Log.d("SplashActivity", feed.id)
    }
}