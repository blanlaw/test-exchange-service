package pe.test.exchange.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pe.test.exchange.domain.service.CurrencyConversionService;
import pe.test.exchange.domain.service.ExchangeRateProvider;
import pe.test.exchange.infrastructure.external.ExchangeRateProperties;

import java.time.Clock;

/**
 * Wires the framework-agnostic domain into the Spring container. The domain
 * package carries no Spring annotations, so its services are declared here.
 */
@Configuration
@EnableConfigurationProperties(ExchangeRateProperties.class)
public class DomainConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public CurrencyConversionService currencyConversionService(ExchangeRateProvider exchangeRateProvider, Clock clock) {
        return new CurrencyConversionService(exchangeRateProvider, clock);
    }
}
