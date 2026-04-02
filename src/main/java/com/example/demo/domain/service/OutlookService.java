package com.example.demo.domain.service;

import com.example.demo.domain.exception.UserConnectionNotFoundException;
import com.example.demo.domain.repository.UserConnectionRepository;
import com.example.demo.infrastructure.client.MicrosoftGraphClient;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.models.Subscription;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class OutlookService {

    private static final Logger logger = LoggerFactory.getLogger(OutlookService.class);
    private final MicrosoftGraphClient graphClient;
    private final UserConnectionRepository userConnectionRepository;

    @Value("${app.webhook-notification-url}")
    private String notificationUrl;

    public Mono<Message> getEmailContent(String userId, String messageId) {
        logger.info("Processing email for user '{}' and message '{}'", userId, messageId);

        return userConnectionRepository.findByUserId(userId)
                .switchIfEmpty(Mono.error(new UserConnectionNotFoundException(userId)))
                .flatMap(userConnection -> graphClient.getMessage(userId, messageId, userConnection.getAccessToken()))
                .doOnSuccess(message -> {
                    if (message != null) {
                        logger.info("Successfully retrieved message with subject: '{}'", message.getSubject());
                    } else {
                        logger.error("Could not retrieve message '{}' for user '{}'", messageId, userId);
                    }
                });
    }

    public Mono<Subscription> createEmailSubscription(String userId) {
        logger.info("Creating email subscription for user '{}'", userId);
        return userConnectionRepository.findByUserId(userId)
                .switchIfEmpty(Mono.error(new UserConnectionNotFoundException(userId)))
                .flatMap(userConnection -> graphClient.createSubscription(userId, notificationUrl, userConnection.getAccessToken()))
                .doOnSuccess(subscription -> logger.info("Successfully created subscription '{}' for user '{}'", subscription.getId(), userId));
    }
}
