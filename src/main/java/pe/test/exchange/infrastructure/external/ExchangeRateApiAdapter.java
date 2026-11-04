package pe.test.exchange.infrastructure.external;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pe.test.exchange.domain.exception.ExchangeRateUnavailableException;
import pe.test.exchange.domain.exception.UnsupportedCurrencyException;
import pe.test.exchange.domain.model.Currency;
import pe.test.exchange.domain.model.ExchangeRate;
import pe.test.exchange.domain.service.ExchangeRateProvider;
import pe.test.exchange.infrastructure.external.dto.ExchangeRateApiResponse;

import java.math.BigDecimal;

/**
 * Adapter that fulfils the domain's {@link ExchangeRateProvider} port using
 * the ExchangeRate-API REST service through the Feign client. Translates
 * transport-level failures into domain exceptions so that upper layers never
 * depend on Feign.
 */
@Component
public class ExchangeRateApiAdapter implements ExchangeRateProvider {

    private static final Logger log = LoggerFactory.getLogger(ExchangeRateApiAdapter.class);
    private static final String SUCCESS_RESULT = "success";
    private static final String UNSUPPORTED_CODE_ERROR = "unsupported-code";

    private final ExchangeRateApiClient client;
    private final ExchangeRateProperties properties;

    public ExchangeRateApiAdapter(ExchangeRateApiClient client, ExchangeRateProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public ExchangeRate getRate(Currency source, Currency target) {
        ExchangeRateApiResponse response = fetchLatestRates(source);

        if (!SUCCESS_RESULT.equalsIgnoreCase(response.result())) {
            throw new ExchangeRateUnavailableException("provider answered with result '" + response.result() + "'");
        }
        if (response.conversionRates() == null) {
            throw new ExchangeRateUnavailableException("provider answered without conversion rates");
        }

        BigDecimal rate = response.conversionRates().get(target.code());
        if (rate == null) {
            throw new UnsupportedCurrencyException(target.code());
        }
        return ExchangeRate.of(source, target, rate);
    }

    private ExchangeRateApiResponse fetchLatestRates(Currency base) {
        try {
            return client.getLatestRates(properties.key(), base.code());
        } catch (FeignException e) {
            if (isUnsupportedBaseCurrency(e)) {
                throw new UnsupportedCurrencyException(base.code());
            }
            // Feign's message and the exception itself embed the request URL, which
            // contains the API key. Only the status is logged and the cause is dropped.
            log.warn("Exchange rate provider call failed for base {}: {}", base, describe(e));
            throw new ExchangeRateUnavailableException(describe(e));
        }
    }

    /** ExchangeRate-API answers 404 with error-type "unsupported-code" for unknown base currencies. */
    private static boolean isUnsupportedBaseCurrency(FeignException e) {
        return e.status() == 404 && e.contentUTF8().contains(UNSUPPORTED_CODE_ERROR);
    }

    private static String describe(FeignException e) {
        return e.status() > 0 ? "HTTP " + e.status() : "connection error";
    }
}
