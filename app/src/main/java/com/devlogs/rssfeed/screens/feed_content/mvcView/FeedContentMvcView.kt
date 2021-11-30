package com.devlogs.rssfeed.screens.feed_content.mvcView

import android.view.ViewGroup
import com.devlogs.rssfeed.screens.feed_content.presentable_model.FeedPresentableModel
import com.devlogs.rssfeed.screens.common.mvcview.MvcViewFactory
import com.devlogs.rssfeed.screens.common.mvcview.ObservableMvcView

interface FeedContentMvcView : ObservableMvcView<FeedContentMvcView.Listener> {
    interface Listener {

    }

    fun show (feed: FeedPresentableModel)
    fun hideLoading()
}


fun MvcViewFactory.getFeedContentMvcView (viewGroup: ViewGroup?) : FeedContentMvcView = FeedContentMvcViewImp(uiToolkit, viewGroup)