package com.example.demo.domain.service;

import com.microsoft.graph.models.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class EmailPurchaseExtractor {

    private static final Pattern TOTAL_AMOUNT_PATTERN = Pattern.compile("Total: \\$(\\d+\\.\\d{2})");

    public Mono<BigDecimal> extractTotalAmount(Message message) {
        if (message == null || message.getBody() == null || message.getBody().getContent() == null) {
            log.warn("Cannot extract total amount from message, because it is null or has no content");
            return Mono.empty();
        }

        return Mono.fromCallable(() -> {
            String body = message.getBody().getContent();
            log.debug("Attempting to extract total amount from email body");
            Matcher matcher = TOTAL_AMOUNT_PATTERN.matcher(body);

            if (matcher.find()) {
                try {
                    BigDecimal totalAmount = new BigDecimal(matcher.group(1));
                    log.info("Successfully extracted total amount: {}", totalAmount);
                    return totalAmount;
                } catch (NumberFormatException e) {
                    log.error("Could not parse extracted total amount as a number: {}", matcher.group(1), e);
                    return null;
                }
            } else {
                log.warn("Could not find total amount in email body");
            }
            return null;
        });
    }
}
