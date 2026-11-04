package pe.test.exchange.domain.service;

import pe.test.exchange.domain.model.Currency;
import pe.test.exchange.domain.model.ExchangeRate;

/**
 * Outbound port: the domain depends on this abstraction to obtain exchange
 * rates. Infrastructure provides the implementation (e.g. a REST client).
 */
@FunctionalInterface
public interface ExchangeRateProvider {

    /**
     * @throws pe.test.exchange.domain.exception.UnsupportedCurrencyException
     *         when the provider does not quote one of the currencies
     * @throws pe.test.exchange.domain.exception.ExchangeRateUnavailableException
     *         when the provider cannot be reached or answers with an error
     */
    ExchangeRate getRate(Currency source, Currency target);
}
