package com.devlogs.rssfeed.screens.navigation_drawer.controller

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devlogs.rssfeed.R
import com.devlogs.rssfeed.screens.navigation_drawer.presentable_model.ChannelPresentableModel

class ChannelRcvAdapter : RecyclerView.Adapter<ChannelRcvAdapter.ViewHolder> {

    class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        private val img: ImageView = view.findViewById(R.id.img)
        private val txtTitle: TextView = view.findViewById(R.id.txtTitle)

        fun bind (channel: ChannelPresentableModel) {

        }
    }

    private val channels: List<ChannelPresentableModel>

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