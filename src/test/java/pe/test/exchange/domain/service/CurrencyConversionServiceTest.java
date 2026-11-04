package pe.test.exchange.domain.service;

import org.junit.jupiter.api.Test;
import pe.test.exchange.domain.model.Conversion;
import pe.test.exchange.domain.model.Currency;
import pe.test.exchange.domain.model.ExchangeRate;
import pe.test.exchange.domain.model.Money;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class CurrencyConversionServiceTest {

    private static final Currency USD = Currency.of("USD");
    private static final Currency PEN = Currency.of("PEN");
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-03T00:00:00Z"), ZoneOffset.UTC);

    @Test
    void convertsUsingTheRateFromTheProvider() {
        ExchangeRateProvider provider = (source, target) -> ExchangeRate.of(source, target, new BigDecimal("3.70"));
        CurrencyConversionService service = new CurrencyConversionService(provider, CLOCK);

        Conversion conversion = service.convert(Money.of(new BigDecimal("100"), USD), PEN);

        assertThat(conversion.sourceCurrency()).isEqualTo(USD);
        assertThat(conversion.targetCurrency()).isEqualTo(PEN);
        assertThat(conversion.rate().value()).isEqualByComparingTo("3.70");
        assertThat(conversion.convertedAmount().amount()).isEqualByComparingTo("370.00");
    }

    @Test
    void sameCurrencyConversionUsesIdentityRateWithoutCallingProvider() {
        AtomicInteger calls = new AtomicInteger();
        ExchangeRateProvider provider = (source, target) -> {
            calls.incrementAndGet();
            throw new AssertionError("provider must not be called");
        };
        CurrencyConversionService service = new CurrencyConversionService(provider, CLOCK);

        Conversion conversion = service.convert(Money.of(new BigDecimal("55.5"), USD), USD);

        assertThat(calls.get()).isZero();
        assertThat(conversion.rate().value()).isEqualByComparingTo("1");
        assertThat(conversion.convertedAmount().amount()).isEqualByComparingTo("55.50");
    }
}
