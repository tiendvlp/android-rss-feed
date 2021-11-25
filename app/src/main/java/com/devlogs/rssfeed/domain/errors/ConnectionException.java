package com.devlogs.rssfeed.domain.errors;

public class ConnectionException extends Exception {
    public ConnectionException(String message){
        super(message);
    }
}
