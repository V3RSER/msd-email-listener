package com.example.demo.application.service;

import com.example.demo.application.usecase.ProcessNewEmailUseCase;
import com.example.demo.domain.service.EmailPurchaseExtractor;
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
    private final EmailPurchaseExtractor emailPurchaseExtractor;

    @Override
    public Mono<Void> processNewEmail(String userId, String messageId) {
        return outlookService.getEmailContent(userId, messageId)
                .flatMap(emailPurchaseExtractor::extractTotalAmount)
                .doOnNext(totalAmount -> log.info("Extracted total amount: {}", totalAmount))
                .then();
    }
}
