package com.devlogs.rssfeed.users

import com.devlogs.rssfeed.common.background_dispatcher.BackgroundDispatcher
import com.devlogs.rssfeed.domain.entities.UserEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AddUserUseCaseSync @Inject constructor(private val fireStore: FirebaseFirestore) {

    sealed class Result {
        data class Success (val addedUser: UserEntity) : Result()
        data class GeneralError (val errorMessage: String?) : Result()
    }

    suspend fun executes (email: String, name: String, avatarUrl: String?) : Result = withContext(BackgroundDispatcher) {
        val addedUser = UserEntity (email, name, avatarUrl)
        try {
            fireStore.collection("Users").document(email).set(addedUser).await()
            Result.Success(addedUser)
        } catch (e: Exception) {
            Result.GeneralError(e.message)
        }
    }
}