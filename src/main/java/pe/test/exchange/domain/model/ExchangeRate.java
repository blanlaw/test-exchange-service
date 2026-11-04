package pe.test.exchange.domain.model;

import pe.test.exchange.domain.exception.InvalidExchangeRateException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Value Object representing the rate to convert one unit of {@code source}
 * currency into {@code target} currency. Encapsulates the conversion rule,
 * including the rounding policy for monetary results.
 */
public record ExchangeRate(Currency source, Currency target, BigDecimal value) {

    public static final int MONEY_SCALE = 2;
    public static final RoundingMode MONEY_ROUNDING = RoundingMode.HALF_UP;

    public ExchangeRate {
        if (source == null || target == null) {
            throw new IllegalArgumentException("Source and target currencies are required");
        }
        if (value == null || value.signum() <= 0) {
            throw new InvalidExchangeRateException(value);
        }
    }

    public static ExchangeRate of(Currency source, Currency target, BigDecimal value) {
        return new ExchangeRate(source, target, value);
    }

    public static ExchangeRate identity(Currency currency) {
        return new ExchangeRate(currency, currency, BigDecimal.ONE);
    }

    public Money convert(Money money) {
        if (!money.currency().equals(source)) {
            throw new IllegalArgumentException(
                    "Cannot apply rate " + source + "->" + target + " to money in " + money.currency());
        }
        BigDecimal converted = money.amount().multiply(value).setScale(MONEY_SCALE, MONEY_ROUNDING);
        return Money.of(converted, target);
    }
}
