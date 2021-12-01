package com.devlogs.rssfeed.screens.bottomsheet_categories.mvc_view

import android.util.Log
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devlogs.rssfeed.R
import com.devlogs.rssfeed.screens.bottomsheet_categories.controller.CategoriesController
import com.devlogs.rssfeed.screens.bottomsheet_categories.controller.CategoriesRcvAdapter
import com.devlogs.rssfeed.screens.bottomsheet_categories.presentable_model.CategoryPresentableModel
import com.devlogs.rssfeed.screens.common.mvcview.BaseMvcView
import com.devlogs.rssfeed.screens.common.mvcview.UIToolkit

class BottomSheetCategoriesMvcViewImp : BaseMvcView<BottomSheetCategoriesMvcView.Listener>, BottomSheetCategoriesMvcView {

    private val uiToolkit: UIToolkit
    private lateinit var edtCategoryName: EditText
    private lateinit var btnCreate:  Button
    private lateinit var progressBar: ProgressBar
    private lateinit var  txtEmpty : TextView
    private lateinit var lvCategories : RecyclerView
    private lateinit var btnConfirm : Button
    private val categories = HashSet<CategoryPresentableModel>()
    private lateinit var categoriesAdapter: CategoriesRcvAdapter
    private val controller: CategoriesController

    constructor(uiToolkit: UIToolkit, controller: CategoriesController) {
        this.controller = controller
        this.uiToolkit = uiToolkit
        setRootView(uiToolkit.layoutInflater.inflate(R.layout.layout_bottomsheet_select_categories, null, false))
        addControls()
        addEvents()
        controller.setMvcView(this)
        controller.getCategories()
    }

    private fun addControls() {
        edtCategoryName = findViewById(R.id.edtCategoryName)
        btnCreate = findViewById(R.id.btnCreate)
        btnConfirm = findViewById(R.id.btnConfirm)
        progressBar = findViewById(R.id.progressBar)
        txtEmpty = findViewById(R.id.txtEmpty)
        lvCategories = findViewById(R.id.lvCategories)
        lvCategories.layoutManager = LinearLayoutManager(getContext())
        categoriesAdapter = CategoriesRcvAdapter(categories)
        lvCategories.adapter = categoriesAdapter
    }

    private fun addEvents() {
        btnCreate.setOnClickListener {
            getListener().forEach { listener ->
                listener.onBtnCreateClicked (edtCategoryName.text.toString())
            }
        }

        btnConfirm.setOnClickListener {
            getListener().forEach { listener ->
                listener.onBtnConfirmClicked( categoriesAdapter.checkedCategories
                    .map { it.title }.toSet())
            }
        }
    }


    override fun setCategories(categories: Set<CategoryPresentableModel>) {
        this.categories.clear()
        this.categories.addAll(categories)
        Log.d("CategoriesMvcViewImp", "Show: " + categories.size)
        categoriesAdapter.notifyDataSetChanged()
    }

    override fun addNewCategories(newCategory: CategoryPresentableModel) {
        this.categories.add(newCategory)
        categoriesAdapter.notifyDataSetChanged()
    }

    override fun toast(message: String) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun loading() {
        txtEmpty.visibility = View.GONE
        lvCategories.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
    }

    override fun showEmptyNotification() {
        txtEmpty.visibility = View.VISIBLE
        lvCategories.visibility = View.GONE
        progressBar.visibility = View.GONE
    }
}