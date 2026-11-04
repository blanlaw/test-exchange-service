package pe.test.exchange.domain.model;

import org.junit.jupiter.api.Test;
import pe.test.exchange.domain.exception.InvalidAmountException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void createsMoneyWithPositiveAmount() {
        Money money = Money.of(new BigDecimal("100"), Currency.of("USD"));

        assertThat(money.amount()).isEqualByComparingTo("100");
        assertThat(money.currency()).isEqualTo(Currency.of("USD"));
    }

    @Test
    void rejectsNullAmount() {
        assertThatThrownBy(() -> Money.of(null, Currency.of("USD")))
                .isInstanceOf(InvalidAmountException.class);
    }

    @Test
    void rejectsZeroAmount() {
        assertThatThrownBy(() -> Money.of(BigDecimal.ZERO, Currency.of("USD")))
                .isInstanceOf(InvalidAmountException.class);
    }

    @Test
    void rejectsNegativeAmount() {
        assertThatThrownBy(() -> Money.of(new BigDecimal("-5"), Currency.of("USD")))
                .isInstanceOf(InvalidAmountException.class);
    }

    @Test
    void rejectsNullCurrency() {
        assertThatThrownBy(() -> Money.of(BigDecimal.TEN, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
