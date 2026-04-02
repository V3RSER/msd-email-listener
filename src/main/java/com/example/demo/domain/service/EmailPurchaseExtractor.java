package com.example.demo.domain.service;

import com.microsoft.graph.models.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EmailPurchaseExtractor {

    private static final Pattern TOTAL_AMOUNT_PATTERN = Pattern.compile("Total: \\$(\\d+\\.\\d{2})");

    public Mono<BigDecimal> extractTotalAmount(Message message) {
        if (message == null || message.getBody() == null || message.getBody().getContent() == null) {
            return Mono.empty();
        }

        return Mono.fromCallable(() -> {
            String body = message.getBody().getContent();
            Matcher matcher = TOTAL_AMOUNT_PATTERN.matcher(body);

            if (matcher.find()) {
                try {
                    return new BigDecimal(matcher.group(1));
                } catch (NumberFormatException e) {
                    // Log the error, the captured group was not a valid number
                    return null;
                }
            }
            return null;
        });
    }
}
