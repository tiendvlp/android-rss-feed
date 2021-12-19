package com.devlogs.rssfeed.screens.category_feeds.controllers

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.devlogs.rssfeed.R
import com.devlogs.rssfeed.common.helper.LogTarget
import com.devlogs.rssfeed.common.helper.normalLog
import com.devlogs.rssfeed.screens.category_feeds.presentable_model.FeedPresentableModel
import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter
import org.sufficientlysecure.htmltextview.HtmlTextView
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
            txtAuthor.text = if (feed.author.isEmpty()) feed.channelTitle else "${feed.author} at ${feed.channelTitle}"
            txtPubDate.text = feed.pubDateInString
            txtTitle.text = feed.title
            btnSaved.visibility = View.GONE
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
                            imgHtml.setHtml(
                                "<img src=\"${feed.imageUrl}\">",
                                HtmlHttpImageGetter(imgHtml, "", true)
                            )
                            return true
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
                onFeedClicked?.invoke(feed)
            }
        }
    }

    private val feeds: TreeSet<FeedPresentableModel>

    var onFeedClicked: ((selectedChannel: FeedPresentableModel) -> Unit)? = null

    constructor(feeds: TreeSet<FeedPresentableModel>) {
        this.feeds = feeds;
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
            val itemView = inflater.inflate(R.layout.item_feed, parent, false)
            return ViewHolder(itemView)
    }



    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            holder.bind(feeds.elementAt(position))
        }

    }

    override fun getItemCount(): Int {
        return feeds.size
    }

}