package com.example.demo.application.controller.dto;

public class AuthStatusResponse {
    private final String userId;
    private final boolean connected;
    private final String message;

    public AuthStatusResponse(String userId, boolean connected, String message) {
        this.userId = userId;
        this.connected = connected;
        this.message = message;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isConnected() {
        return connected;
    }

    public String getMessage() {
        return message;
    }
}
