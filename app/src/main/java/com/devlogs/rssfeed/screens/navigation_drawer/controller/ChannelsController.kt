package com.devlogs.rssfeed.screens.navigation_drawer.controller

import com.devlogs.rssfeed.rss_channels.GetUserRssChannelsUseCaseSync
import com.devlogs.rssfeed.screens.navigation_drawer.mvc_view.MainNavMvcView
import com.devlogs.rssfeed.screens.navigation_drawer.presentable_model.ChannelPresentableModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChannelsController @Inject constructor(private val getUserRssChannelsUseCaseSync: GetUserRssChannelsUseCaseSync) {

    private val coroutine = CoroutineScope(Dispatchers.Main.immediate)
    private lateinit var mvcView: MainNavMvcView

    fun setMvcView (mvcView: MainNavMvcView) {
        this.mvcView = mvcView
    }

    fun getChannels () {
        mvcView.loading()
        coroutine.launch {
           val result = getUserRssChannelsUseCaseSync.executes()

           if (result is GetUserRssChannelsUseCaseSync.Result.Success) {
                mvcView.setChannels(result.channels.map { ChannelPresentableModel(it.id, it.imageUrl, it.title) })
           }
        }
    }

}