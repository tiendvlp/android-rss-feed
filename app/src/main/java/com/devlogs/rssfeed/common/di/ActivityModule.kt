package com.devlogs.rssfeed.common.di

import android.app.Activity
import android.view.LayoutInflater
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.devlogs.rssfeed.R
import com.devlogs.rssfeed.common.shared_context.AppConfig.DaggerNamed.ACTIVITY_SCOPE
import com.devlogs.rssfeed.screens.common.mvcview.MvcViewFactory
import com.devlogs.rssfeed.screens.common.mvcview.UIToolkit
import com.devlogs.rssfeed.screens.common.presentation_state.PresentationStateManager
import com.devlogs.rssfeed.screens.main.MainScreenNavigator
import com.ncapdevi.fragnav.FragNavController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Named

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
    @ActivityScoped
    @Named(ACTIVITY_SCOPE)
    fun providePresentationStateManager () : PresentationStateManager {
        return PresentationStateManager()
    }

    @Provides
    fun provideMvcViewFactory (toolKit: UIToolkit): MvcViewFactory {
        return MvcViewFactory(toolKit)
    }

    private fun getMainFragNavController (fragmentManager: FragmentManager) : FragNavController {
        return FragNavController(fragmentManager, R.id.mainLayoutContainer)
    }

    @Provides
    fun provideMainScreenNavigator (fragmentManager: FragmentManager) : MainScreenNavigator {
        return MainScreenNavigator(getMainFragNavController(fragmentManager))
    }
}