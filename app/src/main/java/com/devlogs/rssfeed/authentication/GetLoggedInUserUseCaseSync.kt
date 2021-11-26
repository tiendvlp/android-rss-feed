package com.devlogs.rssfeed.authentication

import android.content.SharedPreferences
import com.devlogs.rssfeed.common.shared_context.AppConfig
import com.devlogs.rssfeed.common.shared_context.AppConfig.SharedPreferencesKey.USER_AVATAR
import com.devlogs.rssfeed.common.shared_context.AppConfig.SharedPreferencesKey.USER_EMAIL
import com.devlogs.rssfeed.common.shared_context.AppConfig.SharedPreferencesKey.USER_NAME
import com.devlogs.rssfeed.domain.entities.UserEntity
import javax.inject.Inject

class GetLoggedInUserUseCaseSync @Inject constructor(private val sharedPreferences: SharedPreferences) {
        sealed class Result {
            data class Success(val user: UserEntity) : Result()
            class InValidLogin() : Result()
        }

        fun executes(): Result {
            val expiredTime =
                sharedPreferences.getLong(AppConfig.SharedPreferencesKey.LOGIN_EXPIRED_TIME, 1);
            if (expiredTime > System.currentTimeMillis()) {
                if (sharedPreferences.contains(USER_EMAIL)
                    && sharedPreferences.contains(USER_AVATAR)
                    && sharedPreferences.contains(USER_NAME)
                ) {
                   val email = sharedPreferences.getString(USER_EMAIL, "")
                    val avatar = sharedPreferences.getString(USER_AVATAR, "")
                    val name = sharedPreferences.getString(USER_NAME, "")

                    if (email!!.isNotBlank() || avatar!!.isNotBlank() || name!!.isNotBlank()) {
                        return Result.Success(UserEntity(email, name!!, avatar!!))
                    }
                }
            }
            return Result.InValidLogin()
    }
}
