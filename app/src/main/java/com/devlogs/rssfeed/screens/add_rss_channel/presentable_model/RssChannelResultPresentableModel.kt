package com.devlogs.rssfeed.screens.add_rss_channel.presentable_model

import java.io.Serializable

data class RssChannelResultPresentableModel (
            val id: String? = null,
            val url: String,
            val rssUrl: String,
            val title: String,
            val imageUrl: String,
            val isAdded: Boolean
        ) : Serializable