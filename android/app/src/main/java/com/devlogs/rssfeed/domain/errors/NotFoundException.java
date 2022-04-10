package com.devlogs.rssfeed.domain.errors;

public class NotFoundException extends Exception {
    public NotFoundException (String message){
        super(message);
    }
}
