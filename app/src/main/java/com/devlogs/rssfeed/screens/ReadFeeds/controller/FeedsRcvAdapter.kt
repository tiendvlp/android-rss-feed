package com.devlogs.rssfeed.screens.ReadFeeds.controller

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.devlogs.rssfeed.R
import com.devlogs.rssfeed.screens.ReadFeeds.presentable_model.FeedPresentableModel

class FeedsRcvAdapter : RecyclerView.Adapter<FeedsRcvAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val txtAuthor : TextView = view.findViewById(R.id.txtAuthor)
        private val txtPubDate: TextView = view.findViewById(R.id.txtPubDate)
        private val txtTitle: TextView = view.findViewById(R.id.txtTitle)
        private val img : ImageView = view.findViewById(R.id.img)

        fun bind (feed: FeedPresentableModel) {
            txtAuthor.text = feed.author
            txtPubDate.text = feed.pubDateInString
            txtTitle.text = feed.title
            //Glide.with(itemView.context).load(feed.)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }
}