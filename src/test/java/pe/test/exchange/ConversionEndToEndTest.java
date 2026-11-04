package pe.test.exchange;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pe.test.exchange.infrastructure.external.ExchangeRateApiClient;
import pe.test.exchange.infrastructure.external.dto.ExchangeRateApiResponse;
import pe.test.exchange.infrastructure.persistence.ConversionJpaRepository;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises the whole slice (HTTP -> application -> domain -> JPA) with the
 * only external boundary, the Feign client, replaced by a stub.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ConversionEndToEndTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConversionJpaRepository jpaRepository;

    @MockitoBean
    private ExchangeRateApiClient exchangeRateApiClient;

    @BeforeEach
    void cleanDatabase() {
        jpaRepository.deleteAll();
    }

    @Test
    void convertsAndStoresTheOperation() throws Exception {
        when(exchangeRateApiClient.getLatestRates(anyString(), eq("USD"))).thenReturn(
                new ExchangeRateApiResponse("success", "USD", Map.of("PEN", new BigDecimal("3.70"))));

        mockMvc.perform(post("/api/conversion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"monto": 100, "monedaOrigen": "USD", "monedaDestino": "PEN"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monto").value(100))
                .andExpect(jsonPath("$.montoConvertido").value(370.00))
                .andExpect(jsonPath("$.monedaOrigen").value("USD"))
                .andExpect(jsonPath("$.monedaDestino").value("PEN"))
                .andExpect(jsonPath("$.tipoCambio").value(3.70));

        assertThat(jpaRepository.findAll()).singleElement().satisfies(stored -> {
            assertThat(stored.getOriginalAmount()).isEqualByComparingTo("100");
            assertThat(stored.getSourceCurrency()).isEqualTo("USD");
            assertThat(stored.getTargetCurrency()).isEqualTo("PEN");
            assertThat(stored.getExchangeRate()).isEqualByComparingTo("3.70");
            assertThat(stored.getConvertedAmount()).isEqualByComparingTo("370.00");
            assertThat(stored.getOccurredAt()).isNotNull();
        });
    }

    @Test
    void doesNotStoreAnythingWhenTargetCurrencyIsUnsupported() throws Exception {
        when(exchangeRateApiClient.getLatestRates(anyString(), eq("USD"))).thenReturn(
                new ExchangeRateApiResponse("success", "USD", Map.of("PEN", new BigDecimal("3.70"))));

        mockMvc.perform(post("/api/conversion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"monto": 100, "monedaOrigen": "USD", "monedaDestino": "ZZZ"}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("UNSUPPORTED_CURRENCY"));

        assertThat(jpaRepository.count()).isZero();
    }
}
