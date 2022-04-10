package com.devlogs.rssfeed.screens.read_feeds.presentable_model

import java.io.Serializable

data class RssChannelPresentableModel (val id: String, val url: String, val rssUrl:String, val title: String, val isFollowed : Boolean) : Serializable