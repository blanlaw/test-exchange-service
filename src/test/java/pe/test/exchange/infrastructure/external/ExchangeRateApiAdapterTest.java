package pe.test.exchange.infrastructure.external;

import feign.FeignException;
import feign.Request;
import feign.Response;
import feign.RetryableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.test.exchange.domain.exception.ExchangeRateUnavailableException;
import pe.test.exchange.domain.exception.UnsupportedCurrencyException;
import pe.test.exchange.domain.model.Currency;
import pe.test.exchange.domain.model.ExchangeRate;
import pe.test.exchange.infrastructure.external.dto.ExchangeRateApiResponse;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExchangeRateApiAdapterTest {

    private static final Currency USD = Currency.of("USD");
    private static final Currency PEN = Currency.of("PEN");
    private static final String API_KEY = "test-key";

    private ExchangeRateApiClient client;
    private ExchangeRateApiAdapter adapter;

    @BeforeEach
    void setUp() {
        client = mock(ExchangeRateApiClient.class);
        adapter = new ExchangeRateApiAdapter(client, new ExchangeRateProperties("http://unused", API_KEY));
    }

    @Test
    void returnsRateForTargetCurrencyUsingConfiguredApiKey() {
        when(client.getLatestRates(API_KEY, "USD")).thenReturn(
                new ExchangeRateApiResponse("success", "USD",
                        Map.of("PEN", new BigDecimal("3.7012"), "EUR", new BigDecimal("0.92"))));

        ExchangeRate rate = adapter.getRate(USD, PEN);

        assertThat(rate.source()).isEqualTo(USD);
        assertThat(rate.target()).isEqualTo(PEN);
        assertThat(rate.value()).isEqualByComparingTo("3.7012");
    }

    @Test
    void throwsUnsupportedCurrencyWhenTargetIsNotQuoted() {
        when(client.getLatestRates(anyString(), eq("USD"))).thenReturn(
                new ExchangeRateApiResponse("success", "USD", Map.of("EUR", new BigDecimal("0.92"))));

        assertThatThrownBy(() -> adapter.getRate(USD, Currency.of("XXX")))
                .isInstanceOf(UnsupportedCurrencyException.class)
                .hasMessageContaining("XXX");
    }

    @Test
    void throwsUnsupportedCurrencyWhenProviderRejectsBaseCurrency() {
        when(client.getLatestRates(anyString(), eq("XXX")))
                .thenThrow(feignError(404, "{\"result\":\"error\",\"error-type\":\"unsupported-code\"}"));

        assertThatThrownBy(() -> adapter.getRate(Currency.of("XXX"), PEN))
                .isInstanceOf(UnsupportedCurrencyException.class)
                .hasMessageContaining("XXX");
    }

    @Test
    void throwsUnavailableWhenProviderAnswersWithErrorResult() {
        when(client.getLatestRates(anyString(), eq("USD"))).thenReturn(
                new ExchangeRateApiResponse("error", null, null));

        assertThatThrownBy(() -> adapter.getRate(USD, PEN))
                .isInstanceOf(ExchangeRateUnavailableException.class);
    }

    @Test
    void throwsUnavailableWhenProviderReturnsServerError() {
        when(client.getLatestRates(anyString(), eq("USD"))).thenThrow(feignError(503, "down"));

        assertThatThrownBy(() -> adapter.getRate(USD, PEN))
                .isInstanceOf(ExchangeRateUnavailableException.class)
                .hasMessageContaining("503");
    }

    @Test
    void throwsUnavailableWhenProviderCannotBeReached() {
        when(client.getLatestRates(anyString(), eq("USD"))).thenThrow(
                new RetryableException(-1, "connection refused", Request.HttpMethod.GET, (Long) null, dummyRequest()));

        assertThatThrownBy(() -> adapter.getRate(USD, PEN))
                .isInstanceOf(ExchangeRateUnavailableException.class);
    }

    @Test
    void neverLeaksTheApiKeyThroughTheDomainException() {
        FeignException withKeyInUrl = FeignException.errorStatus("getLatestRates",
                Response.builder()
                        .status(500)
                        .reason("")
                        .request(Request.create(Request.HttpMethod.GET,
                                "http://unused/" + API_KEY + "/latest/USD", Map.of(), null, StandardCharsets.UTF_8, null))
                        .body("boom", StandardCharsets.UTF_8)
                        .build());
        when(client.getLatestRates(anyString(), eq("USD"))).thenThrow(withKeyInUrl);

        assertThatThrownBy(() -> adapter.getRate(USD, PEN))
                .isInstanceOf(ExchangeRateUnavailableException.class)
                .satisfies(ex -> {
                    assertThat(ex.getMessage()).doesNotContain(API_KEY);
                    assertThat(ex.getCause()).isNull();
                });
    }

    private static Request dummyRequest() {
        return Request.create(Request.HttpMethod.GET, "http://unused", Map.of(), null, StandardCharsets.UTF_8, null);
    }

    private static FeignException feignError(int status, String body) {
        return FeignException.errorStatus("getLatestRates",
                Response.builder()
                        .status(status)
                        .reason("")
                        .request(dummyRequest())
                        .body(body, StandardCharsets.UTF_8)
                        .build());
    }
}
