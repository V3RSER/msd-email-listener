package com.example.demo.domain.service;

import com.example.demo.application.usecase.ProcessNewEmailUseCase;
import com.example.demo.domain.repository.UserConnectionRepository;
import com.example.demo.infrastructure.msgraph.MicrosoftGraphClient;
import com.microsoft.graph.models.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        Message message = graphClient.getMessage(userId, messageId, userConnection.getAccessToken());

        if (message == null) {
            logger.error("Could not retrieve message '{}' for user '{}'", messageId, userId);
            return;
        }

        logger.info("Successfully retrieved message with subject: '{}'", message.getSubject());

        extractPurchaseInfo(message);
    }

    private void extractPurchaseInfo(Message message) {
        String subject = message.getSubject();
        String body = Objects.requireNonNull(message.getBody()).getContent();

        logger.info("--- EXTRACTING PURCHASE INFO ---");
        logger.info("Subject: {}", subject);

        Pattern pattern = Pattern.compile("Total: \\$(\\d+\\.\\d{2})");
        Matcher matcher = pattern.matcher(body);

        if (matcher.find()) {
            String totalAmount = matcher.group(1);
            logger.info("Total amount found: {}", totalAmount);
        }

        logger.info("--- EXTRACTION COMPLETE ---");
    }
}
