package com.devlogs.rssfeed.screens.feed_content.mvcView

import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ProgressBar
import com.devlogs.rssfeed.R
import com.devlogs.rssfeed.screens.feed_content.controller.FeedWebViewClient
import com.devlogs.rssfeed.screens.feed_content.presentable_model.FeedPresentableModel
import com.devlogs.rssfeed.screens.common.mvcview.BaseMvcView
import com.devlogs.rssfeed.screens.common.mvcview.UIToolkit

class FeedContentMvcViewImp : BaseMvcView<FeedContentMvcView.Listener>, FeedContentMvcView {

    private val uiToolkit : UIToolkit
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar

    private lateinit var webViewClient: FeedWebViewClient

    constructor(uiToolkit: UIToolkit, viewGroup: ViewGroup?) {
        this.uiToolkit = uiToolkit
        setRootView(uiToolkit.layoutInflater.inflate(R.layout.layout_feed_content, viewGroup, false))
        addControls()
    }

    private fun addControls() {
        webView = findViewById(R.id.webview)
        progressBar = findViewById(R.id.progressBar)
        webViewClient = FeedWebViewClient()
        webView.webViewClient = webViewClient
        webView.settings.javaScriptEnabled = true
    }

    override fun show(feed: FeedPresentableModel) {
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