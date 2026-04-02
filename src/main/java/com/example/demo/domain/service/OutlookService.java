package com.example.demo.domain.service;

import com.example.demo.domain.exception.UserConnectionNotFoundException;
import com.example.demo.domain.repository.UserConnectionRepository;
import com.example.demo.infrastructure.msgraph.MicrosoftGraphClient;
import com.microsoft.graph.models.Message;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class OutlookService {

    private static final Logger logger = LoggerFactory.getLogger(OutlookService.class);
    private final MicrosoftGraphClient graphClient;
    private final UserConnectionRepository userConnectionRepository;

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
}
