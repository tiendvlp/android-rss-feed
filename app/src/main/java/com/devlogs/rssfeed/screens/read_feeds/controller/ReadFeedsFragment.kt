package com.devlogs.rssfeed.screens.read_feeds.controller

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.devlogs.rssfeed.screens.read_feeds.presentation_state.ReadFeedsScreenPresentationAction.InitialLoadSuccessAction
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
    private val coroutine = CoroutineScope(Dispatchers.Main.immediate)
    @Inject
    protected lateinit var getFeedsByRssChannelUseCaseSync : GetFeedsByRssChannelUseCaseSync
    @Inject
    protected lateinit var presentationStateManager: PresentationStateManager
    @Inject
    protected lateinit var feedsController: FeedsController
    @Inject
    protected lateinit var applicationStateManager: ApplicationStateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presentationStateManager.init(savedInstanceState, InitialLoadingState(applicationStateManager.selectedChannelId!!))
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
        presentationStateManager.register(this)
    }

    override fun onStop() {
        super.onStop()
        presentationStateManager.unRegister(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        coroutine.launch {
//            val gResult = getFeedsByRssChannelUseCaseSync.executes(UrlEncrypt.encode("https://vnexpress.net/rss/tin-noi-bat.rss"), System.currentTimeMillis(), 20)
//            val treeSet = TreeSet<FeedPresentableModel>()
//            Log.d("ReadFeedsFragment", "Start loadfeeds")
//            if (gResult is GetFeedsByRssChannelUseCaseSync.Result.Success) {
//                treeSet.addAll((gResult.rssChannel.map { FeedPresentableModel(
//                it.id,
//                it.rssChannelId,
//                it.channelTitle,
//                it.title,
//                it.pubDate, it.pubDate.toString(),
//                it.url,
//                it.author,
//                it.imageUrl
//            ) }))
//                Log.d("ReadFeedsFragment", "Success load ${gResult.rssChannel.size} feeds")
//                mvcView.addNewFeeds(treeSet)
//            } else {
//                Log.d("ReadFeedsFragment", "Error: ${gResult.javaClass.simpleName}")
//            }
//        }
    }

    override fun onFeedClicked(selectedFeeds: FeedPresentableModel) {
        TODO("Not yet implemented")

    }

    override fun onFeedSavedClicked(selectedFeeds: FeedPresentableModel) {
        TODO("Not yet implemented")
    }

    override fun onLoadMoreFeeds() {
        TODO("Not yet implemented")
    }

    override fun onStateChanged(
        previousState: PresentationState?,
        currentState: PresentationState,
        action: PresentationAction
    ) {
        when (currentState) {
            is InitialLoadingState -> {}
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
            is InitialLoadSuccessAction -> {mvcView.appendFeeds(action.feeds)}
            is ReadFeedsScreenPresentationAction.LoadMoreFailedAction -> {}
        }
    }
}