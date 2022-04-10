package com.devlogs.rssfeed.screens.bottomsheet_categories.controller

import android.util.Log
import com.devlogs.rssfeed.categories.AddFeedToCategoryUseCaseSync
import com.devlogs.rssfeed.categories.CreateCategoryUseCaseSync
import com.devlogs.rssfeed.categories.GetFeedCategoriesUseCaseSync
import com.devlogs.rssfeed.categories.GetUserCategoriesUseCaseSync
import com.devlogs.rssfeed.screens.bottomsheet_categories.mvc_view.BottomSheetCategoriesMvcView
import com.devlogs.rssfeed.screens.bottomsheet_categories.presentable_model.CategoryPresentableModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class CategoriesController @Inject constructor(private val addFeedToCategoryUseCaseSync: AddFeedToCategoryUseCaseSync,
                                               private val createCategoryUseCaseSync: CreateCategoryUseCaseSync,
                                               private val getFeedCategoriesUseCaseSync: GetFeedCategoriesUseCaseSync,
                                               private val getUserCategoriesUseCaseSync: GetUserCategoriesUseCaseSync) : BottomSheetCategoriesMvcView.Listener {


    private val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main.immediate)
    private lateinit var mvcView : BottomSheetCategoriesMvcView
    private lateinit var selectedFeedId : String

    fun setMvcView (mvcView: BottomSheetCategoriesMvcView) {
        this.mvcView = mvcView
        mvcView.register(this)
    }

    fun getCategories () {
        mvcView.loading()
        Log.d("GetCategoriesController", "Runnn")
        coroutine.launch {
            val result = getUserCategoriesUseCaseSync.executes()
            if (result is GetUserCategoriesUseCaseSync.Result.Success) {
                if (result.categories.isEmpty()) {
                    mvcView.showEmptyNotification()
                } else {
                    val getFeedCategoryResult = getFeedCategoriesUseCaseSync.executes(selectedFeedId)
                    val pm = HashSet<CategoryPresentableModel> ()
                    if (getFeedCategoryResult is GetFeedCategoriesUseCaseSync.Result.Success) {
                        Log.d("GetCategoriesController", "Success")
                        result.categories.forEach {
                            pm.add(CategoryPresentableModel(it.title, getFeedCategoryResult.categories.find { e -> e.title.equals(it.title) } != null))
                        }
                    }
                    pm.forEach {
                        Log.d("GetCategoriesController", it.title)
                    }
                    mvcView.setCategories(pm)
                }
            } else {
                mvcView.toast("Internal server error")
            }
        }
    }

    fun setSelectedFeed(feedId: String) {
        selectedFeedId = feedId
    }

    override fun onBtnCreateClicked(title: String) {
        coroutine.launch {
            val result = createCategoryUseCaseSync.executes(title)
            if (result is CreateCategoryUseCaseSync.Result.UnAuthorized) {
                mvcView.toast("Internal error")
            }
            else if (result is CreateCategoryUseCaseSync.Result.Success) {
                mvcView.addNewCategories (CategoryPresentableModel(result.createdCategory.title, true))
                mvcView.toast("Success")
            }

            else if (result is CreateCategoryUseCaseSync.Result.AlreadyExist) {
                mvcView.toast("Your title already exist")
            }

            else if (result is CreateCategoryUseCaseSync.Result.GeneralError) {
                mvcView.toast("Internal error: ${result.javaClass.simpleName}")
            }
        }
    }

    override fun onBtnConfirmClicked(titles: Set<String>) {
        coroutine.launch {
            val result = addFeedToCategoryUseCaseSync.executes(titles, selectedFeedId)

            if (result is AddFeedToCategoryUseCaseSync.Result.Success) {
                mvcView.toast("Success")
            } else {
                mvcView.toast("Internal server error")
            }
        }
    }
}