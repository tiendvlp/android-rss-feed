package com.devlogs.rssfeed.rss_parser

class RssFeed(
    var title: String,
    var pubDate: String,
    var link: String,
    var guid: String,
    var author: String,
    var thumbnail: String,
    var description: String,
    var content: String,
    var categories: List<String>
)