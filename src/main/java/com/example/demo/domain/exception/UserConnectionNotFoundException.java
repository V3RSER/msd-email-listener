package com.example.demo.domain.exception;

public class UserConnectionNotFoundException extends RuntimeException {

    public UserConnectionNotFoundException(String userId) {
        super("User connection not found for user: " + userId);
    }
}
