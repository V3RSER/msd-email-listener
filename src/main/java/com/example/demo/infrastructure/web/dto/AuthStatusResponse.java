package com.example.demo.infrastructure.web.dto;

public record AuthStatusResponse(String userId, boolean connected, String message) {
}
