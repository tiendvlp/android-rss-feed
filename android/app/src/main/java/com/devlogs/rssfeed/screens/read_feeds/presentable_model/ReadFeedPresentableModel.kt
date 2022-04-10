package com.devlogs.rssfeed.screens.read_feeds.presentable_model

import java.io.Serializable
import java.util.*

data class ReadFeedPresentableModel  (val feeds : TreeSet<FeedPresentableModel>, val channelPresentableModel: RssChannelPresentableModel, val avatarUrl: String, var currentScrollPos: Int = 0) : Serializable