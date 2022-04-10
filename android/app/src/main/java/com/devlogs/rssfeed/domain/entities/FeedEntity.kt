package com.devlogs.rssfeed.domain.entities

data class FeedEntity
                (
                val id: String,
                val rssChannelId: String,
                val channelTitle: String,
                val title: String,
                val description: String,
                val pubDate: Long,
                val url: String,
                val author: String,
                val content: String,
                val imageUrl: String?, ) : Comparable<FeedEntity> {


    override fun compareTo(other: FeedEntity): Int {
        if (other.id.equals(id)) {
            return 0
        }

        if (other.pubDate == pubDate) {
            return title.compareTo(other.title)
        }

        if (other.pubDate < pubDate) {
            return -1
        }
        return 1
    }
}