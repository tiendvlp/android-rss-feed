package com.devlogs.rssfeed.authentication

import android.content.SharedPreferences
import com.devlogs.rssfeed.common.shared_context.AppConfig.SharedPreferencesKey
import javax.inject.Inject

class SignOutUseCase @Inject constructor(private val sharedPreferences: SharedPreferences){

    fun executes () {
        sharedPreferences.edit().remove(SharedPreferencesKey.SELECTED_CHANNEL)
        .remove(SharedPreferencesKey.USER_NAME)
        .remove(SharedPreferencesKey.USER_ID)
        .remove(SharedPreferencesKey.USER_AVATAR)
        .remove(SharedPreferencesKey.USER_EMAIL)
        .remove(SharedPreferencesKey.LOGIN_EXPIRED_TIME)
            .commit()
    }

}