package com.example.demo.application.usecase;

/**
 * Defines the use case for processing a new email notification.
 */
public interface ProcessNewEmailUseCase {

    /**
     * Executes the use case.
     *
     * @param userId The ID of the user who received the email.
     * @param messageId The ID of the new email message.
     */
    void processNewEmail(String userId, String messageId);
}
