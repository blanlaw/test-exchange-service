package pe.test.exchange.domain.service;

import pe.test.exchange.domain.model.Conversion;
import pe.test.exchange.domain.model.Currency;
import pe.test.exchange.domain.model.ExchangeRate;
import pe.test.exchange.domain.model.Money;

import java.time.Clock;
import java.util.Objects;

/**
 * Domain service holding the conversion rule: resolve the rate between the
 * two currencies and produce a {@link Conversion}. Converting a currency
 * into itself never hits the external provider.
 */
public class CurrencyConversionService {

    private final ExchangeRateProvider exchangeRateProvider;
    private final Clock clock;

    public CurrencyConversionService(ExchangeRateProvider exchangeRateProvider, Clock clock) {
        this.exchangeRateProvider = Objects.requireNonNull(exchangeRateProvider, "exchangeRateProvider");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public Conversion convert(Money original, Currency target) {
        ExchangeRate rate = resolveRate(original.currency(), target);
        return Conversion.create(original, rate, clock);
    }

    private ExchangeRate resolveRate(Currency source, Currency target) {
        if (source.equals(target)) {
            return ExchangeRate.identity(source);
        }
        return exchangeRateProvider.getRate(source, target);
    }
}
