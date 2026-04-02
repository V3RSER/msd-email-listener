package com.example.demo.application.service;

import com.example.demo.application.usecase.ProcessNewEmailUseCase;
import com.example.demo.domain.service.EmailPurchaseExtractor;
import com.example.demo.domain.service.OutlookService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ProcessNewEmailService implements ProcessNewEmailUseCase {

    private static final Logger logger = LoggerFactory.getLogger(ProcessNewEmailService.class);
    private final OutlookService outlookService;
    private final EmailPurchaseExtractor emailPurchaseExtractor;

    @Override
    public Mono<Void> processNewEmail(String userId, String messageId) {
        return outlookService.getEmailContent(userId, messageId)
                .flatMap(emailPurchaseExtractor::extractTotalAmount)
                .doOnNext(totalAmount -> logger.info("Extracted total amount: {}", totalAmount))
                .then();
    }
}
