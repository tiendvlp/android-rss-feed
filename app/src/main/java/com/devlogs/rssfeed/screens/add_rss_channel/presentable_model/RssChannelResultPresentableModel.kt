package com.devlogs.rssfeed.screens.add_rss_channel.presentable_model

data class RssChannelResultPresentableModel (
            val id: String,
            val url: String,
            val rssUrl: String,
            val title: String,
            val imageUrl: String,
            val isAdded: Boolean
        )