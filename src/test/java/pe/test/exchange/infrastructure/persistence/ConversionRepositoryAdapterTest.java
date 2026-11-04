package pe.test.exchange.infrastructure.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import pe.test.exchange.domain.model.Conversion;
import pe.test.exchange.domain.model.Currency;
import pe.test.exchange.domain.model.ExchangeRate;
import pe.test.exchange.domain.model.Money;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(ConversionRepositoryAdapter.class)
class ConversionRepositoryAdapterTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-03T15:00:00Z"), ZoneOffset.UTC);
    private static final LocalDateTime EXPECTED_DATE = LocalDateTime.of(2026, 11, 3, 15, 0, 0);

    @Autowired
    private ConversionRepositoryAdapter adapter;

    @Autowired
    private ConversionJpaRepository jpaRepository;

    @Test
    void savesConversionAndReturnsItWithGeneratedId() {
        Conversion conversion = Conversion.create(
                Money.of(new BigDecimal("100"), Currency.of("USD")),
                ExchangeRate.of(Currency.of("USD"), Currency.of("PEN"), new BigDecimal("3.7012")),
                CLOCK);

        Conversion saved = adapter.save(conversion);

        assertThat(saved.id()).isNotNull();
        assertThat(saved.originalAmount().amount()).isEqualByComparingTo("100");
        assertThat(saved.sourceCurrency().code()).isEqualTo("USD");
        assertThat(saved.targetCurrency().code()).isEqualTo("PEN");
        assertThat(saved.rate().value()).isEqualByComparingTo("3.7012");
        assertThat(saved.convertedAmount().amount()).isEqualByComparingTo("370.12");
        assertThat(saved.occurredAt()).isEqualTo(EXPECTED_DATE);
    }

    @Test
    void persistsAllRequiredColumns() {
        adapter.save(Conversion.create(
                Money.of(new BigDecimal("50"), Currency.of("EUR")),
                ExchangeRate.of(Currency.of("EUR"), Currency.of("USD"), new BigDecimal("1.1")),
                CLOCK));

        assertThat(jpaRepository.findAll()).singleElement().satisfies(entity -> {
            assertThat(entity.getOriginalAmount()).isEqualByComparingTo("50");
            assertThat(entity.getSourceCurrency()).isEqualTo("EUR");
            assertThat(entity.getTargetCurrency()).isEqualTo("USD");
            assertThat(entity.getExchangeRate()).isEqualByComparingTo("1.1");
            assertThat(entity.getConvertedAmount()).isEqualByComparingTo("55.00");
            assertThat(entity.getOccurredAt()).isEqualTo(EXPECTED_DATE);
        });
    }
}
