package com.devlogs.rssfeed.screens.common

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager

class RssLinearLayoutManager(context: Context) : LinearLayoutManager(context) {

    override fun supportsPredictiveItemAnimations(): Boolean {
        return false
    }
}