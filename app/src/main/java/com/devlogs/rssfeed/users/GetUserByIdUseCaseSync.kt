package com.devlogs.rssfeed.users

import com.devlogs.rssfeed.common.background_dispatcher.BackgroundDispatcher
import com.devlogs.rssfeed.domain.entities.UserEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetUserByIdUseCaseSync @Inject constructor(private val fireStore: FirebaseFirestore) {

    sealed class Result {
        data class Success(val user: UserEntity) : Result()
        data class GeneralError(val errorMessage: String?) : Result()
        class UserDoesNotExist : Result()
    }

    suspend fun executes(userId: String): Result =
        withContext(
            BackgroundDispatcher
        ) {
            try {
               val document = fireStore.collection("Users").document(userId).get().await()
               if (!document.exists()) {
                   Result.UserDoesNotExist()
               }
                val user = UserEntity ( document["email"].toString(), document["name"].toString(), document["avatar"].toString());
                Result.Success(user)
            } catch (e: Exception) {
                Result.GeneralError(e.message)
            }
        }
}