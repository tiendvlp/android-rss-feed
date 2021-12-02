package com.devlogs.rssfeed.screens.category_feeds.mvc_view

import android.view.View
import android.view.ViewGroup
import android.widget.*
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
    private lateinit var toolbar: Toolbar
    private lateinit var btnToolbarBack: ImageButton
    private val feeds = TreeSet<FeedPresentableModel> ()
    private lateinit var feedAdapter : FeedsRcvAdapter
    private lateinit var txtToolbarTitle: TextView

    constructor(uiToolkit: UIToolkit, viewGroup: ViewGroup?) {
        this.uiToolkit = uiToolkit
        setRootView(uiToolkit.layoutInflater.inflate(R.layout.layout_category_feeds, viewGroup, false))
        addControls()
        setupToolbar()
        addEvents()
    }

    private fun setupToolbar() {
        val layoutToolbar = uiToolkit.layoutInflater.inflate(R.layout.layout_title_back_toolbar, toolbar, false)
        toolbar.addView(layoutToolbar)
        btnToolbarBack = layoutToolbar.findViewById(R.id.btnBack)
        txtToolbarTitle = layoutToolbar.findViewById(R.id.txtTitle)
    }

    private fun addControls() {
        txtEmpty = findViewById(R.id.txtEmpty)
        toolbar = findViewById(R.id.toolbar)
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

        btnToolbarBack.setOnClickListener {
            getListener().forEach { listener ->
                listener.onBackClicked()
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

    override fun setTitle(title: String) {
        txtToolbarTitle.text = title
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

    override fun toast(message: String) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show()
    }
}