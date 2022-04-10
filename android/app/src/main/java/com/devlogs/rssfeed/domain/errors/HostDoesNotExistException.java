package com.devlogs.rssfeed.domain.errors;

public class HostDoesNotExistException extends Exception {
    public HostDoesNotExistException(String message) {
        super(message);
    }
}
