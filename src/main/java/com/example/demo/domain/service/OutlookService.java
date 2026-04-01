package com.example.demo.domain.service;

import com.example.demo.application.usecase.ProcessNewEmailUseCase;
import com.example.demo.domain.repository.UserConnectionRepository;
import com.example.demo.infrastructure.msgraph.MicrosoftGraphClient;
import com.microsoft.graph.models.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OutlookService implements ProcessNewEmailUseCase {

    private static final Logger logger = LoggerFactory.getLogger(OutlookService.class);
    private final MicrosoftGraphClient graphClient;
    private final UserConnectionRepository userConnectionRepository;

    public OutlookService(MicrosoftGraphClient graphClient, UserConnectionRepository userConnectionRepository) {
        this.graphClient = graphClient;
        this.userConnectionRepository = userConnectionRepository;
    }

    @Override
    public void processNewEmail(String userId, String messageId) {
        logger.info("Processing email for user '{}' and message '{}'", userId, messageId);

        var userConnection = userConnectionRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User connection not found for user: " + userId));

        // TODO: Implement token refresh logic if accessToken is expired

        Message message = graphClient.getMessage(userId, messageId, userConnection.getAccessToken());

        if (message == null) {
            logger.error("Could not retrieve message '{}' for user '{}'", messageId, userId);
            return;
        }

        logger.info("Successfully retrieved message with subject: '{}'", message.subject);

        // --- Placeholder for your processing logic ---
        extractPurchaseInfo(message);
        // --------------------------------------------
    }

    private void extractPurchaseInfo(Message message) {
        String subject = message.subject;
        String body = message.body.content;

        logger.info("--- EXTRACTING PURCHASE INFO ---");
        logger.info("Subject: {}", subject);
        // logger.info("Body: {}", body);

        // Here you would implement your logic with templates, regex, or even an LLM
        // to parse the email body and extract credit card purchase details.

        // Example:
        // if (subject.contains("Your purchase from Store XYZ")) {
        //     // ... use regex to find amount, date, items, etc.
        // }

        logger.info("--- EXTRACTION COMPLETE ---");
    }
}
