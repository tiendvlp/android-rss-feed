package com.devlogs.rssfeed.screens.main

import com.devlogs.rssfeed.common.base.BaseObservable

class MainScreenInsiderObservable : BaseObservable<MainScreenInsiderListener>() {


    fun userSelectChannel (channelId: String) {
        getListener().forEach { insider ->
            insider.onUserSelectedChannel(channelId)
        }
    }


}