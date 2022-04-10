package com.devlogs.rssfeed.screens.read_feeds.mvc_view

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.devlogs.rssfeed.R
import com.devlogs.rssfeed.common.helper.LogTarget
import com.devlogs.rssfeed.common.helper.normalLog
import com.devlogs.rssfeed.screens.common.RssLinearLayoutManager
import com.devlogs.rssfeed.screens.read_feeds.controller.FeedsRcvAdapter
import com.devlogs.rssfeed.screens.read_feeds.presentable_model.FeedPresentableModel
import com.devlogs.rssfeed.screens.read_feeds.presentable_model.RssChannelPresentableModel
import com.devlogs.rssfeed.screens.common.mvcview.BaseMvcView
import com.devlogs.rssfeed.screens.common.mvcview.UIToolkit
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class ReadFeedsMvcViewImp : BaseMvcView<ReadFeedsMvcView.Listener>, ReadFeedsMvcView, LogTarget {

    private lateinit var toolbar: Toolbar
    private lateinit var txtChannelTitle : TextView
    private lateinit var imgAvatar: CircleImageView
    private lateinit var lvFeeds: RecyclerView
    private val feeds = TreeSet<FeedPresentableModel> ()
    private val uiToolkit: UIToolkit
    private lateinit var txtEmptyResult : TextView
    private lateinit var feedsRcvAdapter : FeedsRcvAdapter
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var btnFollow: Button
    private lateinit var btnUnFollow: Button
    private lateinit var followProgress : ProgressBar


    constructor(uiToolkit: UIToolkit, viewGroup: ViewGroup?) {
        setRootView(uiToolkit.layoutInflater.inflate(R.layout.layout_read_feeds, viewGroup, false))
        feeds.descendingSet()
        this.uiToolkit = uiToolkit
        addControls();
        initToolbar()
        addEvents();
    }

    private fun initToolbar() {
        val toolbarLayout = uiToolkit.layoutInflater.inflate(R.layout.layout_feed_screen_toolbar, toolbar,false)
        toolbar.addView(toolbarLayout)
        txtChannelTitle = toolbarLayout.findViewById(R.id.txtChannelTitle)
        imgAvatar = toolbarLayout.findViewById(R.id.imgAvatar)
        btnFollow = toolbarLayout.findViewById(R.id.btnFollow)
        btnUnFollow = toolbarLayout.findViewById(R.id.btnUnFollow)
        followProgress = toolbarLayout.findViewById(R.id.followProgress)
    }

    override fun getCurrentScrollPos(): Int {
        return (lvFeeds.layoutManager!! as LinearLayoutManager).findFirstVisibleItemPosition()
    }

    private fun addEvents() {
        btnFollow.setOnClickListener {
            getListener().forEach { listener ->
                listener.onBtnFollowClicked()
            }
        }
        btnUnFollow.setOnClickListener {
            getListener().forEach { listener ->
                listener.onBtnUnFollowClicked()
            }
        }
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
        txtEmptyResult = findViewById(R.id.txtEmpty)
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

    override fun scrollToPos(position: Int) {
        lvFeeds.scrollToPosition(position)
    }

    override fun setUserAvatarUrl(url: String?) {
        Glide.with(getContext()).load(url).into(imgAvatar).onLoadFailed(getContext().getDrawable(R.drawable.ic_default_avatar))
    }

    override fun setChannels(channel: RssChannelPresentableModel) {
        txtChannelTitle.text = channel.title
        if (channel.isFollowed) {
            showUnFollowButton()
        } else {
            showFollowButton()
        }
    }

    override fun appendFeeds(feeds: TreeSet<FeedPresentableModel>) {
        Log.d("ReadFeedsMvcView", "Append feeds: ${feeds.size}")
        if (feeds.isEmpty()) {
            Toast.makeText(getContext(),"No more", Toast.LENGTH_SHORT).show()
            lvFeeds.scrollToPosition(getCurrentScrollPos())
            return
        }
        refreshLayout.isEnabled = true
        refreshLayout.visibility = View.VISIBLE
        txtEmptyResult.visibility = View.GONE
        feedsRcvAdapter.isLoadMoreEnable = true
        if (this.feeds.addAll(feeds)) {
            feedsRcvAdapter.notifyItemRangeInserted(this.feeds.size,feeds.size)
        }
    }

    override fun addNewFeeds(feeds: TreeSet<FeedPresentableModel>) {
        Log.d("ReadFeedsMvcView", "Add new feeds: ${feeds.size}")
        refreshLayout.isEnabled = true
        refreshLayout.visibility = View.VISIBLE
        txtEmptyResult.visibility = View.GONE
        val oldSize = this.feeds.size
        feedsRcvAdapter.isLoadMoreEnable = true
        refreshLayout.isRefreshing = false
        feedsRcvAdapter.isLoading = false
        if (this.feeds.addAll(feeds)) {
            feedsRcvAdapter.notifyDataSetChanged()
        }
        lvFeeds.scrollToPosition(0)
    }

    override fun hideRefreshLayout() {
        refreshLayout.isRefreshing = false
    }

    override fun clear() {
        feeds.clear()
        feedsRcvAdapter.notifyDataSetChanged()
    }

    override fun empty() {
        refreshLayout.isRefreshing = false
        refreshLayout.isEnabled = false
        refreshLayout.visibility = View.GONE
        txtEmptyResult.visibility = View.VISIBLE
    }

    override fun showFollowLoading() {
        followProgress.visibility = View.VISIBLE
        btnFollow.visibility = View.GONE
        btnUnFollow.visibility = View.GONE
    }

    override fun dismissFollowLoading() {
        followProgress.visibility = View.GONE
    }

    override fun showFollowButton() {
        dismissFollowLoading()
        btnFollow.visibility = View.VISIBLE
        btnUnFollow.visibility = View.GONE
    }

    override fun showUnFollowButton() {
        dismissFollowLoading()
        btnUnFollow.visibility = View.VISIBLE
        btnFollow.visibility = View.GONE
    }

    override fun showMessage(message: String) {

        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show()
    }

}