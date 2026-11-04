package pe.test.exchange.infrastructure.external;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds the {@code exchange-rate.api.*} block of {@code application.yaml}.
 */
@ConfigurationProperties(prefix = "exchange-rate.api")
public record ExchangeRateProperties(String baseUrl, String key) {
}
