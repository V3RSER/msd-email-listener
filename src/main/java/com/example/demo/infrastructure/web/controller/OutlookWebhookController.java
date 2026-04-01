package com.example.demo.infrastructure.web.controller;

import com.example.demo.application.usecase.ProcessNewEmailUseCase;
import com.example.demo.infrastructure.web.dto.OutlookNotification;
import com.example.demo.infrastructure.web.dto.OutlookValidationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhooks")
public class OutlookWebhookController {

    private final ProcessNewEmailUseCase processNewEmailUseCase;

    public OutlookWebhookController(ProcessNewEmailUseCase processNewEmailUseCase) {
        this.processNewEmailUseCase = processNewEmailUseCase;
    }

    @PostMapping("/outlook")
    public ResponseEntity<OutlookValidationResponse> handleOutlookNotification(
            @RequestBody OutlookNotification notification,
            @RequestParam(required = false) String validationToken) {

        // 1. Microsoft Graph Webhook validation
        if (validationToken != null && !validationToken.isEmpty()) {
            return ResponseEntity.ok(new OutlookValidationResponse(validationToken));
        }

        // 2. Process the actual notification
        if (notification != null && notification.getValue() != null) {
            notification.getValue().forEach(v -> {
                if ("created".equals(v.getChangeType())) {
                    String messageId = v.getResourceData().getId();
                    String userId = extractUserIdFromOdataId(v.getResourceData().getOdataId());
                    if (userId != null) {
                        processNewEmailUseCase.processNewEmail(userId, messageId);
                    }
                }
            });
        }

        return ResponseEntity.accepted().build();
    }

    private String extractUserIdFromOdataId(String odataId) {
        if (odataId == null || odataId.isEmpty()) {
            return null;
        }
        // e.g., "users('d2a67972-656c-4b53-83d3-9914b807755c')/messages('...')"
        String[] parts = odataId.split("'");
        if (parts.length > 1) {
            return parts[1];
        }
        return null;
    }
}
