package com.devlogs.rssfeed.screens.categories.mvc_view

import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.devlogs.rssfeed.R
import com.devlogs.rssfeed.screens.categories.presentable_model.CategoryPresentableModel
import com.devlogs.rssfeed.screens.common.mvcview.BaseMvcView
import com.devlogs.rssfeed.screens.common.mvcview.UIToolkit

class CategoriesMvcViewImp : BaseMvcView<CategoriesMvcView.Listener>, CategoriesMvcView {

    private val uiToolkit: UIToolkit
    private lateinit var lvCategories : RecyclerView
    private lateinit var toolbar : Toolbar
    private lateinit var progressBar : ProgressBar

    private val categories : HashSet<CategoryPresentableModel> = HashSet()

    constructor(uiToolkit: UIToolkit, viewGroup: ViewGroup) {
        this.uiToolkit = uiToolkit
        setRootView(uiToolkit.layoutInflater.inflate(R.layout.item_categories_full, viewGroup, false))

        addControls ()
        addEvents ()
        setupToolbar()
    }

    private fun setupToolbar() {
        val layoutToolbar = uiToolkit.layoutInflater.inflate(R.layout.layout_title_toolbar, toolbar, false)
        toolbar.addView(layoutToolbar)
        layoutToolbar.findViewById<TextView>(R.id.title).text = "Categories"
    }

    private fun addControls() {
        lvCategories = findViewById(R.id.lvCategories)
        toolbar = findViewById(R.id.toolbar)
        progressBar = findViewById(R.id.progressBar)

    }

    private fun addEvents() {

    }


    override fun setCategories(categories: Set<CategoryPresentableModel>) {
        progressBar.visibility = View.GONE
        lvCategories.visibility = View.VISIBLE

        this.categories.clear()
        this.categories.addAll(categories)

    }

    override fun loading() {
        progressBar.visibility = View.VISIBLE
        lvCategories.visibility = View.GONE
    }

    override fun toast(message: String) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show()
    }
}