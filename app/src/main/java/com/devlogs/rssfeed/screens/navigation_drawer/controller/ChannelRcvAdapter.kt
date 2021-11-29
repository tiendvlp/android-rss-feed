package com.devlogs.rssfeed.screens.navigation_drawer.controller

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.devlogs.rssfeed.R
import com.devlogs.rssfeed.screens.navigation_drawer.presentable_model.ChannelPresentableModel

class ChannelRcvAdapter : RecyclerView.Adapter<ChannelRcvAdapter.ViewHolder> {

    inner class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        private val img: ImageView = view.findViewById(R.id.img)
        private val txtTitle: TextView = view.findViewById(R.id.txtTitle)
        private val failedDrawable = view.context.getDrawable(R.drawable.ic_www)
        fun bind (channel: ChannelPresentableModel) {
            Glide.with(itemView.context)
                .load(channel.url)
                .into(img)
                .onLoadFailed(failedDrawable)
            itemView.setOnClickListener {
                onItemClicked?.invoke(channel)
            }
            txtTitle.text = channel.title
        }
    }

    private val channels: List<ChannelPresentableModel>

    var onItemClicked : ((channel: ChannelPresentableModel) -> Unit)? = null

    constructor(channels: List<ChannelPresentableModel>) {
        this.channels = channels
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_nav_channel, parent, false)
        return ViewHolder(itemView)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(channels[position])
    }

    override fun getItemCount(): Int {
        return channels.size
    }


}