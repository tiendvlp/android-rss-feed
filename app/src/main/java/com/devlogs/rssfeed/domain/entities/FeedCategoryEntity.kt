package com.devlogs.rssfeed.domain.entities

data class FeedCategoryEntity (val id: String, val title: String, val feeds: Array<String>) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass == other?.javaClass) return true

        other as FeedCategoryEntity

        if (id == other.id) return true

        return false
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + feeds.contentHashCode()
        return result
    }
}