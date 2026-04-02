package com.example.demo.infrastructure.web.controller;

import com.example.demo.application.usecase.ProcessNewEmailUseCase;
import com.example.demo.infrastructure.web.dto.OutlookNotification;
import com.example.demo.infrastructure.web.dto.OutlookValidationResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/v1/webhooks")
public class OutlookWebhookController {

    private static final Logger log = LoggerFactory.getLogger(OutlookWebhookController.class);
    // Fixed illegal escape character
    private static final Pattern USER_ID_PATTERN = Pattern.compile("users\\('([^']+)'\\)");

    private final ProcessNewEmailUseCase processNewEmailUseCase;

    public OutlookWebhookController(ProcessNewEmailUseCase processNewEmailUseCase) {
        this.processNewEmailUseCase = processNewEmailUseCase;
    }

    @PostMapping("/outlook")
    public Mono<ResponseEntity<Object>> handleOutlookNotification(
            @Valid @RequestBody OutlookNotification notification,
            @RequestParam(required = false) String validationToken) {

        if (validationToken != null && !validationToken.isEmpty()) {
            log.info("Responding to Outlook validation request");
            return Mono.just(ResponseEntity.ok().header("Content-Type", "text/plain").body(validationToken));
        }

        log.info("Received Outlook notification");
        
        if (notification == null || notification.getValue() == null) {
            return Mono.just(ResponseEntity.accepted().build());
        }

        return Flux.fromIterable(notification.getValue())
                .filter(v -> "created".equals(v.getChangeType()))
                .flatMap(v -> {
                    String messageId = v.getResourceData().getId();
                    return Mono.justOrEmpty(extractUserIdFromOdataId(v.getResourceData().getOdataId()))
                            .doOnNext(userId -> log.info("Processing new email for user {} and message {}", userId, messageId))
                            .flatMap(userId -> processNewEmailUseCase.processNewEmail(userId, messageId))
                            .doOnError(error -> log.error("Failed to process message {}", messageId, error))
                            .onErrorResume(e -> Mono.empty()) // Don't fail the whole batch if one fails
                            .switchIfEmpty(Mono.fromRunnable(() -> log.warn("Could not extract user ID from OData ID: {}", v.getResourceData().getOdataId())));
                })
                .then(Mono.just(ResponseEntity.accepted().build()));
    }

    private Optional<String> extractUserIdFromOdataId(String odataId) {
        if (odataId == null || odataId.isEmpty()) {
            return Optional.empty();
        }
        Matcher matcher = USER_ID_PATTERN.matcher(odataId);
        if (matcher.find()) {
            return Optional.of(matcher.group(1));
        }
        return Optional.empty();
    }
}
