package com.devlogs.rssfeed.common.di

import com.devlogs.rssfeed.common.shared_context.AppConfig.DaggerNamed.FRAGMENT_SCOPE
import com.devlogs.rssfeed.screens.common.presentation_state.PresentationStateManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.scopes.FragmentScoped
import javax.inject.Named

@Module
@InstallIn(FragmentComponent::class)
class FragmentModule {

    @Provides
    @FragmentScoped
    @Named(FRAGMENT_SCOPE)
    fun providePresentationStateManager () : PresentationStateManager {
        return PresentationStateManager()
    }

}