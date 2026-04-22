package com.alvarengacarlos.sakura.common;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface DataAnalysisRepository extends JpaRepository<DataAnalysisEntity, UUID> {

    List<DataAnalysisEntity> findByNextPurchaseDate(LocalDate date);
}
