package com.devlogs.rssfeed.authentication

import android.content.SharedPreferences
import com.devlogs.rssfeed.common.shared_context.AppConfig.SharedPreferencesKey.LOGIN_EXPIRED_TIME
import com.devlogs.rssfeed.common.shared_context.AppConfig.SharedPreferencesKey.USER_AVATAR
import com.devlogs.rssfeed.common.shared_context.AppConfig.SharedPreferencesKey.USER_EMAIL
import com.devlogs.rssfeed.common.shared_context.AppConfig.SharedPreferencesKey.USER_NAME
import javax.inject.Inject

class ValidateLoginUseCaseSync @Inject constructor(private val sharedPreferences: SharedPreferences) {
    sealed class Result {
        class Valid () : Result ()
        class InValid () : Result ()
    }

    fun executes (): Result {
        val expiredTime = sharedPreferences.getLong(LOGIN_EXPIRED_TIME, 1);
        if (expiredTime > System.currentTimeMillis()) {
            if (sharedPreferences.contains(USER_EMAIL)
                && sharedPreferences.contains(USER_AVATAR)
                && sharedPreferences.contains(USER_NAME)) {
                return Result.Valid();
            }
        }
        return Result.InValid()
    }
}
