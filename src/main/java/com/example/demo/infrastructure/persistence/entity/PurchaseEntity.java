package com.example.demo.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Table("purchases")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseEntity {
    @Id
    private UUID id;
    private String userId;
    private String messageId;
    private BigDecimal totalAmount;
    private OffsetDateTime purchaseDate;
}
