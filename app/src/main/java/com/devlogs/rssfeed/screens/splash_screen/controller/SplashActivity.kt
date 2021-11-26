package com.devlogs.rssfeed.screens.splash_screen.controller

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.devlogs.rssfeed.R
import com.devlogs.rssfeed.authentication.ValidateLoginUseCaseSync
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
    @Inject
    protected lateinit var validateLoginUseCaseSync: ValidateLoginUseCaseSync

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_splash)
        CoroutineScope(BackgroundDispatcher).launch {
            withContext(Dispatchers.Main.immediate) {
                delay(3000)
                if (validateLoginUseCaseSync.executes() is ValidateLoginUseCaseSync.Result.InValid) {
                    LoginActivity.start(this@SplashActivity)
                    finish()
                } else {
                    Toast.makeText(this@SplashActivity, "Welcome", Toast.LENGTH_LONG).show()
                }

            }
        }

    }
}