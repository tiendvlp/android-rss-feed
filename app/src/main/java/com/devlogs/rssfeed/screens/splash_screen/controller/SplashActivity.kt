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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_splash)

        val htmlTextView = findViewById<HtmlTextView>(R.id.img)
        htmlTextView.setHtml(
            "<img src=\"https://vcdn1-giaitri.vnecdn.net/2021/11/27/erik-1638029251-3302-1638029325.jpg?w=1200&amp;h=0&amp;q=100&amp;dpr=1&amp;fit=crop&amp;s=uMkWDNBA9tz_o-OAoB1ovw\">"
            , HtmlHttpImageGetter(htmlTextView, "", true)
        )
        CoroutineScope(BackgroundDispatcher).launch {

            withContext(Dispatchers.Main.immediate) {
//                val gResult = getFeedsByRssChannelUseCaseSync.executes(UrlEncrypt.encode("https://vnexpress.net/rss/tin-noi-bat.rss"), System.currentTimeMillis(), 20) as GetFeedsByRssChannelUseCaseSync.Result.Success

//                gResult.rssChannel.forEach {
//                    Log.d("SplashActivity", "GetFeed: " + it.title)
//                }
//
                addNewRssChannelByRssUrlUseCaseSync.executes("https://vnexpress.net/rss/tin-noi-bat.rss")
                val result = addNewRssChannelByRssUrlUseCaseSync.executes("https://vatvostudio.vn/feed")
                RssChannelTrackingService.bind(this@SplashActivity, this@SplashActivity)
                Log.d("Add Rss result", result.javaClass.simpleName)
                if (validateLoginUseCaseSync.executes() is ValidateLoginUseCaseSync.Result.InValid) {
                    LoginActivity.start(this@SplashActivity)
                } else {
//                    Toast.makeText(this@SplashActivity, "Welcome", Toast.LENGTH_LONG).show()
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