package com.devlogs.rssfeed.domain.ports

import com.devlogs.rssfeed.domain.entities.UserEntity
import com.devlogs.rssfeed.domain.errors.*

interface UserRepository {
    /**
     * @throws ConnectionException when the there is a connecting problems
     * */
    fun getUserByEmail (email: String) : UserEntity?
    /**
     * @throws ConnectionException when the there is a connecting problems
     * */
    fun addUser (email: String, name: String, avatarUrl: String)
}