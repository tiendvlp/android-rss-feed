package com.devlogs.rssfeed.common.di

import android.app.Activity
import android.view.LayoutInflater
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.devlogs.rssfeed.screens.common.mvcview.MvcViewFactory
import com.devlogs.rssfeed.screens.common.mvcview.UIToolkit
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
class ActivityModule {
    @Provides
    fun provideLayoutInflater (activity: Activity): LayoutInflater {
        return LayoutInflater.from(activity)
    }

    @Provides
    fun provideFragmentManager (activity: Activity): FragmentManager {
        return (activity as FragmentActivity).supportFragmentManager
    }

    @Provides
    fun provideUIToolkit (activity: Activity, inflater: LayoutInflater) : UIToolkit {
        return UIToolkit(activity.window, activity, inflater)
    }

    @Provides
    fun provideMvcViewFactory (toolKit: UIToolkit): MvcViewFactory {
        return MvcViewFactory(toolKit)
    }
}