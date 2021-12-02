package com.devlogs.rssfeed.screens.category_feeds.mvc_view

import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devlogs.rssfeed.R
import com.devlogs.rssfeed.screens.category_feeds.controllers.FeedsRcvAdapter
import com.devlogs.rssfeed.screens.category_feeds.presentable_model.FeedPresentableModel
import com.devlogs.rssfeed.screens.common.mvcview.BaseMvcView
import com.devlogs.rssfeed.screens.common.mvcview.UIToolkit
import java.util.*

class CategoryFeedsMvcViewImp : BaseMvcView<CategoryFeedsMvcView.Listener> , CategoryFeedsMvcView {

    private val uiToolkit: UIToolkit
    private lateinit var txtEmpty : TextView
    private lateinit var lvFeeds: RecyclerView
    private lateinit var progressBar: ProgressBar
    private val feeds = TreeSet<FeedPresentableModel> ()
    private lateinit var feedAdapter : FeedsRcvAdapter

    constructor(uiToolkit: UIToolkit) {
        this.uiToolkit = uiToolkit

        addControls()
        addEvents()
    }

    private fun addControls() {
        txtEmpty = findViewById(R.id.txtEmpty)
        lvFeeds = findViewById(R.id.lvFeeds)
        progressBar = findViewById(R.id.progressBar)
        feedAdapter = FeedsRcvAdapter(feeds)
        lvFeeds.layoutManager = LinearLayoutManager(getContext())
        lvFeeds.setHasFixedSize(true)
        lvFeeds.setItemViewCacheSize(30)
        lvFeeds.adapter = feedAdapter
    }

    private fun addEvents() {
        feedAdapter.onFeedClicked = {
            getListener().forEach { listener ->
                listener.onFeedSelected (it)
            }
        }
    }

    override fun loading() {
        txtEmpty.visibility = View.GONE
        lvFeeds.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
    }

    override fun showEmptyText() {
        txtEmpty.visibility = View.VISIBLE
        lvFeeds.visibility = View.GONE
        progressBar.visibility = View.GONE
    }

    override fun setFeeds(feeds: Set<FeedPresentableModel>) {
        txtEmpty.visibility = View.GONE
        lvFeeds.visibility = View.VISIBLE
        progressBar.visibility = View.GONE

        this.feeds.clear()
        if (this.feeds.addAll(feeds)) {
            feedAdapter.notifyDataSetChanged()
        }
    }
}