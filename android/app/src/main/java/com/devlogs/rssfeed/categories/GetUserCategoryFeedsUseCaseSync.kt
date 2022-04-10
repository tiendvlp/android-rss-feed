package com.devlogs.rssfeed.categories

import com.devlogs.rssfeed.authentication.GetLoggedInUserUseCaseSync
import com.devlogs.rssfeed.common.background_dispatcher.BackgroundDispatcher
import com.devlogs.rssfeed.domain.entities.FeedEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.lang.RuntimeException
import javax.inject.Inject

class GetUserCategoryFeedsUseCaseSync  @Inject constructor(private val getLoggedInUserUseCaseSync: GetLoggedInUserUseCaseSync, private val fireStore: FirebaseFirestore) {

    sealed class Result {
        class Success (feeds: Set<FeedEntity>) : Result()
        class UnAuthorized () : Result ()
        class GeneralError (errorMessage: String?) : Result ()
    }


    suspend fun executes (categoryId: String, feedId: String) : Result = withContext(BackgroundDispatcher) {

        val getUserResult = getLoggedInUserUseCaseSync.executes()

        if (getUserResult is GetLoggedInUserUseCaseSync.Result.InValidLogin) {
            return@withContext Result.UnAuthorized()
        }

        if (getUserResult is GetLoggedInUserUseCaseSync.Result.Success) {
            try {
                val snapshot = fireStore.collection("Users")
                    .document(getUserResult.user.email)
                    .collection("Categories")
                    .document(categoryId)
                    .collection("Feeds")
                    .get().await()

                val feeds = HashSet<FeedEntity> ()

                if (!snapshot.isEmpty) {
                    snapshot.documents.forEach { categoryFeed ->
                        val doc = fireStore.collection("Feeds")
                                        .document(categoryFeed.getString("id")!!)
                                        .get().await()

                        feeds.add(FeedEntity(
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
                        ))
                    }

                    return@withContext Result.Success(feeds)
                }

            } catch (ex: Exception) {
                return@withContext Result.GeneralError(ex.message)
            }
        }

        throw RuntimeException("UnHandle Result")
    }
}