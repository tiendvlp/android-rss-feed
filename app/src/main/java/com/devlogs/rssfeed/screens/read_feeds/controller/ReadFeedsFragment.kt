package com.devlogs.rssfeed.screens.read_feeds.controller

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.devlogs.rssfeed.android_services.RssChannelTrackingService
import com.devlogs.rssfeed.application.ApplicationStateManager
import com.devlogs.rssfeed.feeds.GetFeedsByRssChannelUseCaseSync
import com.devlogs.rssfeed.screens.read_feeds.mvc_view.ReadFeedsMvcView
import com.devlogs.rssfeed.screens.read_feeds.mvc_view.getReadFeedsMvcView
import com.devlogs.rssfeed.screens.read_feeds.presentable_model.FeedPresentableModel
import com.devlogs.rssfeed.screens.common.mvcview.MvcViewFactory
import com.devlogs.rssfeed.screens.common.presentation_state.PresentationAction
import com.devlogs.rssfeed.screens.common.presentation_state.PresentationState
import com.devlogs.rssfeed.screens.common.presentation_state.PresentationStateChangedListener
import com.devlogs.rssfeed.screens.common.presentation_state.PresentationStateManager
import com.devlogs.rssfeed.screens.read_feeds.presentation_state.ReadFeedsScreenPresentationAction
import com.devlogs.rssfeed.screens.read_feeds.presentation_state.ReadFeedsScreenPresentationAction.*
import com.devlogs.rssfeed.screens.read_feeds.presentation_state.ReadFeedsScreenPresentationState.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@AndroidEntryPoint
class ReadFeedsFragment : Fragment(), ReadFeedsMvcView.Listener, PresentationStateChangedListener {
    companion object {
        @JvmStatic
        fun newInstance() =
            ReadFeedsFragment()
    }

    @Inject
    protected lateinit var mvcViewFactory: MvcViewFactory
    private lateinit var mvcView : ReadFeedsMvcView
    @Inject
    protected lateinit var getFeedsByRssChannelUseCaseSync : GetFeedsByRssChannelUseCaseSync
    @Inject
    protected lateinit var presentationStateManager: PresentationStateManager
    @Inject
    protected lateinit var feedsController: FeedsController
    @Inject
    protected lateinit var applicationStateManager: ApplicationStateManager
    @Inject
    protected lateinit var newFeedsServiceConnector: NewFeedsServiceConnector


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presentationStateManager.init(savedInstanceState, InitialLoadingState(applicationStateManager.selectedChannelId!!))
        Log.d("ReadFeedsFragment", presentationStateManager.currentState.javaClass.simpleName)
        if (presentationStateManager.currentState is InitialLoadingState) {
            feedsController.initialLoad()
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        presentationStateManager.onSavedInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mvcView = mvcViewFactory.getReadFeedsMvcView(container)
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

    override fun onFeedClicked(selectedFeeds: FeedPresentableModel) {
        Toast.makeText(requireContext(), selectedFeeds.title, Toast.LENGTH_SHORT).show()
    }

    override fun onFeedSavedClicked(selectedFeeds: FeedPresentableModel) {
        Toast.makeText(requireContext(),"Saved: " + selectedFeeds.title, Toast.LENGTH_SHORT).show()
    }

    override fun onLoadMoreFeeds() {
        feedsController.loadMore()
    }

    override fun onReload() {
        feedsController.reload()
    }

    override fun onStateChanged(
        previousState: PresentationState?,
        currentState: PresentationState,
        action: PresentationAction
    ) {
        Log.d("ReadFeedsFragment", presentationStateManager.currentState.javaClass.simpleName)
        when (currentState) {
            is InitialLoadingState -> {
            }
            is InitialLoadFailedState -> {}
            is DisplayState -> {
                displayStateProcess (previousState, currentState, action)
            }
        }
    }

    private fun displayStateProcess(
        previousState: PresentationState?,
        currentState: DisplayState,
        action: PresentationAction
    ) {
        when (action) {
            is ReloadActionSuccess -> {
                mvcView.hideRefreshLayout()
            }
            is ReloadActionFailed -> {
                mvcView.hideRefreshLayout()
            }
            is NewFeedsAction -> {
                mvcView.addNewFeeds(action.feeds)
            }
            is InitialLoadSuccessAction -> {
                mvcView.appendFeeds(action.feeds)
                mvcView.setChannels(action.channel)
                mvcView.setUserAvatarUrl(action.userAvatar)
                RssChannelTrackingService.bind(requireContext(), newFeedsServiceConnector)
            }
            is LoadMoreFailedAction -> {
                Log.d("ReadFeedsFragment", "Load more failed: ${action.errorMessage}")
            }
            is LoadMoreSuccessAction -> {
                mvcView.appendFeeds(action.feeds)
                Log.d("ReadFeedsFragment", "Load more success: ${action.feeds.size} feeds")
            }
        }
    }
}