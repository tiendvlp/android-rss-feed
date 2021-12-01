package com.devlogs.rssfeed.categories

import com.devlogs.rssfeed.authentication.GetLoggedInUserUseCaseSync
import com.devlogs.rssfeed.common.background_dispatcher.BackgroundDispatcher
import com.devlogs.rssfeed.domain.entities.CategoryEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.lang.RuntimeException
import javax.inject.Inject

class CreateCategoryUseCaseSync @Inject constructor(private val getLoggedInUserUseCaseSync: GetLoggedInUserUseCaseSync, private val fireStore: FirebaseFirestore){

    sealed class Result {
        class Success (createdCategory : CategoryEntity) : Result()
        class AlreadyExist () : Result()
        class UnAuthorized () : Result ()
        class GeneralError (errorMessage: String?) : Result ()
    }


    suspend fun executes (title: String) : Result = withContext(BackgroundDispatcher) {

        val getUserResult = getLoggedInUserUseCaseSync.executes()

        if (getUserResult is GetLoggedInUserUseCaseSync.Result.InValidLogin) {
            return@withContext Result.UnAuthorized()
        }

        if (getUserResult is GetLoggedInUserUseCaseSync.Result.Success) {
            try {
                val docRef = fireStore
                .collection("Users")
                .document(getUserResult.user.email)
                .collection("Categories").document(title)

                if (docRef.get().await().exists()) {
                    return@withContext Result.AlreadyExist()
                }

                val newCategory = CategoryEntity(title)
                docRef.set(newCategory).await()
                return@withContext Result.Success(newCategory)
            } catch (ex: Exception) {
                return@withContext Result.GeneralError(ex.message)
            }
        }

        throw RuntimeException("UnHandle result (GetLoggedInUserUseCaseSync)")

    }

}