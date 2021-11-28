package com.devlogs.rssfeed.screens.read_feeds.controller

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.devlogs.rssfeed.encrypt.UrlEncrypt
import com.devlogs.rssfeed.feeds.GetFeedsByRssChannelUseCaseSync
import com.devlogs.rssfeed.screens.read_feeds.mvc_view.ReadFeedsMvcView
import com.devlogs.rssfeed.screens.read_feeds.mvc_view.getReadFeedsMvcView
import com.devlogs.rssfeed.screens.read_feeds.presentable_model.FeedPresentableModel
import com.devlogs.rssfeed.screens.common.mvcview.MvcViewFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ReadFeedsFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @Inject
    protected lateinit var mvcViewFactory: MvcViewFactory
    private lateinit var mvcView : ReadFeedsMvcView
    private val coroutine = CoroutineScope(Dispatchers.Main.immediate)
    @Inject
    protected lateinit var getFeedsByRssChannelUseCaseSync : GetFeedsByRssChannelUseCaseSync

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mvcView = mvcViewFactory.getReadFeedsMvcView(container)
        return mvcView.getRootView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        coroutine.launch {
            val gResult = getFeedsByRssChannelUseCaseSync.executes(UrlEncrypt.encode("https://vnexpress.net/rss/tin-noi-bat.rss"), System.currentTimeMillis(), 20)
            val treeSet = TreeSet<FeedPresentableModel>()
            Log.d("ReadFeedsFragment", "Start loadfeeds")
            if (gResult is GetFeedsByRssChannelUseCaseSync.Result.Success) {
                treeSet.addAll((gResult.rssChannel.map { FeedPresentableModel(
                it.id,
                it.rssChannelId,
                it.channelTitle,
                it.title,
                it.pubDate, it.pubDate.toString(),
                it.url,
                it.author,
                it.imageUrl
            ) }))
                Log.d("ReadFeedsFragment", "Success load ${gResult.rssChannel.size} feeds")
                mvcView.addNewFeeds(treeSet)
            } else {
                Log.d("ReadFeedsFragment", "Error: ${gResult.javaClass.simpleName}")
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ReadFeedsFragment()
    }
}