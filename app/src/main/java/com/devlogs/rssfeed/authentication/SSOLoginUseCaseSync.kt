package com.devlogs.rssfeed.authentication

import android.content.SharedPreferences
import com.devlogs.rssfeed.common.background_dispatcher.BackgroundDispatcher
import com.devlogs.rssfeed.common.shared_context.AppConfig
import com.devlogs.rssfeed.domain.entities.UserEntity
import com.devlogs.rssfeed.users.AddUserUseCaseSync
import kotlinx.coroutines.withContext
import java.lang.RuntimeException
import javax.inject.Inject

class SSOLoginUseCaseSync @Inject constructor(
    private val loginRule: LoginRule,
    private val sharedPreferences: SharedPreferences,
    private val addUserUseCaseSync: AddUserUseCaseSync,
) {

    sealed class Result {
        data class Success (val user: UserEntity) : Result ()
        data class GeneralError (val message: String?) : Result ()
    }

    suspend fun executes (email: String, name: String, avatarUrl: String?) : Result = withContext(BackgroundDispatcher){
        val addUserResult = addUserUseCaseSync.executes(email, name, avatarUrl)
        if (addUserResult is AddUserUseCaseSync.Result.Success) {
            sharedPreferences.edit()
                .putString(AppConfig.SharedPreferencesKey.USER_EMAIL, email)
                .putString(AppConfig.SharedPreferencesKey.USER_NAME, name)
                .putString(AppConfig.SharedPreferencesKey.USER_AVATAR, avatarUrl)
                .putLong(AppConfig.SharedPreferencesKey.LOGIN_EXPIRED_TIME, System.currentTimeMillis() + loginRule.getValidTime())
                .apply()
            return@withContext Result.Success(addUserResult.addedUser)
        } else if (addUserResult is AddUserUseCaseSync.Result.GeneralError) {
            return@withContext Result.GeneralError(addUserResult.errorMessage)
        }

        throw RuntimeException("UnHandle AddUserUseCaseSync.Result")
    }

}