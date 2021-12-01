package com.devlogs.rssfeed.screens.bottomsheet_categories

import android.content.Context
import com.devlogs.rssfeed.screens.bottomsheet_categories.controller.CategoriesController
import com.devlogs.rssfeed.screens.bottomsheet_categories.mvc_view.BottomSheetCategoriesMvcView
import com.devlogs.rssfeed.screens.bottomsheet_categories.mvc_view.getBottomSheetCategoriesMvcView
import com.devlogs.rssfeed.screens.common.mvcview.MvcViewFactory
import com.google.android.material.bottomsheet.BottomSheetDialog
import javax.inject.Inject

class CategoriesBottomSheet @Inject constructor(private val mvcViewFactory: MvcViewFactory,
                                                private val controller: CategoriesController) {

    private lateinit var bottomSheet : BottomSheetDialog
    private val mvcView : BottomSheetCategoriesMvcView = mvcViewFactory.getBottomSheetCategoriesMvcView(controller)

    fun show (context: Context) {
        if (!::bottomSheet.isInitialized) {
            bottomSheet = BottomSheetDialog(context)
            bottomSheet.setContentView(mvcView.getRootView())
        }

        bottomSheet.show()
    }

    fun dismiss () {
        bottomSheet.dismissWithAnimation = true
        bottomSheet.dismiss()
    }

    fun setSelectedFeedId (feedId: String) {
        controller.setSelectedFeed(feedId)
    }

}