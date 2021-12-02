package com.devlogs.rssfeed.feeds

import com.devlogs.rssfeed.authentication.GetLoggedInUserUseCaseSync
import com.devlogs.rssfeed.common.background_dispatcher.BackgroundDispatcher
import com.devlogs.rssfeed.domain.entities.FeedEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.lang.RuntimeException
import javax.inject.Inject

class GetFeedsByCategoryUseCaseSync @Inject constructor(private val fireStore: FirebaseFirestore, private val getLoggedInUserUseCaseSync: GetLoggedInUserUseCaseSync){

    sealed class Result {
        data class Success (val feeds: Set<FeedEntity>) : Result()
        class UnAuthorized () : Result()
        data class GeneralError (val message: String?) : Result()
    }


    suspend fun executes (categoryTitle: String, count: Long) : Result = withContext(BackgroundDispatcher) {

        val getUserResult = getLoggedInUserUseCaseSync.executes()

        if (getUserResult is GetLoggedInUserUseCaseSync.Result.InValidLogin) {
            return@withContext Result.UnAuthorized()
        }

        if (getUserResult is GetLoggedInUserUseCaseSync.Result.Success) {
            try {
                val snapShot = fireStore.collection("Users")
                    .document(getUserResult.user.email)
                    .collection("Categories")
                    .document(categoryTitle)
                    .collection("Feeds")
                    .limit(count)
                    .get().await()

                val feedIds = ArrayList<String>()

                if (snapShot.isEmpty) {
                    return@withContext Result.Success(emptySet())
                }

                snapShot.documents.forEach { doc ->
                    feedIds.add(doc.id)
                }

                val feeds = HashSet<FeedEntity>()

                val feedSnapShot = fireStore.collection("Feeds")
                    .whereIn("id", feedIds)
                    .get().await()

                if (feedSnapShot.isEmpty) {
                    return@withContext Result.Success(emptySet())
                }

                feedSnapShot.documents.forEach { doc ->
                    feeds.add(
                        FeedEntity(
                            doc.getString("id")!!,
                            doc.getString("rssChannelId")!!,
                            doc.getString("channelTitle")!!,
                            doc.getString("title")!!,
                            doc.getString("description")!!,
                            doc.getLong("pubDate")!!,
                            doc.getString("url")!!,
                            doc.getString("author")!!,
                            doc.getString("content")!!,
                            doc.getString("imageUrl")!!
                        )
                    )
                }

                return@withContext Result.Success(feeds)
            } catch (ex: Exception) {
                return@withContext Result.GeneralError(ex.message)
            }
        }

        throw RuntimeException("Unhandled Result")

    }
}