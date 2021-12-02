package com.devlogs.rssfeed.screens.categories.mvc_view

import android.view.ViewGroup
import com.devlogs.rssfeed.screens.categories.presentable_model.CategoryPresentableModel
import com.devlogs.rssfeed.screens.common.mvcview.MvcView
import com.devlogs.rssfeed.screens.common.mvcview.MvcViewFactory
import com.devlogs.rssfeed.screens.common.mvcview.ObservableMvcView

interface CategoriesMvcView : ObservableMvcView<CategoriesMvcView.Listener> {

    interface Listener {
        fun onItemClicked (category: CategoryPresentableModel)
    }

    fun setCategories (categories : Set<CategoryPresentableModel>)
    fun loading ()
    fun toast (message: String)
    fun showEmptyText()

}

fun MvcViewFactory.getCategoriesFullMvcView (viewGroup: ViewGroup?) : CategoriesMvcView = CategoriesMvcViewImp(uiToolkit, viewGroup)