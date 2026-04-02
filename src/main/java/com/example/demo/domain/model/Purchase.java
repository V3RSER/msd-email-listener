package com.example.demo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Purchase {
    private UUID id;
    private String userId;
    private String messageId;
    private BigDecimal totalAmount;
    private OffsetDateTime purchaseDate;
}
