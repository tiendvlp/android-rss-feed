package com.devlogs.rssfeed.application

import android.content.SharedPreferences
import com.devlogs.rssfeed.authentication.GetLoggedInUserUseCaseSync
import com.devlogs.rssfeed.common.shared_context.AppConfig.SharedPreferencesKey.SELECTED_CHANNEL
import com.devlogs.rssfeed.domain.entities.UserEntity
import javax.inject.Inject

class ApplicationStateManager @Inject constructor(private val getLoggedInUserUseCaseSync: GetLoggedInUserUseCaseSync, private val sharedPreferences: SharedPreferences){

    val user : UserEntity?; get() {
        val result = getLoggedInUserUseCaseSync.executes()

        if (result is GetLoggedInUserUseCaseSync.Result.Success) {
            return result.user
        }
        return null
    }

    var selectedChannelId : String?; get() {
        return sharedPreferences.getString(SELECTED_CHANNEL, null)
    }
    set(value) {
        sharedPreferences.edit().putString(SELECTED_CHANNEL, value).commit()
    }

}