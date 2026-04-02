package com.example.demo.domain.service;

import com.example.demo.domain.exception.UserConnectionNotFoundException;
import com.example.demo.domain.repository.UserConnectionRepository;
import com.example.demo.infrastructure.client.MicrosoftGraphClient;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.models.Subscription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutlookService {

    private final MicrosoftGraphClient graphClient;
    private final UserConnectionRepository userConnectionRepository;

    @Value("${app.webhook-notification-url}")
    private String notificationUrl;

    public Mono<Message> getEmailContent(String userId, String messageId) {
        log.info("Attempting to retrieve email content for user '{}' and message '{}'", userId, messageId);

        return userConnectionRepository.findByUserId(userId)
                .doOnNext(userConnection -> log.debug("Found user connection for user ID: {}", userId))
                .switchIfEmpty(Mono.error(new UserConnectionNotFoundException(userId)))
                .flatMap(userConnection -> {
                    log.debug("User access token acquired. Requesting message from Microsoft Graph API.");
                    return graphClient.getMessage(userId, messageId, userConnection.getAccessToken());
                })
                .doOnSuccess(message -> {
                    if (message != null) {
                        log.info("Successfully retrieved message with subject: '{}'", message.getSubject());
                    } else {
                        log.warn("Retrieved a null message for messageId '{}' and userId '{}'", messageId, userId);
                    }
                })
                .doOnError(error -> log.error("Failed to retrieve message '{}' for user '{}'", messageId, userId, error));
    }

    public Mono<Subscription> createEmailSubscription(String userId) {
        log.info("Attempting to create email subscription for user '{}'", userId);
        return userConnectionRepository.findByUserId(userId)
                .doOnNext(userConnection -> log.debug("Found user connection for user ID: {}", userId))
                .switchIfEmpty(Mono.error(new UserConnectionNotFoundException(userId)))
                .flatMap(userConnection -> {
                    log.debug("User access token acquired. Requesting subscription creation from Microsoft Graph API.");
                    return graphClient.createSubscription(userId, notificationUrl, userConnection.getAccessToken());
                })
                .doOnSuccess(subscription -> {
                    if (subscription != null) {
                        log.info("Successfully created subscription '{}' for user '{}'. Expires: {}", subscription.getId(), userId, subscription.getExpirationDateTime());
                    } else {
                        log.warn("Created a null subscription for user '{}'", userId);
                    }
                })
                .doOnError(error -> log.error("Failed to create subscription for user '{}'", userId, error));
    }
}
