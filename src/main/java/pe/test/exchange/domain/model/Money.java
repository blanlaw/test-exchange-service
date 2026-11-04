package pe.test.exchange.domain.model;

import pe.test.exchange.domain.exception.InvalidAmountException;

import java.math.BigDecimal;

/**
 * Value Object representing a monetary amount in a given currency.
 * Amounts must be strictly positive for a conversion to make sense.
 */
public record Money(BigDecimal amount, Currency currency) {

    public Money {
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidAmountException(amount);
        }
        if (currency == null) {
            throw new IllegalArgumentException("Currency is required");
        }
    }

    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }
}
