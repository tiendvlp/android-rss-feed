package com.devlogs.rssfeed.screens.bottomsheet_categories.mvc_view

import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devlogs.rssfeed.R
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

    constructor(uiToolkit: UIToolkit) {
        this.uiToolkit = uiToolkit
        setRootView(uiToolkit.layoutInflater.inflate(R.layout.layout_bottomsheet_select_categories, null, false))
        addControls()
        addEvents()
    }

    private fun addControls() {
        edtCategoryName = findViewById(R.id.edtCategoryName)
        btnCreate = findViewById(R.id.btnCreate)
        progressBar = findViewById(R.id.progressBar)
        txtEmpty = findViewById(R.id.txtEmpty)
        lvCategories = findViewById(R.id.lvCategories)
    }

    private fun addEvents() {
        btnCreate.setOnClickListener {
            getListener().forEach { listener ->
                listener.onBtnCreateClicked (edtCategoryName.text.toString())
            }
        }
    }


    override fun setCategories(categories: Set<CategoryPresentableModel>) {
        TODO("Not yet implemented")
    }
}