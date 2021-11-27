package com.devlogs.rssfeed.screens.add_rss_channel.controller

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.devlogs.rssfeed.R

class AddRssChannelFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Toast.makeText(requireContext(), "OnCreate AddRss", Toast.LENGTH_LONG).show()
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.layout_add_rss_channel, container, false)
    }

    companion object {

        @JvmStatic
        fun newInstance() = AddRssChannelFragment()
    }
}