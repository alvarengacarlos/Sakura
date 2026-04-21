package com.alvarengacarlos.sakura.common;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tax_receipt")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class TaxReceiptEntity {

    @Id
    private UUID id;

    private String description;

    private int quantity;

    @Column(name = "price", columnDefinition = "MONEY")
    @ColumnTransformer(read = "price::numeric")
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "payment_method", columnDefinition = "payment_methods")
    private PaymentMethod paymentMethod;

    @Column(name = "`where`")
    private String storeName;

    @Column(name = "`when`")
    private LocalDateTime transactionTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
