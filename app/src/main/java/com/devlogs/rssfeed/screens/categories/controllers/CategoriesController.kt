package com.devlogs.rssfeed.screens.categories.controllers

import android.util.Log
import com.devlogs.rssfeed.categories.GetUserCategoriesUseCaseSync
import com.devlogs.rssfeed.feeds.GetFeedsByCategoryUseCaseSync
import com.devlogs.rssfeed.screens.categories.mvc_view.CategoriesMvcView
import com.devlogs.rssfeed.screens.categories.presentable_model.CategoryPresentableModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class CategoriesController @Inject constructor(private val getFeedsByCategoryUseCaseSync: GetFeedsByCategoryUseCaseSync,
                                               private val getUserCategoriesUseCaseSync: GetUserCategoriesUseCaseSync
) {

    private lateinit var mvcView : CategoriesMvcView
    private val coroutine = CoroutineScope(Dispatchers.Main.immediate)

    fun setMvcView (mvcView: CategoriesMvcView) {
        this.mvcView = mvcView
    }

    fun getCategories () {
        mvcView.loading()
        coroutine.launch {
            Log.d("CategoriesController", "getCategories")
            val getCategoriesResult = getUserCategoriesUseCaseSync.executes()
            Log.d("CategoriesController", getCategoriesResult.javaClass.canonicalName)

            if (getCategoriesResult is GetUserCategoriesUseCaseSync.Result.GeneralError) {
                mvcView.toast("Internal server error + ${getCategoriesResult.errorMessage}")
            }

            if (getCategoriesResult is GetUserCategoriesUseCaseSync.Result.UnAuthorized) {
                mvcView.toast("Internal server error")
            }

            if (getCategoriesResult is GetUserCategoriesUseCaseSync.Result.Success) {
                val categories = HashSet<CategoryPresentableModel> ()
                if (getCategoriesResult.categories.isEmpty()) {
                    mvcView.showEmptyText()
                }
                getCategoriesResult.categories.forEach { category ->
                    val getFirst3FeedsResult = getFeedsByCategoryUseCaseSync.executes(category.title, 4)
                    Log.d("CategoriesController", getFirst3FeedsResult.javaClass.canonicalName)

                    if (getFirst3FeedsResult is GetFeedsByCategoryUseCaseSync.Result.UnAuthorized) {
                        mvcView.toast("Internal server error")
                    }

                    if (getFirst3FeedsResult is GetFeedsByCategoryUseCaseSync.Result.GeneralError) {
                        mvcView.toast("Internal server error: ${getFirst3FeedsResult.message}")
                    }

                    if (getFirst3FeedsResult is GetFeedsByCategoryUseCaseSync.Result.Success) {
                        val imgs = getFirst3FeedsResult.feeds.map {
                            Log.d("ImageUrlVaTitle:", "${it.id} co imageUrl la: ${it.imageUrl}")
                           return@map it.imageUrl
                        }
                        Log.d("CategoriesController", "Success with ${imgs.size}")
                        categories.add(CategoryPresentableModel(
                            category.title,
                            imgs
                        ))
                    }
                }
                mvcView.setCategories(categories)
            }
        }

    }

}

