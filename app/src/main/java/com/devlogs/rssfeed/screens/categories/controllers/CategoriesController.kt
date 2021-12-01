package com.devlogs.rssfeed.screens.categories.controllers

import com.devlogs.rssfeed.categories.GetFeedCategoriesUseCaseSync
import com.devlogs.rssfeed.categories.GetUserCategoriesUseCaseSync
import javax.inject.Inject

class CategoriesController @Inject constructor(private val getFeedCategoriesUseCaseSync: GetFeedCategoriesUseCaseSync,
                                               private val getUserCategoriesUseCaseSync: GetUserCategoriesUseCaseSync
) {

}

