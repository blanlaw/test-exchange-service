package pe.test.exchange.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA representation of a persisted conversion. Deliberately separate from the
 * domain aggregate so that persistence concerns never leak into the domain.
 */
@Entity
@Table(name = "conversions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ConversionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal originalAmount;

    @Column(name = "source_currency", nullable = false, length = 3)
    private String sourceCurrency;

    @Column(name = "target_currency", nullable = false, length = 3)
    private String targetCurrency;

    @Column(name = "exchange_rate", nullable = false, precision = 19, scale = 6)
    private BigDecimal exchangeRate;

    @Column(name = "converted_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal convertedAmount;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;
}
