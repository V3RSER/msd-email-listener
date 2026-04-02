package com.example.demo.domain.repository;

import com.example.demo.domain.model.Purchase;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PurchaseRepository extends ReactiveCrudRepository<Purchase, UUID> {
}
