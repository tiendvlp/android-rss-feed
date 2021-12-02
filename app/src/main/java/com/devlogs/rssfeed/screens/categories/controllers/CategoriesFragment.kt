package com.devlogs.rssfeed.screens.categories.controllers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.devlogs.rssfeed.screens.categories.mvc_view.CategoriesMvcView
import com.devlogs.rssfeed.screens.categories.mvc_view.getCategoriesFullMvcView
import com.devlogs.rssfeed.screens.categories.presentable_model.CategoryPresentableModel
import com.devlogs.rssfeed.screens.common.mvcview.MvcViewFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CategoriesFragment : Fragment(), CategoriesMvcView.Listener {

    companion object {
        fun newInstance () = CategoriesFragment()
    }


    @Inject
    protected lateinit var mvcViewFactory: MvcViewFactory
    @Inject
    protected lateinit var controller: CategoriesController
    private lateinit var mvcView : CategoriesMvcView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        mvcView.register(this)
    }

    override fun onStop() {
        super.onStop()
        mvcView.unRegister(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mvcView = mvcViewFactory.getCategoriesFullMvcView(container)
        controller.setMvcView(mvcView)
        controller.getCategories()
        return mvcView.getRootView()
    }

    override fun onItemClicked(category: CategoryPresentableModel) {
        Toast.makeText(context, "Selected: ${category.title}", Toast.LENGTH_LONG).show()
    }

}