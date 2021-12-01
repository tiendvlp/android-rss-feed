package com.devlogs.rssfeed.domain.entities

data class CategoryEntity (val id: String, val title : String) : Comparable<CategoryEntity> {


    override fun compareTo(other: CategoryEntity): Int {
        if (other == this) {
            return 0
        }

        if (other.id.equals(id)) {
            return 0
        }

        return title.compareTo(other.title)
    }

}
