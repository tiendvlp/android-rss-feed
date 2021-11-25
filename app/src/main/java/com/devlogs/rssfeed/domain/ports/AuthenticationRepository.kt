package com.devlogs.rssfeed.domain.ports

import com.devlogs.rssfeed.domain.entities.UserEntity
import com.devlogs.rssfeed.domain.errors.ConnectionException

interface AuthenticationRepository {
    /**
     * @throws ConnectionException when the there is a connecting problems
     * */
    fun getCurrentLoggedInUser () : UserEntity
    /**
     * @throws ConnectionException when the there is a connecting problems
     * */
    fun addLoggedInInfo (expiredTime: Long, userData: UserEntity)
    /**
     * Return the expired timestamp in millisecond
     * @throws ConnectionException when the there is a connecting problems
     * */
    fun getExpiredTime() : Long
}