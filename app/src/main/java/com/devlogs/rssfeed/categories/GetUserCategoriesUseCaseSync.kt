package com.devlogs.rssfeed.categories

import com.devlogs.rssfeed.authentication.GetLoggedInUserUseCaseSync
import com.devlogs.rssfeed.common.background_dispatcher.BackgroundDispatcher
import com.devlogs.rssfeed.domain.entities.CategoryEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.RuntimeException
import javax.inject.Inject

class GetUserCategoriesUseCaseSync @Inject constructor (private val getLoggedInUserUseCaseSync: GetLoggedInUserUseCaseSync, private val fireStore: FirebaseFirestore){

    sealed class Result {
        data class Success (val categories: Set<CategoryEntity>) : Result()
        class UnAuthorized() : Result()
        class GeneralError (errorMessage: String) : Result()
    }


    suspend fun executes () : Result = withContext(BackgroundDispatcher) {
        val getUserResult = getLoggedInUserUseCaseSync.executes()

        if (getUserResult is GetLoggedInUserUseCaseSync.Result.InValidLogin) {
            return@withContext Result.UnAuthorized()
        }

        if (getUserResult is GetLoggedInUserUseCaseSync.Result.Success) {

            val snapShot = fireStore.collection("Users")
                .document(getUserResult.user.email)
                .collection("Categories")
                .get().await()

            val categories = HashSet<CategoryEntity> ()

            if (!snapShot.isEmpty) {
                snapShot.documents.forEach { doc ->
                    categories.add(
                        CategoryEntity(
                        doc.getString("title")!!,
                        )
                    )
                }
            }

            return@withContext Result.Success(categories)

        }

        throw RuntimeException("UnHandle result (GetLoggedInUserUseCaseSync)")
    }
}