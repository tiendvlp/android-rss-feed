package com.devlogs.rssfeed.screens.read_feeds.controller

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.devlogs.rssfeed.R
import com.devlogs.rssfeed.common.helper.LogTarget
import com.devlogs.rssfeed.common.helper.normalLog
import com.devlogs.rssfeed.screens.common.viewholder.ItemLoadingViewHolder
import com.devlogs.rssfeed.screens.read_feeds.presentable_model.FeedPresentableModel
import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter
import org.sufficientlysecure.htmltextview.HtmlTextView
import java.lang.Exception
import java.net.URL
import java.util.*

class FeedsRcvAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>, LogTarget {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val txtAuthor : TextView = view.findViewById(R.id.txtAuthor)
        private val txtPubDate: TextView = view.findViewById(R.id.txtPubDate)
        private val txtTitle: TextView = view.findViewById(R.id.txtTitle)
        private val img : ImageView = view.findViewById(R.id.img)
        private val imgHtml : HtmlTextView = view.findViewById(R.id.imgHtml)
        private val btnSaved: ImageButton = view.findViewById(R.id.btnSaved)
        fun bind (feed: FeedPresentableModel) {
            txtAuthor.text = feed.author
            if (txtAuthor.text.isEmpty()) {
                txtAuthor.text = feed.channelTitle
            } else {
                if (txtAuthor.text.length < 15) {
                    if (feed.channelTitle.length <= 17) {
                        txtAuthor.text = txtAuthor.text.toString() + " at ${feed.channelTitle}"
                    }
                }
                else if (txtAuthor.text.length > 32) {
                    txtAuthor.text = txtAuthor.text.substring(0 .. 32) + "..."
                }
            }
            txtPubDate.text = feed.pubDateInString
            txtTitle.text = feed.title
            if (feed.imageUrl != null) {
                normalLog("load image: ${feed.imageUrl}" )
                Glide
                    .with(itemView.context)
                    .load((feed.imageUrl))
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            normalLog("Load image failed: ${feed.imageUrl}")
                            try {
                                imgHtml.setHtml(
                                    "<img src=\"${feed.imageUrl}\">",
                                    HtmlHttpImageGetter(imgHtml, "", true)
                                )
                            } catch (ex: Exception) {
                                normalLog("Set html failed due to: ${ex.message}")
                            }
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }
                    })
                    .into(img)
            }
            itemView.setOnClickListener {
                onFeedClicked?.invoke(FeedInteractionType.Clicked, feed)
            }

            btnSaved.setOnClickListener {
                onFeedClicked?.invoke(FeedInteractionType.Favorite, feed)
            }
        }
    }

    enum class FeedInteractionType {
        Favorite, Clicked
    }

    private val feeds: TreeSet<FeedPresentableModel>
    private val FEED_TYPE = 1
    private val LOAD_MORE_TYPE = 2
    var isLoading: Boolean = false
    var isLoadMoreEnable = true;  set(value) {
        field = value
        notifyItemChanged(feeds.size + 1)
    }
    private var lastVisibleItem = 0
    private var totalItemCount = 0
    private var visibleThreadHold = 3
    var onLoadMore : (() -> Unit)? = null
    var onFeedClicked: ((type: FeedInteractionType, selectedChannel: FeedPresentableModel) -> Unit)? = null

    constructor(feeds: TreeSet<FeedPresentableModel>) {
        this.feeds = feeds;
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        if (viewType == FEED_TYPE) {
            val itemView = inflater.inflate(R.layout.item_feed, parent, false)
            return ViewHolder(itemView)
        }
        return ItemLoadingViewHolder(inflater, parent)
    }

    fun setRecyclerView (rcv: RecyclerView) {
        val layoutManager = rcv.layoutManager as LinearLayoutManager
        rcv.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                totalItemCount = layoutManager.itemCount
                lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                if (isLoadMoreEnable && !isLoading && totalItemCount <= (lastVisibleItem+visibleThreadHold)) {
                    onLoadMore?.invoke()
                    isLoading = true
                }
            }
        })
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            holder.bind(feeds.elementAt(position))
        }

    }

    override fun getItemCount(): Int {
        return feeds.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        if (feeds.elementAtOrNull(position) == null) {
            return LOAD_MORE_TYPE
        }
        return FEED_TYPE
    }
}