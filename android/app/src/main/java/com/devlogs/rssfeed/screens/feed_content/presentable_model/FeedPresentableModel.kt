package com.devlogs.rssfeed.screens.feed_content.presentable_model

data class FeedPresentableModel (
                                 val feedUrl: String,
                                 val channelTitle: String,
                                 val feedTitle: String,
                                 val feedContent: String,
                                 val author: String,
                                 val pubDate: String, ) {
}