package com.devlogs.rssfeed.screens.ReadFeeds.controller

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.devlogs.rssfeed.R

class ReadFeedsFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.layout_read_feeds, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ReadFeedsFragment()
    }
}