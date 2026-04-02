package com.example.demo.domain.service;

import com.example.demo.application.usecase.ProcessNewEmailUseCase;
import com.example.demo.domain.exception.UserConnectionNotFoundException;
import com.example.demo.domain.repository.UserConnectionRepository;
import com.example.demo.infrastructure.msgraph.MicrosoftGraphClient;
import com.microsoft.graph.models.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class OutlookService implements ProcessNewEmailUseCase {

    private static final Logger logger = LoggerFactory.getLogger(OutlookService.class);
    private final MicrosoftGraphClient graphClient;
    private final UserConnectionRepository userConnectionRepository;
    private final EmailPurchaseExtractor emailPurchaseExtractor;

    public OutlookService(
            MicrosoftGraphClient graphClient,
            UserConnectionRepository userConnectionRepository,
            EmailPurchaseExtractor emailPurchaseExtractor) {
        this.graphClient = graphClient;
        this.userConnectionRepository = userConnectionRepository;
        this.emailPurchaseExtractor = emailPurchaseExtractor;
    }

    @Override
    public void processNewEmail(String userId, String messageId) {
        logger.info("Processing email for user '{}' and message '{}'", userId, messageId);

        var userConnection = userConnectionRepository.findByUserId(userId)
                .orElseThrow(() -> new UserConnectionNotFoundException(userId));

        Message message = graphClient.getMessage(userId, messageId, userConnection.getAccessToken());

        if (message == null) {
            logger.error("Could not retrieve message '{}' for user '{}'", messageId, userId);
            return;
        }

        logger.info("Successfully retrieved message with subject: '{}'", message.getSubject());

        Optional<BigDecimal> totalAmount = emailPurchaseExtractor.extractTotalAmount(message);

        totalAmount.ifPresentOrElse(
            amount -> logger.info("Extracted total amount: {}", amount),
            () -> logger.info("No total amount found in the email.")
        );
    }
}
