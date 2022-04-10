package com.devlogs.rssfeed.authentication

import javax.inject.Inject

class LoginRule {

    @Inject
    constructor() {

    }

    fun getValidTime () : Long {
        // the login is valid in 2 month
        return 1000 * 60 * 60 * 24 * 60
    }

}