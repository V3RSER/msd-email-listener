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

    // Fixed illegal escape character
    private static final Pattern USER_ID_PATTERN = Pattern.compile("users\\('([^']+)'.*");

    private final ProcessNewEmailUseCase processNewEmailUseCase;

    @PostMapping("/outlook")
    public Mono<ResponseEntity<Object>> handleOutlookNotification(
            @Valid @RequestBody(required = false) OutlookNotification notification,
            @RequestParam(required = false) String validationToken) {

        if (validationToken != null && !validationToken.isEmpty()) {
            log.info("Responding to Outlook validation request");
            return Mono.just(ResponseEntity.ok().header("Content-Type", "text/plain").body(validationToken));
        }

        log.info("Received Outlook notification");
        if (notification == null || notification.getValue() == null || notification.getValue().isEmpty()) {
            log.warn("Outlook notification is null or has no value");
            return Mono.just(ResponseEntity.accepted().build());
        }

        return Flux.fromIterable(notification.getValue())
                .doOnNext(value -> log.info("Processing notification value: {}", value))
                .filter(v -> "created".equals(v.getChangeType()))
                .doOnNext(v -> log.info("Filtered for 'created' change type"))
                .flatMap(v -> {
                    String messageId = v.getResourceData().getId();
                    log.info("Processing message with ID: {}", messageId);
                    return Mono.justOrEmpty(extractUserIdFromOdataId(v.getResourceData().getOdataId()))
                            .doOnNext(userId -> log.info("Extracted user ID: {}", userId))
                            .flatMap(userId -> processNewEmailUseCase.processNewEmail(userId, messageId))
                            .doOnError(error -> log.error("Error processing message {}", messageId, error))
                            .onErrorResume(e -> Mono.empty()) // Continue with next notifications
                            .switchIfEmpty(Mono.fromRunnable(() -> log.warn("Could not extract user ID from OData ID: {}", v.getResourceData().getOdataId())));
                })
                .then(Mono.just(ResponseEntity.accepted().build()))
                .doOnSubscribe(subscription -> log.info("Subscribed to Outlook notification processing flow"))
                .doOnSuccess(response -> log.info("Completed processing of all Outlook notifications"));
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
