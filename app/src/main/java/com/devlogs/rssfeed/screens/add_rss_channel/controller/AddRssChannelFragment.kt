package com.devlogs.rssfeed.screens.add_rss_channel.controller

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.devlogs.rssfeed.R
import com.devlogs.rssfeed.application.ApplicationStateManager
import com.devlogs.rssfeed.screens.add_rss_channel.mvc_view.AddRssChannelMvcView
import com.devlogs.rssfeed.screens.add_rss_channel.mvc_view.getAddRssChannelMvcView
import com.devlogs.rssfeed.screens.add_rss_channel.presentation_state.AddChannelPresentationState
import com.devlogs.rssfeed.screens.add_rss_channel.presentation_state.AddChannelPresentationState.DisplayState
import com.devlogs.rssfeed.screens.common.mvcview.MvcViewFactory
import com.devlogs.rssfeed.screens.common.presentation_state.PresentationAction
import com.devlogs.rssfeed.screens.common.presentation_state.PresentationState
import com.devlogs.rssfeed.screens.common.presentation_state.PresentationStateChangedListener
import com.devlogs.rssfeed.screens.common.presentation_state.PresentationStateManager
import com.devlogs.rssfeed.screens.read_feeds.mvc_view.ReadFeedsMvcView
import com.devlogs.rssfeed.screens.read_feeds.presentable_model.FeedPresentableModel
import com.devlogs.rssfeed.screens.read_feeds.presentation_state.ReadFeedsScreenPresentationState
import javax.inject.Inject

class AddRssChannelFragment : Fragment(), AddRssChannelMvcView.Listener,
    PresentationStateChangedListener {
    companion object {

        @JvmStatic
        fun newInstance() = AddRssChannelFragment()
    }

    @Inject
    protected lateinit var mvcViewFactory: MvcViewFactory
    @Inject
    protected lateinit var applicationStateManager: ApplicationStateManager
    @Inject
    protected lateinit var presentationStateManager: PresentationStateManager
    private lateinit var mvcView: AddRssChannelMvcView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presentationStateManager.init(savedInstanceState,
            DisplayState(null, true)
        )
        Log.d("AddRssChannelFragment", presentationStateManager.currentState.javaClass.simpleName)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mvcView = mvcViewFactory.getAddRssChannelMvcView(container)
        return mvcView.getRootView()
    }

    override fun onStart() {
        super.onStart()
        mvcView.register(this)
        presentationStateManager.register(this, true)
    }

    override fun onStop() {
        super.onStop()
        mvcView.unRegister(this)
        presentationStateManager.unRegister(this)
    }

    override fun onStateChanged(
        previousState: PresentationState?,
        currentState: PresentationState,
        action: PresentationAction
    ) {
        TODO("Not yet implemented")
    }

    override fun onBtnSearchClicked() {
        TODO("Not yet implemented")
    }

    override fun onBtnAddClicked(text: CharSequence?) {
        TODO("Not yet implemented")
    }


}