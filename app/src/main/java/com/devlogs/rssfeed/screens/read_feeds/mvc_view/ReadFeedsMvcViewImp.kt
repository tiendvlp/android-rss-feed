package com.devlogs.rssfeed.screens.read_feeds.mvc_view

import android.util.Log
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.devlogs.rssfeed.R
import com.devlogs.rssfeed.screens.common.RssLinearLayoutManager
import com.devlogs.rssfeed.screens.read_feeds.controller.FeedsRcvAdapter
import com.devlogs.rssfeed.screens.read_feeds.presentable_model.FeedPresentableModel
import com.devlogs.rssfeed.screens.read_feeds.presentable_model.RssChannelPresentableModel
import com.devlogs.rssfeed.screens.common.mvcview.BaseMvcView
import com.devlogs.rssfeed.screens.common.mvcview.UIToolkit
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class ReadFeedsMvcViewImp : BaseMvcView<ReadFeedsMvcView.Listener>, ReadFeedsMvcView {

    private lateinit var toolbar: Toolbar
    private lateinit var txtChannelTitle : TextView
    private lateinit var imgAvatar: CircleImageView
    private lateinit var lvFeeds: RecyclerView
    private val feeds = TreeSet<FeedPresentableModel> ()
    private val uiToolkit: UIToolkit
    private lateinit var feedsRcvAdapter : FeedsRcvAdapter
    private lateinit var refreshLayout: SwipeRefreshLayout

    constructor(uiToolkit: UIToolkit, viewGroup: ViewGroup?) {
        setRootView(uiToolkit.layoutInflater.inflate(R.layout.layout_read_feeds, viewGroup, false))
        feeds.descendingSet()
        this.uiToolkit = uiToolkit
        addControls();
        addEvents();
        initToolbar()
    }

    private fun initToolbar() {
        val toolbarLayout = uiToolkit.layoutInflater.inflate(R.layout.layout_feed_screen_toolbar, toolbar,false)
        toolbar.addView(toolbarLayout)
        txtChannelTitle = toolbarLayout.findViewById(R.id.txtChannelTitle)
        imgAvatar = toolbarLayout.findViewById(R.id.imgAvatar)
    }

    private fun addEvents() {
        feedsRcvAdapter.onFeedClicked = { type, selectedFeeds ->
            getListener().forEach { listener ->
                if (type == FeedsRcvAdapter.FeedInteractionType.Clicked) {
                    listener.onFeedClicked (selectedFeeds)
                } else if (type == FeedsRcvAdapter.FeedInteractionType.Favorite) {
                    listener.onFeedSavedClicked(selectedFeeds)
                }
            }
        }

        feedsRcvAdapter.onLoadMore = {
            getListener().forEach { listener ->
                listener.onLoadMoreFeeds()
            }
        }

        refreshLayout.setOnRefreshListener {
            getListener().forEach { listener ->
                listener.onReload()
            }
        }
    }

    private fun addControls() {
        refreshLayout = findViewById(R.id.refresh)
        lvFeeds = findViewById(R.id.lvFeeds)
        toolbar = findViewById(R.id.toolbar)
        feedsRcvAdapter = FeedsRcvAdapter(feeds)
        lvFeeds.layoutManager = RssLinearLayoutManager(getContext())
        lvFeeds.setItemViewCacheSize(30)
        lvFeeds.setHasFixedSize(true)
        feedsRcvAdapter.setRecyclerView(lvFeeds)
        feedsRcvAdapter.isLoadMoreEnable = false
        lvFeeds.adapter = feedsRcvAdapter


    }

    override fun setUserAvatarUrl(url: String) {
        Glide.with(getContext()).load(url).into(imgAvatar)
    }

    override fun setChannels(channel: RssChannelPresentableModel) {
        txtChannelTitle.text = channel.title
    }

    override fun appendFeeds(feeds: TreeSet<FeedPresentableModel>) {
        Log.d("ReadFeedsMvcView", "Append feeds: ${feeds.size}")
        feedsRcvAdapter.isLoadMoreEnable = true
        if (this.feeds.addAll(feeds)) {
            feedsRcvAdapter.notifyItemRangeInserted(this.feeds.size,feeds.size)
        }
    }

    override fun addNewFeeds(feeds: TreeSet<FeedPresentableModel>) {
        Log.d("ReadFeedsMvcView", "Add new feeds: ${feeds.size}")
        feedsRcvAdapter.isLoadMoreEnable = true
        refreshLayout.isRefreshing = false
        feedsRcvAdapter.isLoading = false
        if (this.feeds.addAll(feeds)) {
            feedsRcvAdapter.notifyItemRangeInserted(0, feeds.size)
        }
    }

    override fun hideRefreshLayout() {
        refreshLayout.isRefreshing = false
    }

    override fun clear() {
        feeds.clear()
        feedsRcvAdapter.notifyDataSetChanged()
    }

}