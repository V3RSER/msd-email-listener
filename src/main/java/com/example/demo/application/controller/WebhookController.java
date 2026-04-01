package com.example.demo.application.controller;

import com.example.demo.domain.service.OutlookService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Base64;

@RestController
@RequestMapping("/api/webhook")
public class WebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);
    private final OutlookService outlookService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WebhookController(OutlookService outlookService) {
        this.outlookService = outlookService;
    }

    @PostMapping("/callback")
    public ResponseEntity<String> handleCallback(@RequestBody String json, @RequestParam(required = false) String validationToken) {
        if (validationToken != null) {
            logger.info("Validation token received: '{}'", validationToken);
            return ResponseEntity.ok(validationToken);
        }

        try {
            JsonNode rootNode = objectMapper.readTree(json);
            JsonNode values = rootNode.get("value");
            if (values != null && values.isArray()) {
                for (JsonNode value : values) {
                    JsonNode resourceData = value.get("resourceData");
                    if (resourceData != null) {
                        String encryptedContent = resourceData.get("encryptedContent").get("data").asText();
                        byte[] decodedBytes = Base64.getDecoder().decode(encryptedContent);
                        String decodedContent = new String(decodedBytes);

                        JsonNode contentJson = objectMapper.readTree(decodedContent);
                        String userId = contentJson.get("userAccount").get("id").asText();
                        String messageId = contentJson.get("item").get("id").asText();

                        outlookService.processEmail(userId, messageId);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error processing webhook callback", e);
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok().build();
    }
}
