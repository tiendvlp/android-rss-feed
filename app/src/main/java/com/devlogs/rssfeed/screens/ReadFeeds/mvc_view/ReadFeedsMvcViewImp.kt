package com.devlogs.rssfeed.screens.ReadFeeds.mvc_view

import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.devlogs.rssfeed.R
import com.devlogs.rssfeed.screens.ReadFeeds.presentable_model.FeedPresentableModel
import com.devlogs.rssfeed.screens.ReadFeeds.presentable_model.RssChannelPresentableModel
import com.devlogs.rssfeed.screens.common.mvcview.BaseMvcView
import com.devlogs.rssfeed.screens.common.mvcview.UIToolkit
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class ReadFeedsMvcViewImp : BaseMvcView<ReadFeedsMvcView.Listener>, ReadFeedsMvcView {

    private lateinit var toolbar: Toolbar
    private lateinit var txtChannelTitle : TextView
    private lateinit var imgAvatar: CircleImageView
    private lateinit var lvFeeds: RecyclerView
    private val feeds = TreeSet<FeedPresentableModel> ().descendingSet()
    private val uiToolkit: UIToolkit

    constructor(uiToolkit: UIToolkit, viewGroup: ViewGroup?) {
        this.uiToolkit = uiToolkit
        setRootView(uiToolkit.layoutInflater.inflate(R.layout.layout_add_rss_channel, viewGroup))
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
        TODO("Not yet implemented")
    }

    private fun addControls() {
        lvFeeds = findViewById(R.id.lvFeeds)
        toolbar = findViewById(R.id.toolbar)
    }

    override fun setUserAvatarUrl(url: String) {
        Glide.with(getContext()).load(url).into(imgAvatar)
    }

    override fun setChannels(channel: RssChannelPresentableModel) {
        txtChannelTitle.text = channel.title
    }

    override fun appendFeeds(feeds: TreeSet<FeedPresentableModel>) {
        feeds.addAll(feeds)
    }

    override fun addNewFeeds(feeds: TreeSet<FeedPresentableModel>) {
        feeds.addAll(feeds)
    }

}