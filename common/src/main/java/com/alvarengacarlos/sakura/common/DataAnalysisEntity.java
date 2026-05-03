package com.alvarengacarlos.sakura.common;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "data_analysis")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class DataAnalysisEntity {

    @Id
    private UUID id;

    private String item;

    private int quantity;

    private String measureUnit;

    @Column(name = "next_purchase_date")
    private LocalDate nextPurchaseDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
