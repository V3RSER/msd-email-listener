package com.example.demo.application.usecase;

import reactor.core.publisher.Mono;

/**
 * Defines the use case for processing a new email notification.
 */
public interface ProcessNewEmailUseCase {

    /**
     * Executes the use case.
     *
     * @param userId The ID of the user who received the email.
     * @param messageId The ID of the new email message.
     * @return A {@link Mono} that completes when the processing is finished.
     */
    Mono<Void> processNewEmail(String userId, String messageId);
}
