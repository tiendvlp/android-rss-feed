package com.devlogs.rssfeed.android_services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.devlogs.rssfeed.common.base.Observable
import com.devlogs.rssfeed.domain.entities.FeedEntity
import com.devlogs.rssfeed.rss_channels.GetUserRssChannelsUseCaseSync
import com.google.firebase.firestore.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.RuntimeException
import javax.inject.Inject

@AndroidEntryPoint
class RssChannelTrackingService : Service() {
    interface Listener {
        fun onNewFeed(feed: FeedEntity)
    }

    private inner class ListenerManager : Observable <Listener>, EventListener<QuerySnapshot> {
        private val listeners = HashSet<Listener>()
        private val registration: ListenerRegistration
        private val fireStore: FirebaseFirestore

        val isEmpty: Boolean; get() {
            return listeners.isEmpty()
        }
        constructor(channelId: String, fireStore: FirebaseFirestore) {
            this.fireStore = fireStore
            registration = fireStore
                .collection("Feeds")
                .whereEqualTo("rssChannelId", channelId)
                .orderBy("pubDate", Query.Direction.DESCENDING)
                .addSnapshotListener (this)
            Log.d("RssChannelTracking", "Query: rssChannelId = ${channelId}")
        }

        constructor(channelIds: Set<String>, fireStore: FirebaseFirestore) {
            this.fireStore = fireStore
            registration = fireStore
                .collection("Feeds")
                .whereIn("rssChannelId", channelIds.toList())
                .orderBy("pubDate", Query.Direction.DESCENDING)
                .addSnapshotListener (this)

            channelIds.forEach {
                Log.d("RssChannelTracking", "Query: rssChannelId = ${it}")
            }
        }

        override fun register (listener: Listener) {
            listeners.add(listener)
        }

        override fun unRegister (listener: Listener) {
            listeners.remove(listener)
            if (listeners.isEmpty()) {
                registration.remove()
            }
        }

        override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
            if (error != null) {
                error.message?.let { Log.e("RssChannelTracking", it) }
            }

            // If it from cache that means, it's the old change
            else if (value!!.documentChanges.isNotEmpty() && !value.metadata.isFromCache) {
                Log.d("RssChannelTracking", "Number of doc changed: " + value.documentChanges.size)
                listeners.forEach {listener ->
                    value.documentChanges.forEach {doc->
                        listener.onNewFeed(fireStoreDocToFeedEntity(doc.document))
                    }
                }
            } else {
                Log.d("RssChannelTracking", "Unchanged: ")
            }

        }

        private fun fireStoreDocToFeedEntity (doc: DocumentSnapshot) : FeedEntity = FeedEntity(
            doc.getString("id")!!,
            doc.getString("rssChannelId")!!,
            doc.getString("channelTitle")!!,
            doc.getString("title")!!,
            doc.getString("description")!!,
            doc.getLong("pubDate")!!,
            doc.getString("url")!!,
            doc.getString("author")!!,
            doc.getString("content")!!
        )
    }

    inner class LocalBinder : Binder() {
        val service = this@RssChannelTrackingService
    }

    companion object {
        fun bind(
            context: Context,
            connection: ServiceConnection,
            flag: Int = Context.BIND_AUTO_CREATE
        ) {
            val intent = Intent(context, RssChannelTrackingService::class.java)
            context.bindService(intent, connection, flag)
        }
    }

    @Inject
    protected lateinit var getUserRssChannelsUseCaseSync: GetUserRssChannelsUseCaseSync
    @Inject
    protected lateinit var fireStore: FirebaseFirestore

    private val coroutine = CoroutineScope(Dispatchers.Main.immediate)
    private val observers = HashMap<String, ListenerManager>()

    override fun onBind(intent: Intent?): IBinder? {
        return LocalBinder()
    }

    fun register(channel:String, listener: Listener) {
        val entry: ListenerManager
        if (observers.containsKey(channel)) {
            entry = observers[channel]!!
        } else {
            entry = ListenerManager (channel, fireStore)
            observers[channel] = entry
        }
        entry.register(listener)
    }

    fun registerAllChannel (listener: Listener) {
        coroutine.launch {
            val entry: ListenerManager
            if (observers.containsKey("All")) {
                entry = observers["All"]!!
            } else {
                val result = getUserRssChannelsUseCaseSync.executes()
                if (result is GetUserRssChannelsUseCaseSync.Result.Success) {
                    entry = ListenerManager (result.channels.map { it.id }.toSet(), fireStore)
                } else {
                    throw RuntimeException(result.javaClass.simpleName)
                }
                observers["All"] = entry
            }
            entry.register(listener)
        }
    }

    fun unRegister(channel: String, listener: Listener) {
        if (observers.containsKey(channel)) {
            val entry = observers[channel]!!
            entry.unRegister(listener)
            if (entry.isEmpty) {
                observers.remove(channel)
            }
        }
    }
}