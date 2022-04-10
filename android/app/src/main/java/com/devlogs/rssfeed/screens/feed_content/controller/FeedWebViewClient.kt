package com.devlogs.rssfeed.screens.feed_content.controller

import android.webkit.WebView
import android.webkit.WebViewClient
import com.devlogs.rssfeed.screens.feed_content.mvc_view.FeedContentMvcView
import android.content.Intent
import android.net.Uri


class FeedWebViewClient : WebViewClient() {

    private var css: String? = null

    fun setCss (css: String) {
        this.css = css
    }

    private lateinit var mvcView: FeedContentMvcView;

    override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
        return if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
            view.context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(url))
            )
            true
        } else {
            false
        }
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        val code = """javascript:(function() { 
                            var node = document.createElement('style');
                    
                            node.type = 'text/css';
                            node.innerHTML = '
                                ${if (css == null) "" else css}
                            ';
                            
                            document.head.appendChild(node);
                         
                        })()""".trimIndent()
        view?.loadUrl(code)
        mvcView.hideLoading()
    }

    fun setMvcView(mvcView: FeedContentMvcView) {
        this.mvcView = mvcView
    }

}