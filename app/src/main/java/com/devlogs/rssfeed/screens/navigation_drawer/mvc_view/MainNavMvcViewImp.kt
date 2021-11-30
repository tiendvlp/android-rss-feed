package com.devlogs.rssfeed.screens.navigation_drawer.mvc_view

import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.devlogs.rssfeed.R
import com.devlogs.rssfeed.screens.common.mvcview.BaseMvcView
import com.devlogs.rssfeed.screens.common.mvcview.UIToolkit
import com.devlogs.rssfeed.screens.navigation_drawer.controller.ChannelRcvAdapter
import com.devlogs.rssfeed.screens.navigation_drawer.presentable_model.ChannelPresentableModel
import com.devlogs.rssfeed.screens.navigation_drawer.presentable_model.UserPresentableModel

class MainNavMvcViewImp : BaseMvcView<MainNavMvcView.Listener>, MainNavMvcView{

    private val uiToolkit: UIToolkit

    private lateinit var layoutLoading: LinearLayout
    private lateinit var layoutMain: ConstraintLayout
    private lateinit var imgAvatar: ImageView
    private lateinit var lvChannels: RecyclerView
    private lateinit var btnSignOut: Button
    private lateinit var txtUserName: TextView
    private lateinit var channelAdapter: ChannelRcvAdapter
    private lateinit var txtEmpty : TextView
    private val channels = ArrayList<ChannelPresentableModel> ()

    constructor(uiToolkit: UIToolkit, viewGroup: ViewGroup?) {
        this.uiToolkit = uiToolkit
        setRootView(uiToolkit.layoutInflater.inflate(R.layout.layout_main_nav, viewGroup, false))

        addControls()
        addEvents()
    }

    private fun addControls() {
        layoutLoading = findViewById(R.id.layoutLoading)
        txtUserName = findViewById(R.id.txtUserName)
        layoutMain = findViewById(R.id.layoutMain)
        imgAvatar = findViewById(R.id.imgAvatar)
        lvChannels = findViewById(R.id.lvChannels)
        btnSignOut = findViewById(R.id.btnSignOut)
        txtEmpty = findViewById(R.id.txtEmpty)
        channelAdapter = ChannelRcvAdapter(channels)
        lvChannels.layoutManager = LinearLayoutManager(getContext())
        lvChannels.adapter = channelAdapter
    }

    private fun addEvents() {
        channelAdapter.onItemClicked = { channel ->
            getListener().forEach { listener ->
                listener.onChannelSelected(channel)
            }
        }
        btnSignOut.setOnClickListener {
            getListener().forEach { listener ->
                listener.onBtnSignOutClicked()
            }
        }
    }

    override fun loading() {
        txtEmpty.visibility = View.GONE
        layoutLoading.visibility = View.VISIBLE
        layoutMain.visibility = View.GONE
    }

    override fun setChannels(channels: List<ChannelPresentableModel>) {
        layoutMain.visibility = View.VISIBLE
        if (channels.isEmpty()) {
            txtEmpty.visibility = View.VISIBLE
            layoutLoading.visibility = View.GONE
            lvChannels.visibility = View.GONE
        } else {
            layoutLoading.visibility = View.GONE
            lvChannels.visibility = View.VISIBLE
            this.channels.clear()
            this.channels.addAll(channels)
            channelAdapter.notifyDataSetChanged()
        }
    }

    override fun setUsers(user: UserPresentableModel) {
        Glide.with(getContext())
            .load(user.avatarUrl)
            .into(imgAvatar)

        txtUserName.text = user.name
    }
}