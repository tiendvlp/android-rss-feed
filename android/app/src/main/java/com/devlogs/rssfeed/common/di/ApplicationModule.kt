package com.devlogs.rssfeed.common.di

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.devlogs.rssfeed.android_services.DemoDepenedencies
import com.devlogs.rssfeed.application.ApplicationStateManager
import com.devlogs.rssfeed.authentication.GetLoggedInUserUseCaseSync
import com.devlogs.rssfeed.rss.RssUrlFinder
import com.devlogs.rssfeed.rss_parser.RssParser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApplicationModule {
    @Provides
    @Singleton
    fun provideSharedPreference(appContext: Application): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(appContext)
    }

    @Provides
    @Singleton
    fun provideApplicationStateManager (getLoggedInUserUseCaseSync: GetLoggedInUserUseCaseSync, sharedPreferences: SharedPreferences): ApplicationStateManager {
        return ApplicationStateManager(getLoggedInUserUseCaseSync, sharedPreferences)
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestoreInstance (): FirebaseFirestore {
        return Firebase.firestore
    }

    @Provides
    @Singleton
    fun provideDemoDependency () : DemoDepenedencies {
        return DemoDepenedencies()
    }

    @Provides
    @Singleton
    fun provideFirebaseMessaging () : FirebaseMessaging {
        return FirebaseMessaging.getInstance()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient () : OkHttpClient {
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(11, TimeUnit.SECONDS)
            .build()

        return client
    }

    @Provides
    @Singleton
    fun provideRssUrlFinder (httpClient: OkHttpClient) : RssUrlFinder {
        return RssUrlFinder(httpClient)
    }

    @Provides
    @Singleton
    fun provideRssParser (httpClient: OkHttpClient) : RssParser {
        return RssParser(httpClient)
    }
}