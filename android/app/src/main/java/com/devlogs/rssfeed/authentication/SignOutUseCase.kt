package com.devlogs.rssfeed.authentication

import android.content.SharedPreferences
import com.devlogs.rssfeed.common.background_dispatcher.BackgroundDispatcher
import com.devlogs.rssfeed.common.shared_context.AppConfig.SharedPreferencesKey
import com.devlogs.rssfeed.receive_channel_update.UnSubscribeFollowedChannelsNotificationUseCaseSync
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val unSubscribeFollowedChannelsNotificationUseCaseSync: UnSubscribeFollowedChannelsNotificationUseCaseSync,
    private val sharedPreferences: SharedPreferences, private val fireStore: FirebaseFirestore){

    suspend fun executes () = withContext(BackgroundDispatcher) {
        unSubscribeFollowedChannelsNotificationUseCaseSync.executes()
        sharedPreferences.edit().remove(SharedPreferencesKey.SELECTED_CHANNEL)
        .remove(SharedPreferencesKey.USER_NAME)
        .remove(SharedPreferencesKey.USER_ID)
        .remove(SharedPreferencesKey.USER_AVATAR)
        .remove(SharedPreferencesKey.USER_EMAIL)
        .remove(SharedPreferencesKey.LOGIN_EXPIRED_TIME)
            .commit()
        fireStore.clearPersistence()
    }

}