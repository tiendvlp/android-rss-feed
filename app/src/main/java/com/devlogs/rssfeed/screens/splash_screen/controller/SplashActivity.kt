package com.devlogs.rssfeed.screens.splash_screen.controller

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.devlogs.rssfeed.R
import com.devlogs.rssfeed.common.background_dispatcher.BackgroundDispatcher
import com.devlogs.rssfeed.common.shared_context.AppConfig.SharedPreferencesKey.USER_EMAIL
import com.devlogs.rssfeed.common.shared_context.AppConfig.SharedPreferencesKey.USER_NAME
import com.devlogs.rssfeed.screens.login.controller.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject
    protected lateinit var sharedPreferences: SharedPreferences;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_splash)
        CoroutineScope(BackgroundDispatcher).launch {
            delay(3000)
            withContext(Dispatchers.Main.immediate) {
                    LoginActivity.start(this@SplashActivity)
                    finish()
            }
        }
        // if contains user data

    }
}