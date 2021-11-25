package com.devlogs.rssfeed.domain.entities

data class FeedEntity (
                val rssChannelUrl: String,
                val channelTitle: String,
                val title: String,
                val description: String,
                val pubDate: Long,
                val url: String,
                val author: String,
                val content: String, )