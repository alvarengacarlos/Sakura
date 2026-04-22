package com.alvarengacarlos.sakura.common;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TaxReceiptRepository extends JpaRepository<TaxReceiptEntity, UUID> {

    List<TaxReceiptEntity> findByTransactionTimeAfter(LocalDateTime dateTime);
}
