package com.example.demo.infrastructure.web.dto;

public class OutlookValidationResponse {
    private final String validationToken;

    public OutlookValidationResponse(String validationToken) {
        this.validationToken = validationToken;
    }

    public String getValidationToken() {
        return validationToken;
    }
}
