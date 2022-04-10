package com.devlogs.rssfeed.screens.bottomsheet_categories.presentable_model


data class CategoryPresentableModel (val title : String, val added: Boolean) : Comparable<CategoryPresentableModel> {

    override fun compareTo(other: CategoryPresentableModel): Int {
        if (other == this) {
            return 0
        }

        if (other.title.equals(title)) {
            return 0
        }

        return 1
    }

}
