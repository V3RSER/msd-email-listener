package com.example.demo.application.service;

import com.example.demo.application.usecase.ProcessNewEmailUseCase;
import com.example.demo.domain.service.OutlookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessNewEmailService implements ProcessNewEmailUseCase {

    private final OutlookService outlookService;

    @Override
    public Mono<Void> processNewEmail(String userId, String messageId) {
        log.info("Processing new email for user: {} and message: {}", userId, messageId);
        return outlookService.getEmailContent(userId, messageId)
                .doOnSuccess(message -> log.info("Successfully retrieved email content for message: {}", messageId))
                .doOnError(error -> log.error("Error retrieving email content for message: {}", messageId, error))
                .then()
                .doOnSuccess(aVoid -> log.info("Successfully processed email: {}", messageId))
                .doOnError(error -> log.error("Error processing email: {}", messageId, error));
    }
}
