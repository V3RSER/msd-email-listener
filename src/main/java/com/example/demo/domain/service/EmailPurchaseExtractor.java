package com.example.demo.domain.service;

import com.microsoft.graph.models.Message;
import com.microsoft.graph.models.ItemBody;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EmailPurchaseExtractor {

    private static final Pattern TOTAL_AMOUNT_PATTERN = Pattern.compile("Total: \\$(\\d+\\.\\d{2})");

    public Optional<BigDecimal> extractTotalAmount(Message message) {
        if (message == null || message.getBody() == null || message.getBody().getContent() == null) {
            return Optional.empty();
        }

        String body = message.getBody().getContent();
        Matcher matcher = TOTAL_AMOUNT_PATTERN.matcher(body);

        if (matcher.find()) {
            try {
                return Optional.of(new BigDecimal(matcher.group(1)));
            } catch (NumberFormatException e) {
                // Log the error, the captured group was not a valid number
                return Optional.empty();
            }
        }

        return Optional.empty();
    }
}
