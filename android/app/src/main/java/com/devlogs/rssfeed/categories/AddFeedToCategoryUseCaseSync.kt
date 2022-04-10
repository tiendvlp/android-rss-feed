package com.devlogs.rssfeed.categories

import com.devlogs.rssfeed.authentication.GetLoggedInUserUseCaseSync
import com.devlogs.rssfeed.common.background_dispatcher.BackgroundDispatcher
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.lang.RuntimeException
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class AddFeedToCategoryUseCaseSync  @Inject constructor(private val getLoggedInUserUseCaseSync: GetLoggedInUserUseCaseSync, private val fireStore: FirebaseFirestore) {

    sealed class Result {
        class Success () : Result()
        class AlreadyExist (categoryTitles : List<String>) : Result()
        class UnAuthorized () : Result ()
        class GeneralError (errorMessage: String?) : Result ()
    }


    suspend fun executes (categoryTitles: Set<String>, feedId: String) : Result = withContext(BackgroundDispatcher) {

        val getUserResult = getLoggedInUserUseCaseSync.executes()

        if (getUserResult is GetLoggedInUserUseCaseSync.Result.InValidLogin) {
            return@withContext Result.UnAuthorized()
        }

        if (getUserResult is GetLoggedInUserUseCaseSync.Result.Success) {
            val alreadyExistsCategoryTitles = ArrayList<String>()
                try {
                    categoryTitles.forEach { categoryTitle ->
                        val doc = fireStore.collection("Users")
                            .document(getUserResult.user.email)
                            .collection("Categories")
                            .document(categoryTitle)
                            .collection("Feeds").document(feedId)

                        if (doc.get().await().exists()) {
                            alreadyExistsCategoryTitles.add(categoryTitle)
                        }
                        doc.set(
                            hashMapOf(
                                "id" to feedId
                            )
                        ).await()
                }
                    if (alreadyExistsCategoryTitles.isNotEmpty()) {
                        return@withContext Result.AlreadyExist(alreadyExistsCategoryTitles)
                    }
                    return@withContext Result.Success()
            } catch (ex: Exception) {
                return@withContext Result.GeneralError(ex.message)
            }
        }

        throw RuntimeException("UnHandle Result")
    }
}