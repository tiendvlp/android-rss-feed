package com.devlogs.rssfeed.screens.ReadFeeds.mvc_view

import com.devlogs.rssfeed.domain.entities.FeedEntity
import com.devlogs.rssfeed.domain.entities.RssChannelEntity
import com.devlogs.rssfeed.screens.ReadFeeds.presentable_model.FeedPresentableModel
import com.devlogs.rssfeed.screens.ReadFeeds.presentable_model.RssChannelPresentableModel
import com.devlogs.rssfeed.screens.common.mvcview.ObservableMvcView
import java.util.*

interface ReadFeedsMvcView : ObservableMvcView<ReadFeedsMvcView.Listener> {
    interface Listener {

    }

    fun setUserAvatarUrl (url:String)
    fun setChannels (channel: RssChannelPresentableModel)
    fun appendFeeds (feeds: TreeSet<FeedPresentableModel>)
    fun addNewFeeds (feeds: TreeSet<FeedPresentableModel>)
}