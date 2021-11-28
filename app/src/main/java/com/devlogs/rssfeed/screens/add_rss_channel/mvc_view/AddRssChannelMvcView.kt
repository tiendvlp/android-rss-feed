package com.devlogs.rssfeed.screens.add_rss_channel.mvc_view

import android.view.ViewGroup
import com.devlogs.rssfeed.screens.add_rss_channel.presentable_model.RssChannelResultPresentableModel
import com.devlogs.rssfeed.screens.common.mvcview.MvcViewFactory
import com.devlogs.rssfeed.screens.common.mvcview.ObservableMvcView

interface AddRssChannelMvcView : ObservableMvcView<AddRssChannelMvcView.Listener> {
    interface Listener {
        fun onBtnSearchClicked(url: String)
        fun onBtnAddClicked(rssChannelUrl: String)

    }

    fun showResult(channel: RssChannelResultPresentableModel)
    fun loading ()
    fun error (errorMessage: String)
    fun emptyResult ()
    fun clearResult ()
}

fun MvcViewFactory.getAddRssChannelMvcView (viewGroup: ViewGroup?) : AddRssChannelMvcView = AddRssChannelMvcViewImp(uiToolkit, viewGroup)