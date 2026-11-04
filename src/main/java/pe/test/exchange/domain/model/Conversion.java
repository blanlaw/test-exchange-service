package pe.test.exchange.domain.model;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Aggregate root representing a currency conversion that has been performed.
 * It is created through {@link #create(Money, ExchangeRate, Clock)}, which
 * applies the rate, and reconstituted from persistence through
 * {@link #reconstitute(Long, Money, ExchangeRate, Money, LocalDateTime)}.
 */
public final class Conversion {

    private final Long id;
    private final Money originalAmount;
    private final ExchangeRate rate;
    private final Money convertedAmount;
    private final LocalDateTime occurredAt;

    private Conversion(Long id, Money originalAmount, ExchangeRate rate,
                       Money convertedAmount, LocalDateTime occurredAt) {
        this.id = id;
        this.originalAmount = Objects.requireNonNull(originalAmount, "originalAmount");
        this.rate = Objects.requireNonNull(rate, "rate");
        this.convertedAmount = Objects.requireNonNull(convertedAmount, "convertedAmount");
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt");
    }

    public static Conversion create(Money originalAmount, ExchangeRate rate, Clock clock) {
        Money converted = rate.convert(originalAmount);
        return new Conversion(null, originalAmount, rate, converted, LocalDateTime.now(clock));
    }

    public static Conversion reconstitute(Long id, Money originalAmount, ExchangeRate rate,
                                          Money convertedAmount, LocalDateTime occurredAt) {
        return new Conversion(id, originalAmount, rate, convertedAmount, occurredAt);
    }

    public Long id() {
        return id;
    }

    public Money originalAmount() {
        return originalAmount;
    }

    public ExchangeRate rate() {
        return rate;
    }

    public Money convertedAmount() {
        return convertedAmount;
    }

    public LocalDateTime occurredAt() {
        return occurredAt;
    }

    public Currency sourceCurrency() {
        return originalAmount.currency();
    }

    public Currency targetCurrency() {
        return convertedAmount.currency();
    }
}
