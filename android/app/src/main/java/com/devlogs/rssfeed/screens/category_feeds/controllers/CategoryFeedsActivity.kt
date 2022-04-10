package com.devlogs.rssfeed.screens.category_feeds.controllers

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.devlogs.rssfeed.R
import com.devlogs.rssfeed.screens.category_feeds.mvc_view.CategoryFeedsMvcView
import com.devlogs.rssfeed.screens.category_feeds.mvc_view.getCategoryFeedsMvcView
import com.devlogs.rssfeed.screens.category_feeds.presentable_model.FeedPresentableModel
import com.devlogs.rssfeed.screens.common.mvcview.MvcViewFactory
import com.devlogs.rssfeed.screens.feed_content.controller.FeedContentActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CategoryFeedsActivity : AppCompatActivity(), CategoryFeedsMvcView.Listener {


    companion object {
        const val PARAM_CATEGORY_TITLE = "PARAM_CATEGORY_TITLE"

        fun start (currentContext: Context, categoryTitle: String) {
            val intent = Intent(currentContext, CategoryFeedsActivity::class.java)
            intent.putExtra(PARAM_CATEGORY_TITLE, categoryTitle)
            currentContext.startActivity(intent)
        }
    }

    @Inject
    protected lateinit var controller: CategoryFeedsController
    @Inject
    protected lateinit var mvcViewFactory : MvcViewFactory
    private lateinit var mvcView : CategoryFeedsMvcView

    private lateinit var categoryTitle: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.getStringExtra(PARAM_CATEGORY_TITLE) != null) {
            categoryTitle = intent.getStringExtra(PARAM_CATEGORY_TITLE)!!
        } else {
            if (savedInstanceState != null && savedInstanceState.containsKey(PARAM_CATEGORY_TITLE)) {
                categoryTitle = savedInstanceState.getString(PARAM_CATEGORY_TITLE)!!
            } else {
                throw RuntimeException("Invalid argument exception, the $PARAM_CATEGORY_TITLE is required")
            }
        }

        mvcView = mvcViewFactory.getCategoryFeedsMvcView(null)
        controller.setMvcView(mvcView)
        controller.getCategories(categoryTitle)
        setContentView(mvcView.getRootView())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(PARAM_CATEGORY_TITLE, categoryTitle)
        super.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()
        mvcView.register(this)
    }

    override fun onStop() {
        super.onStop()
        mvcView.unRegister(this)
    }

    override fun onFeedSelected(it: FeedPresentableModel) {
        FeedContentActivity.start(this, FeedContentActivity.Param(
            it.url,
            it.channelTitle,
            it.title,
            it.content,
            it.author,
            it.pubDate
        ))
    }

    override fun onBackClicked() {
        onBackPressed()
    }
}