package com.devlogs.rssfeed.authentication

class LoginRule {

    fun getValidTime () : Long {
        // the login is valid in 2 month
        return 1000 * 60 * 60 * 24 * 60
    }

}