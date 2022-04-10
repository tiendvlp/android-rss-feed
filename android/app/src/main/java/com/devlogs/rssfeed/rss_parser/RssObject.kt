package com.devlogs.rssfeed.rss_parser

import com.google.gson.annotations.SerializedName


data class RSSObject (
    val status: String,
    @SerializedName(value="feed")
    val channel: RssChannel,
    @SerializedName(value="items")
    val feeds: List<RssFeed>
)