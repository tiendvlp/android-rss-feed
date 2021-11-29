package com.devlogs.rssfeed.screens.navigation_drawer.mvc_view

import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.devlogs.rssfeed.R
import com.devlogs.rssfeed.screens.common.mvcview.BaseMvcView
import com.devlogs.rssfeed.screens.common.mvcview.UIToolkit
import com.devlogs.rssfeed.screens.navigation_drawer.presentable_model.ChannelPresentableModel
import com.devlogs.rssfeed.screens.navigation_drawer.presentable_model.UserPresentableModel

class MainNavMvcViewImp : BaseMvcView<MainNavMvcView.Listener>, MainNavMvcView{

    private val uiToolkit: UIToolkit

    private lateinit var layoutLoading: LinearLayout
    private lateinit var layoutMain: ConstraintLayout
    private lateinit var imgAvatar: ImageView
    private lateinit var lvChannels: ListView
    private lateinit var btnSignOut: Button
    private lateinit var txtUserName: TextView

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
    }

    private fun addEvents() {
        btnSignOut.setOnClickListener {
            getListener().forEach { listener ->
                listener.onBtnSignOutClicked()
            }
        }
    }

    override fun loading() {
        layoutLoading.visibility = View.VISIBLE
        layoutMain.visibility = View.GONE
    }

    override fun setChannels(channels: List<ChannelPresentableModel>) {
        layoutLoading.visibility = View.GONE
        layoutMain.visibility = View.VISIBLE
    }

    override fun setUsers(user: UserPresentableModel) {
        Glide.with(getContext())
            .load(user.avatarUrl)
            .into(imgAvatar)

        txtUserName.text = user.name
    }
}