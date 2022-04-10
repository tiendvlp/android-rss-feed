package com.devlogs.rssfeed.screens.feed_content.controller

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.devlogs.rssfeed.screens.feed_content.mvc_view.FeedContentMvcView
import com.devlogs.rssfeed.screens.feed_content.mvc_view.getFeedContentMvcView
import com.devlogs.rssfeed.screens.feed_content.presentable_model.FeedPresentableModel
import com.devlogs.rssfeed.screens.common.mvcview.MvcViewFactory
import dagger.hilt.android.AndroidEntryPoint
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class FeedContentActivity : AppCompatActivity(), FeedContentMvcView.Listener {

    data class Param (
        val feedUrl: String,
        val channelTitle: String,
        val feedTitle: String,
        val feedContent: String,
        val author: String,
        val pubDate: Long, ) : Serializable {
    }

    companion object {
        val PARAM_FEED = "PARAM"
        val BUNDLE_FEED = "BUNDLE_FEED"

        fun start (context: Context, param: Param ) {
            val paramBundle = Bundle()
            paramBundle.putSerializable(BUNDLE_FEED, param)
            val intent = Intent(context, FeedContentActivity::class.java)
            intent.putExtra(PARAM_FEED, paramBundle)
            context.startActivity(intent)
        }
    }


    private lateinit var param : Param
    @Inject
    protected lateinit var mvcViewFactory: MvcViewFactory

    private lateinit var mvcView : FeedContentMvcView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mvcView = mvcViewFactory.getFeedContentMvcView(null)
        setContentView(mvcView.getRootView())
        if (savedInstanceState != null && savedInstanceState.containsKey(PARAM_FEED)) {
            param = savedInstanceState.getSerializable(PARAM_FEED) as Param
        } else {
            val bundle = intent.getBundleExtra(PARAM_FEED)

            if (bundle != null) {
                param = bundle.getSerializable(BUNDLE_FEED) as Param
            } else {
                Log.e("FeedContentActivity", "The ${Param::class.java.canonicalName} is required")
                finish()
            }
        }

        mvcView.show(convertParamInToPresentableModel())
    }

    private fun convertParamInToPresentableModel () : FeedPresentableModel {
        val date = Date(param.pubDate)
        val dateFormat = SimpleDateFormat("dd/MM/yy HH:mm")

        val pubDateInString = dateFormat.format(date)

        return FeedPresentableModel(
            param.feedUrl,
            param.channelTitle,
            param.feedTitle,
            param.feedContent,
            param.author,
            pubDateInString,
        )
    }

    override fun onStart() {
        super.onStart()
        mvcView.register(this)
    }

    override fun onStop() {
        super.onStop()
        mvcView.unRegister(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(PARAM_FEED, param)
        super.onSaveInstanceState(outState)
    }

    override fun onBtnBackClicked() {
        onBackPressed()
    }

}