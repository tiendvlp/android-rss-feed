package com.devlogs.rssfeed.screens.login.controller

import com.devlogs.rssfeed.authentication.SSOLoginUseCaseSync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.devlogs.rssfeed.common.base.Observable
import com.devlogs.rssfeed.common.helper.LogTarget
import com.devlogs.rssfeed.common.helper.normalLog
import com.devlogs.rssfeed.receive_channel_update.SubscribeFollowedChannelsNotificationUseCaseSync

class LoginController @Inject constructor(
    private val loginUseCaseSync: SSOLoginUseCaseSync) : Observable<LoginController.Listener>, LogTarget {
    interface Listener {
        fun loginSuccess ()
        fun loginFailed (errorMessage: String)
    }
    private val coroutine = CoroutineScope(Dispatchers.Main.immediate)
    private var listener : Listener? = null


    fun login(email: String, name: String, avatarUrl: String?) {
        coroutine.launch {
            val loginResult = loginUseCaseSync.executes(email, name, avatarUrl)
            if (loginResult is SSOLoginUseCaseSync.Result.Success) {
                normalLog("Login success {avatar: ${loginResult.user.avatar}}")
                listener?.loginSuccess()
            } else if (loginResult is SSOLoginUseCaseSync.Result.GeneralError) {
                if (loginResult.message == null) {
                    listener?.loginFailed("Unknown error")
                } else {
                    listener?.loginFailed(loginResult.message)
                }
            }
        }
    }

    override fun unRegister(listener: Listener) {
        if (this.listener == listener) {
            this.listener = null
        }
    }

    override fun register(listener: Listener) {
        this.listener = listener
    }

}