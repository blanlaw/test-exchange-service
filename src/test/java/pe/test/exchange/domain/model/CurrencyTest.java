package pe.test.exchange.domain.model;

import org.junit.jupiter.api.Test;
import pe.test.exchange.domain.exception.InvalidCurrencyException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CurrencyTest {

    @Test
    void createsCurrencyFromValidIsoCode() {
        Currency currency = Currency.of("usd");

        assertThat(currency.code()).isEqualTo("USD");
    }

    @Test
    void trimsSurroundingWhitespace() {
        assertThat(Currency.of("  pen ").code()).isEqualTo("PEN");
    }

    @Test
    void rejectsNullCode() {
        assertThatThrownBy(() -> Currency.of(null))
                .isInstanceOf(InvalidCurrencyException.class);
    }

    @Test
    void rejectsCodeThatIsNotThreeLetters() {
        assertThatThrownBy(() -> Currency.of("US"))
                .isInstanceOf(InvalidCurrencyException.class)
                .hasMessageContaining("US");
        assertThatThrownBy(() -> Currency.of("U5D"))
                .isInstanceOf(InvalidCurrencyException.class);
    }

    @Test
    void currenciesWithSameCodeAreEqual() {
        assertThat(Currency.of("USD")).isEqualTo(Currency.of("usd"));
    }
}
