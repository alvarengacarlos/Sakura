package com.alvarengacarlos.sakura.common;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TaxReceiptRepository extends JpaRepository<TaxReceiptEntity, UUID> {
}
