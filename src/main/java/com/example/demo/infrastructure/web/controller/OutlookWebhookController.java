package com.example.demo.infrastructure.web.controller;

import com.example.demo.application.usecase.ProcessNewEmailUseCase;
import com.example.demo.infrastructure.web.dto.OutlookNotification;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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

    @PostMapping(value = "/outlook", produces = "text/plain")
    public Mono<ResponseEntity<String>> handleOutlookWebhook(
            @RequestBody(required = false) @Valid OutlookNotification notification,
            @RequestParam(required = false) String validationToken) {

        if (validationToken != null) {
            log.info("Responding to Outlook validation request with token");
            return Mono.just(ResponseEntity.ok().body(validationToken));
        }

        if (notification != null) {
            log.info("Received Outlook notification, processing asynchronously.");
            processNotifications(notification)
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe(
                            null, // onNext is not applicable for Mono<Void>
                            error -> log.error("Error processing outlook notification", error),
                            () -> log.info("Successfully processed outlook notification")
                    );

            return Mono.just(ResponseEntity.accepted().body(""));
        }

        log.warn("Received an empty request for Outlook webhook.");
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Request body is missing."));
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