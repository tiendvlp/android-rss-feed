package com.devlogs.rssfeed.screens.ReadFeeds.presentable_model

data class FeedPresentableModel
    ( val id: String,
      val rssChannelId: String,
      val channelTitle: String,
      val title: String,
      val pubDate:Long,
      val pubDateInString: String,
      val url: String,
      val author: String ) : Comparable<FeedPresentableModel> {

    override fun equals(other: Any?): Boolean {
        if (other == null) {return false}

        if (!other.javaClass.equals(javaClass)) {
            return false
        }

        return (other as FeedPresentableModel).id.equals(id)
    }

    override fun compareTo(other: FeedPresentableModel): Int {
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