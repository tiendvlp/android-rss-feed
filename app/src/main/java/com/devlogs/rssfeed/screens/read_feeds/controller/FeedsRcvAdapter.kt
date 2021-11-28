package com.devlogs.rssfeed.screens.read_feeds.controller

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.devlogs.rssfeed.R
import com.devlogs.rssfeed.screens.read_feeds.presentable_model.FeedPresentableModel
import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter
import org.sufficientlysecure.htmltextview.HtmlTextView
import java.util.*

class FeedsRcvAdapter : RecyclerView.Adapter<FeedsRcvAdapter.ViewHolder> {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val txtAuthor : TextView = view.findViewById(R.id.txtAuthor)
        private val txtPubDate: TextView = view.findViewById(R.id.txtPubDate)
        private val txtTitle: TextView = view.findViewById(R.id.txtTitle)
        private val img : ImageView = view.findViewById(R.id.img)
        private val imgHtml : HtmlTextView = view.findViewById(R.id.imgHtml)

        fun bind (feed: FeedPresentableModel) {
            Log.d("FeedsViewHolder", "Binding: ${feed.title}")
            txtAuthor.text = feed.author + "at " + feed.channelTitle
            txtPubDate.text = feed.pubDateInString
            txtTitle.text = feed.title
            if (feed.imageUrl != null) {
                Glide
                    .with(itemView.context)
                    .load(feed.imageUrl)
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
                            return true
                        }

                    })
                    .into(img)
            }
        }
    }

    private val feeds: TreeSet<FeedPresentableModel>

    constructor(feeds: TreeSet<FeedPresentableModel>) {
        this.feeds = feeds;
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_feed, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(feeds.elementAt(position))
    }

    override fun getItemCount(): Int {
        return feeds.size
    }
}