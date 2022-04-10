package com.devlogs.rssfeed.categories

import android.util.Log
import com.devlogs.rssfeed.authentication.GetLoggedInUserUseCaseSync
import com.devlogs.rssfeed.common.background_dispatcher.BackgroundDispatcher
import com.devlogs.rssfeed.domain.entities.CategoryEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.RuntimeException
import javax.inject.Inject

class GetFeedCategoriesUseCaseSync @Inject constructor (val fireStore: FirebaseFirestore, val getLoggedInUserUseCaseSync: GetLoggedInUserUseCaseSync) {

    sealed class Result {
        data class Success (val categories: List<CategoryEntity>) : Result ()
        class UnAuthorized : Result ()
        class GeneralError (val errorMessage: String) : Result ()
    }


    suspend fun executes (feedId: String) : Result = withContext(BackgroundDispatcher) {
        val getUserResult = getLoggedInUserUseCaseSync.executes()

        if (getUserResult is GetLoggedInUserUseCaseSync.Result.InValidLogin) {
            return@withContext Result.UnAuthorized()
        }

        if (getUserResult is GetLoggedInUserUseCaseSync.Result.Success) {
            val categories = ArrayList <CategoryEntity>()
            val snapShot = fireStore.collection("Users")
                .document(getUserResult.user.email)
                .collection("Categories")
                .get().await()

            snapShot.documents.forEach { category ->
                val snapShot = fireStore.collection("Users")
                    .document(getUserResult.user.email)
                    .collection("Categories")
                    .document(category.id)
                    .collection("Feeds")
                    .get().await()

                snapShot.forEach {
                    if (it.id.equals(feedId)) {
                        Log.d("GetFeedCategories", category.id + "contains ${it.id}")
                        categories.add(CategoryEntity(category.id))
                    }
                }
            }

            return@withContext Result.Success(categories)
        }

        throw RuntimeException("Unhandled usecase result")
    }
}