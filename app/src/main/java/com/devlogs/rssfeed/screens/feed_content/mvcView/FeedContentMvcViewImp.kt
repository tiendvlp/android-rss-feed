package com.devlogs.rssfeed.screens.feed_content.mvcView

import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.*
import com.devlogs.rssfeed.R
import com.devlogs.rssfeed.screens.feed_content.controller.FeedWebViewClient
import com.devlogs.rssfeed.screens.feed_content.presentable_model.FeedPresentableModel
import com.devlogs.rssfeed.screens.common.mvcview.BaseMvcView
import com.devlogs.rssfeed.screens.common.mvcview.UIToolkit

class FeedContentMvcViewImp : BaseMvcView<FeedContentMvcView.Listener>, FeedContentMvcView {

    private val uiToolkit : UIToolkit
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var toolbar: Toolbar
    private lateinit var webViewClient: FeedWebViewClient
    private lateinit var txtToolbarChannelTitle: TextView
    private lateinit var btnToolbarBack: ImageButton

    constructor(uiToolkit: UIToolkit, viewGroup: ViewGroup?) {
        this.uiToolkit = uiToolkit
        setRootView(uiToolkit.layoutInflater.inflate(R.layout.layout_feed_content, viewGroup, false))
        addControls()
        setupToolbarLayout()
        addEvents()
    }

    private fun addEvents() {
        btnToolbarBack.setOnClickListener {
            getListener().forEach { listener ->
                listener.onBtnBackClicked()
            }
        }
    }

    private fun setupToolbarLayout() {
        val toolbarLayout = uiToolkit.layoutInflater.inflate(R.layout.layout_title_back_toolbar, toolbar, false)
        toolbar.addView(toolbarLayout)
        txtToolbarChannelTitle = toolbarLayout.findViewById(R.id.txtTitle)
        btnToolbarBack = toolbar.findViewById(R.id.btnBack)
    }

    private fun addControls() {
        webView = findViewById(R.id.webview)
        toolbar = findViewById(R.id.toolbar)
        progressBar = findViewById(R.id.progressBar)
        webViewClient = FeedWebViewClient()
        webView.webViewClient = webViewClient
        webView.settings.javaScriptEnabled = true
    }

    override fun show(feed: FeedPresentableModel) {
        if (feed.channelTitle.length <= 28) {
            txtToolbarChannelTitle.text = feed.channelTitle
        } else {
            txtToolbarChannelTitle.text = feed.channelTitle.substring(0, 25) + "..."
        }
        progressBar.visibility = View.VISIBLE
        webView.visibility = View.INVISIBLE
        val provider = FeedStyleProvider(feed)
        webViewClient.setCss(provider.getCss())
        webViewClient.setMvcView(this)
        webView.loadData(provider.getHtml(), "text/html; charset=utf-8", "UTF-8");
    }

    override fun hideLoading() {
        webView.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
    }

}