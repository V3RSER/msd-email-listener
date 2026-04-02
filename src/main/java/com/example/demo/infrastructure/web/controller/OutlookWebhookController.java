package com.example.demo.infrastructure.web.controller;

import com.example.demo.application.usecase.ProcessNewEmailUseCase;
import com.example.demo.infrastructure.web.dto.OutlookNotification;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Slf4j
public class OutlookWebhookController {

    private static final Pattern USER_ID_PATTERN = Pattern.compile("users\\('([^']+)'.*");

    private final ProcessNewEmailUseCase processNewEmailUseCase;

    @PostMapping("/outlook")
    public Mono<ResponseEntity<Object>> handleOutlookNotification(
            @Valid @RequestBody(required = false) OutlookNotification notification,
            @RequestParam(required = false) String validationToken) {

        if (validationToken != null && !validationToken.isEmpty()) {
            return handleValidationRequest(validationToken);
        }

        return processNotifications(notification)
                .then(Mono.just(ResponseEntity.accepted().build()))
                .doOnSuccess(response -> log.info("Completed processing of all Outlook notifications."));
    }

    private Mono<ResponseEntity<Object>> handleValidationRequest(String validationToken) {
        log.info("Responding to Outlook validation request with token");
        return Mono.just(ResponseEntity.ok().header("Content-Type", "text/plain").body(validationToken));
    }

    private Mono<Void> processNotifications(OutlookNotification notification) {
        log.info("Received Outlook notification");
        if (notification == null || notification.getValue() == null || notification.getValue().isEmpty()) {
            log.warn("Outlook notification is null or has no value");
            return Mono.empty();
        }

        return Flux.fromIterable(notification.getValue())
                .doOnSubscribe(subscription -> log.info("Subscribed to Outlook notification processing flow"))
                .flatMap(this::processSingleNotification)
                .then();
    }

    private Mono<Void> processSingleNotification(OutlookNotification.Value value) {
        if (!"created".equals(value.getChangeType())) {
            log.debug("Skipping notification with change type: {}", value.getChangeType());
            return Mono.empty();
        }

        String messageId = value.getResourceData().getId();
        log.info("Processing 'created' notification for message ID: {}", messageId);

        return Mono.justOrEmpty(extractUserIdFromOdataId(value.getResourceData().getOdataId()))
                .flatMap(userId -> processNewEmailUseCase.processNewEmail(userId, messageId))
                .doOnError(error -> log.error("Error processing message {}", messageId, error))
                .onErrorResume(e -> Mono.empty()) // Continue with the next notification
                .switchIfEmpty(Mono.fromRunnable(() -> log.warn("Could not extract user ID for message: {}. OData ID: {}", messageId, value.getResourceData().getOdataId())))
                .then();
    }

    private Optional<String> extractUserIdFromOdataId(String odataId) {
        if (odataId == null || odataId.isEmpty()) {
            log.warn("OData ID is null or empty.");
            return Optional.empty();
        }
        log.debug("Attempting to extract user ID from OData ID: {}", odataId);
        Matcher matcher = USER_ID_PATTERN.matcher(odataId);
        if (matcher.find()) {
            String userId = matcher.group(1);
            log.info("Successfully extracted user ID '{}' from OData ID", userId);
            return Optional.of(userId);
        }
        log.warn("Could not find user ID in OData ID using the configured pattern.");
        return Optional.empty();
    }
}
