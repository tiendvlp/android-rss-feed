package com.devlogs.rssfeed.screens.bottomsheet_categories.mvc_view

import com.devlogs.rssfeed.screens.bottomsheet_categories.presentable_model.CategoryPresentableModel
import com.devlogs.rssfeed.screens.common.mvcview.ObservableMvcView

interface BottomSheetCategoriesMvcView : ObservableMvcView <BottomSheetCategoriesMvcView.Listener> {

    interface Listener {
        fun onBtnCreateClicked(title: String)

    }

    fun setCategories (categories: Set<CategoryPresentableModel>)
}