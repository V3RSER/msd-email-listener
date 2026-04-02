package com.example.demo.application.service;

import com.example.demo.application.usecase.ProcessNewEmailUseCase;
import com.example.demo.domain.model.Purchase;
import com.example.demo.domain.repository.PurchaseRepository;
import com.example.demo.domain.service.EmailPurchaseExtractor;
import com.example.demo.domain.service.OutlookService;
import com.microsoft.graph.models.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessNewEmailService implements ProcessNewEmailUseCase {

    private final OutlookService outlookService;
    private final EmailPurchaseExtractor emailPurchaseExtractor;
    private final PurchaseRepository purchaseRepository;

    @Override
    public Mono<Void> processNewEmail(String userId, String messageId) {
        log.info("Processing new email for user: {} and message: {}", userId, messageId);
        return outlookService.getEmailContent(userId, messageId)
                .doOnSuccess(message -> log.info("Successfully retrieved email content for message: {}", messageId))
                .doOnError(error -> log.error("Error retrieving email content for message: {}", messageId, error))
                .flatMap(message -> extractAndSavePurchase(message, userId))
                .doOnError(error -> log.error("Error extracting and saving purchase for message: {}", messageId, error))
                .then()
                .doOnSuccess(aVoid -> log.info("Successfully processed email: {}", messageId))
                .doOnError(error -> log.error("Error processing email: {}", messageId, error));
    }

    private Mono<Void> extractAndSavePurchase(Message message, String userId) {
        return emailPurchaseExtractor.extractTotalAmount(message)
                .flatMap(totalAmount -> {
                    Purchase purchase = Purchase.builder()
                            .userId(userId)
                            .messageId(message.getId())
                            .totalAmount(totalAmount)
                            .purchaseDate(message.getSentDateTime())
                            .build();

                    log.info("Saving purchase: {}", purchase);

                    return purchaseRepository.save(purchase)
                            .doOnSuccess(savedPurchase -> log.info("Successfully saved purchase: {}", savedPurchase))
                            .doOnError(error -> log.error("Error saving purchase: {}", purchase, error));
                })
                .then();
    }
}
