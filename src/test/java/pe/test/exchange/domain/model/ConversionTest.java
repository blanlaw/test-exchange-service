package pe.test.exchange.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class ConversionTest {

    private static final Currency USD = Currency.of("USD");
    private static final Currency PEN = Currency.of("PEN");
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-11-03T10:15:30Z"), ZoneOffset.UTC);

    @Test
    void createsConversionApplyingTheRateAndStampingTheDate() {
        Money original = Money.of(new BigDecimal("100"), USD);
        ExchangeRate rate = ExchangeRate.of(USD, PEN, new BigDecimal("3.70"));

        Conversion conversion = Conversion.create(original, rate, FIXED_CLOCK);

        assertThat(conversion.id()).isNull();
        assertThat(conversion.originalAmount()).isEqualTo(original);
        assertThat(conversion.rate()).isEqualTo(rate);
        assertThat(conversion.convertedAmount().currency()).isEqualTo(PEN);
        assertThat(conversion.convertedAmount().amount()).isEqualByComparingTo("370.00");
        assertThat(conversion.occurredAt()).isEqualTo(LocalDateTime.of(2026, 11, 3, 10, 15, 30));
    }

    @Test
    void reconstitutesPersistedConversionWithoutRecomputing() {
        Money original = Money.of(new BigDecimal("100"), USD);
        ExchangeRate rate = ExchangeRate.of(USD, PEN, new BigDecimal("3.70"));
        Money converted = Money.of(new BigDecimal("369.99"), PEN);
        LocalDateTime at = LocalDateTime.of(2026, 1, 1, 0, 0);

        Conversion conversion = Conversion.reconstitute(7L, original, rate, converted, at);

        assertThat(conversion.id()).isEqualTo(7L);
        assertThat(conversion.convertedAmount()).isEqualTo(converted);
        assertThat(conversion.occurredAt()).isEqualTo(at);
    }
}
