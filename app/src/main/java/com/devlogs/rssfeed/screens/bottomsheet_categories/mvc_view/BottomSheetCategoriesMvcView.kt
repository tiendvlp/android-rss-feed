package com.devlogs.rssfeed.screens.bottomsheet_categories.mvc_view

import com.devlogs.rssfeed.screens.bottomsheet_categories.controller.CategoriesController
import com.devlogs.rssfeed.screens.bottomsheet_categories.presentable_model.CategoryPresentableModel
import com.devlogs.rssfeed.screens.common.mvcview.MvcViewFactory
import com.devlogs.rssfeed.screens.common.mvcview.ObservableMvcView

interface BottomSheetCategoriesMvcView : ObservableMvcView <BottomSheetCategoriesMvcView.Listener> {

    interface Listener {
        fun onBtnCreateClicked(title: String)
        fun onBtnConfirmClicked (titles: Set<String>)
    }

    fun setCategories (categories: Set<CategoryPresentableModel>)
    fun addNewCategories (newCategory: CategoryPresentableModel)
    fun toast (message: String )
    fun loading ()
    fun showEmptyNotification()
}

fun MvcViewFactory.getBottomSheetCategoriesMvcView (controller: CategoriesController) : BottomSheetCategoriesMvcView = BottomSheetCategoriesMvcViewImp(uiToolkit, controller)