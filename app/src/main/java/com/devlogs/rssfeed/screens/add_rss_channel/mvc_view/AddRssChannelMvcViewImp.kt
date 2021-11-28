package com.devlogs.rssfeed.screens.add_rss_channel.mvc_view

import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.devlogs.rssfeed.R
import com.devlogs.rssfeed.screens.add_rss_channel.presentable_model.RssChannelResultPresentableModel
import com.devlogs.rssfeed.screens.common.mvcview.BaseMvcView
import com.devlogs.rssfeed.screens.common.mvcview.UIToolkit
import dagger.hilt.android.internal.Contexts.getApplication

class AddRssChannelMvcViewImp : BaseMvcView<AddRssChannelMvcView.Listener>, AddRssChannelMvcView{

    private val uiToolkit: UIToolkit

    private lateinit var toolbar: Toolbar
    private lateinit var layoutToolbar: View
    private lateinit var edtUrl : EditText
    private lateinit var btnSearch: Button
    private lateinit var btnAdd: Button
    private lateinit var layoutResult: ConstraintLayout
    private lateinit var imgWeb : ImageView
    private lateinit var txtWebTitle : TextView
    private lateinit var txtWebUrl :TextView
    private lateinit var txtTut : TextView
    private lateinit var txtEmptyResult : TextView
    private lateinit var txtError : TextView
    private lateinit var layoutLoading: LinearLayout

    constructor(uiToolkit: UIToolkit, viewGroup: ViewGroup?) {
        this.uiToolkit = uiToolkit
        setRootView(uiToolkit.layoutInflater.inflate(R.layout.layout_add_rss_channel,viewGroup, false))
        addControls()
        setupToolbar()
        addEvents()
    }

    private fun addControls() {
        btnAdd = findViewById(R.id.btnAdd)
        txtEmptyResult = findViewById(R.id.txtEmptyResult)
        toolbar = findViewById(R.id.toolbar)
        edtUrl = findViewById(R.id.edtUrl)
        btnSearch = findViewById(R.id.btnSearch)
        layoutResult = findViewById(R.id.layoutResult)
        imgWeb = findViewById(R.id.imgWeb)
        txtWebTitle = findViewById(R.id.txtWebTitle)
        txtWebUrl = findViewById(R.id.txtWebUrl)
        txtTut = findViewById(R.id.txtTut)
        txtError = findViewById(R.id.txtError)
        layoutLoading = findViewById(R.id.layoutLoading)
    }

    private fun setupToolbar () {
        layoutToolbar = uiToolkit.layoutInflater.inflate(R.layout.layout_title_toolbar, toolbar, false)
        toolbar.addView(layoutToolbar)
    }

    private fun addEvents() {
        btnSearch.setOnClickListener {
            getListener().forEach { listener ->
                listener.onBtnSearchClicked()
            }
        }
    }

    override fun showResult(channel: RssChannelResultPresentableModel) {
        txtTut.visibility = View.GONE
        txtEmptyResult.visibility = View.GONE
        layoutResult.visibility = View.VISIBLE
        txtWebTitle.text = channel.title
        txtWebUrl.text = channel.url

        if (channel.isAdded){
            btnAdd.setTextColor(getApplication(getContext()).getResources().getColor(R.color.gold))
            btnAdd.isEnabled = false
        } else {
            btnAdd.isEnabled = true
            btnAdd.setTextColor(getApplication(getContext()).getResources().getColor(R.color.white))
        }

    }

    override fun loading() {
        txtError.text = ""
        txtEmptyResult.visibility = View.GONE
        layoutResult.visibility = View.GONE
        txtTut.visibility = View.GONE
        layoutLoading.visibility = View.VISIBLE

    }

    override fun error(errorMessage: String) {
        txtEmptyResult.visibility = View.GONE
        txtError.text = errorMessage
        layoutResult.visibility = View.GONE
        txtTut.visibility = View.GONE
        layoutLoading.visibility = View.VISIBLE
    }

    override fun emptyResult() {
        txtEmptyResult.visibility = View.VISIBLE
        layoutResult.visibility = View.GONE
        txtTut.visibility = View.GONE
        layoutLoading.visibility = View.GONE
    }

    override fun clearResult() {
        txtEmptyResult.visibility = View.GONE
        layoutResult.visibility = View.GONE
        txtTut.visibility = View.VISIBLE
        layoutLoading.visibility = View.GONE
    }
}