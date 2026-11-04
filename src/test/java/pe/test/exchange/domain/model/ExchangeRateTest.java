package pe.test.exchange.domain.model;

import org.junit.jupiter.api.Test;
import pe.test.exchange.domain.exception.InvalidExchangeRateException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExchangeRateTest {

    private static final Currency USD = Currency.of("USD");
    private static final Currency PEN = Currency.of("PEN");

    @Test
    void convertsMoneyUsingTheRate() {
        ExchangeRate rate = ExchangeRate.of(USD, PEN, new BigDecimal("3.70"));

        Money converted = rate.convert(Money.of(new BigDecimal("100"), USD));

        assertThat(converted.currency()).isEqualTo(PEN);
        assertThat(converted.amount()).isEqualByComparingTo("370.00");
    }

    @Test
    void roundsConvertedAmountToTwoDecimalsHalfUp() {
        ExchangeRate rate = ExchangeRate.of(USD, PEN, new BigDecimal("3.3333"));

        Money converted = rate.convert(Money.of(new BigDecimal("10"), USD));

        assertThat(converted.amount()).isEqualTo(new BigDecimal("33.33"));
    }

    @Test
    void rejectsConvertingMoneyInADifferentSourceCurrency() {
        ExchangeRate rate = ExchangeRate.of(USD, PEN, new BigDecimal("3.70"));

        assertThatThrownBy(() -> rate.convert(Money.of(BigDecimal.TEN, Currency.of("EUR"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("EUR");
    }

    @Test
    void rejectsNonPositiveRate() {
        assertThatThrownBy(() -> ExchangeRate.of(USD, PEN, BigDecimal.ZERO))
                .isInstanceOf(InvalidExchangeRateException.class);
        assertThatThrownBy(() -> ExchangeRate.of(USD, PEN, null))
                .isInstanceOf(InvalidExchangeRateException.class);
    }

    @Test
    void identityRateForSameCurrencyIsOne() {
        ExchangeRate rate = ExchangeRate.identity(USD);

        assertThat(rate.value()).isEqualByComparingTo("1");
        assertThat(rate.convert(Money.of(new BigDecimal("42"), USD)).amount()).isEqualByComparingTo("42.00");
    }
}
