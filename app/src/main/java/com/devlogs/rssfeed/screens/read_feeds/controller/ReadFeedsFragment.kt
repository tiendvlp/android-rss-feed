package com.devlogs.rssfeed.screens.read_feeds.controller

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.devlogs.chatty.screen.common.presentationstate.CommonPresentationAction.RestoreAction
import com.devlogs.rssfeed.android_services.RssChannelTrackingService
import com.devlogs.rssfeed.application.ApplicationStateManager
import com.devlogs.rssfeed.common.shared_context.AppConfig.DaggerNamed.FRAGMENT_SCOPE
import com.devlogs.rssfeed.screens.feed_content.controller.FeedContentActivity
import com.devlogs.rssfeed.screens.read_feeds.mvc_view.ReadFeedsMvcView
import com.devlogs.rssfeed.screens.read_feeds.mvc_view.getReadFeedsMvcView
import com.devlogs.rssfeed.screens.read_feeds.presentable_model.FeedPresentableModel
import com.devlogs.rssfeed.screens.common.mvcview.MvcViewFactory
import com.devlogs.rssfeed.screens.common.presentation_state.PresentationAction
import com.devlogs.rssfeed.screens.common.presentation_state.PresentationState
import com.devlogs.rssfeed.screens.common.presentation_state.PresentationStateChangedListener
import com.devlogs.rssfeed.screens.common.presentation_state.PresentationStateManager
import com.devlogs.rssfeed.screens.main.MainScreenInsiderListener
import com.devlogs.rssfeed.screens.main.MainScreenInsiderObservable
import com.devlogs.rssfeed.screens.read_feeds.presentation_state.ReadFeedsScreenPresentationAction.*
import com.devlogs.rssfeed.screens.read_feeds.presentation_state.ReadFeedsScreenPresentationState.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class ReadFeedsFragment : Fragment(), ReadFeedsMvcView.Listener, PresentationStateChangedListener, MainScreenInsiderListener{
    companion object {
        @JvmStatic
        fun newInstance() =
            ReadFeedsFragment()
    }

    @Inject
    protected lateinit var mvcViewFactory: MvcViewFactory
    @Inject
    protected lateinit var applicationStateManager: ApplicationStateManager
    @Inject
    @Named(FRAGMENT_SCOPE)
    protected lateinit var presentationStateManager: PresentationStateManager
    private lateinit var mvcView : ReadFeedsMvcView
    @Inject
    protected lateinit var mainScreenInsiderObservable: MainScreenInsiderObservable
    @Inject
    protected lateinit var feedsController: FeedsController
    @Inject
    protected lateinit var newFeedsServiceConnector: NewFeedsServiceConnector


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userSelectedChannelId = applicationStateManager.selectedChannelId
        presentationStateManager.init(savedInstanceState, InitialLoadingState(userSelectedChannelId!!))

        Log.d("ReadFeedsFragment", presentationStateManager.currentState.javaClass.simpleName)

    }

    override fun onSaveInstanceState(outState: Bundle) {
        presentationStateManager.onSavedInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val userSelectedChannelId = applicationStateManager.selectedChannelId

        mvcView = mvcViewFactory.getReadFeedsMvcView(container)
        if (presentationStateManager.currentState is DisplayState) {
            val currentDisplayChannelId = (presentationStateManager.currentState as DisplayState).channelPresentableModel.id
            if (!currentDisplayChannelId.equals(userSelectedChannelId)) {
                presentationStateManager.consumeAction(UserSelectChannelAction(userSelectedChannelId!!))
            }
        }
        return mvcView.getRootView()
    }

    override fun onStart() {
        super.onStart()
        mvcView.register(this)
        mainScreenInsiderObservable.register(this)

        presentationStateManager.register(this, true)
    }

    override fun onStop() {
        super.onStop()
        mvcView.unRegister(this)
        presentationStateManager.unRegister(this)
        requireContext().unbindService(newFeedsServiceConnector)
        mainScreenInsiderObservable.unRegister(this)
    }

    override fun onFeedClicked(selectedFeeds: FeedPresentableModel) {
        Toast.makeText(requireContext(), selectedFeeds.title, Toast.LENGTH_SHORT).show()

        if (presentationStateManager.currentState is DisplayState) {
            val param = FeedContentActivity.Param(selectedFeeds.url, selectedFeeds.channelTitle, selectedFeeds.title, selectedFeeds.content,selectedFeeds.author, selectedFeeds.pubDate)
            FeedContentActivity.start(requireContext(),param)
        }

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
        Log.d("ReadFeedsFragment", presentationStateManager.currentState.javaClass.simpleName + " with action: " + action.javaClass.simpleName)
        when (currentState) {
            is InitialLoadingState -> {
                feedsController.initialLoad()
            }
            is InitialLoadFailedState -> {
                requireContext().unbindService(newFeedsServiceConnector)
            }
            is DisplayState -> {
                displayStateProcess (previousState, currentState, action)
            }
        }
    }

    override fun onUserSelectedChannel(channelId: String) {
        super.onUserSelectedChannel(channelId)
        Log.d("ReadFeedsFragment", "User selected : ${channelId}")
        presentationStateManager.consumeAction(UserSelectChannelAction(channelId))
        feedsController.cancel()

    }

    private fun displayStateProcess(
        previousState: PresentationState?,
        currentState: DisplayState,
        action: PresentationAction
    ) {
        when (action) {
            is RestoreAction -> {
                mvcView.appendFeeds(currentState.feeds)
                mvcView.setChannels(currentState.channelPresentableModel)
                mvcView.setUserAvatarUrl(currentState.avatarUrl)
                mvcView.hideRefreshLayout()
                RssChannelTrackingService.bind(requireContext(), newFeedsServiceConnector)
            }
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
                mvcView.clear()
                mvcView.appendFeeds(currentState.feeds)
                mvcView.setChannels(currentState.channelPresentableModel)
                mvcView.setUserAvatarUrl(currentState.avatarUrl)
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