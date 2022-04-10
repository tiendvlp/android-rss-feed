package com.devlogs.rssfeed.common.shared_context

object AppConfig {
    object SharedPreferencesKey {
        val USER_NAME = "USER_NAME"
        val USER_EMAIL = "USER_EMAIL"
        val USER_ID = "USER_ID"
        val USER_AVATAR = "USER_AVATAR"
        val LOGIN_EXPIRED_TIME = "LOGIN_EXPIRED_TIME"
        val SELECTED_CHANNEL = "SELECTED_CHANNEL"
    }

    object DaggerNamed {
        const val FRAGMENT_SCOPE = "FragmentScope"
        const val ACTIVITY_SCOPE = "ActivityScope"
    }
}