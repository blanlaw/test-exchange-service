package pe.test.exchange.application.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pe.test.exchange.application.dto.ConversionRequest;
import pe.test.exchange.application.dto.ConversionResponse;
import pe.test.exchange.application.service.ConversionApplicationService;
import pe.test.exchange.domain.exception.ExchangeRateUnavailableException;
import pe.test.exchange.domain.exception.InvalidCurrencyException;
import pe.test.exchange.domain.exception.UnsupportedCurrencyException;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConversionController.class)
class ConversionControllerTest {

    private static final String ENDPOINT = "/api/conversion";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConversionApplicationService conversionService;

    @Test
    void returns200WithTheConversionContract() throws Exception {
        when(conversionService.convert(any(ConversionRequest.class))).thenReturn(new ConversionResponse(
                new BigDecimal("100"), new BigDecimal("370.00"), "USD", "PEN", new BigDecimal("3.70")));

        mockMvc.perform(post(ENDPOINT)
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
    }

    @Test
    void returns400WhenRequestFailsBeanValidation() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"monto": -5, "monedaOrigen": "", "monedaDestino": "SOLES"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details").value(hasItem(containsString("monto"))))
                .andExpect(jsonPath("$.details").value(hasItem(containsString("monedaOrigen"))))
                .andExpect(jsonPath("$.details").value(hasItem(containsString("monedaDestino"))));
    }

    @Test
    void returns400WhenBodyIsNotValidJson() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monto\": \"abc\""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("MALFORMED_REQUEST"));
    }

    @Test
    void returns400WhenDomainRejectsCurrency() throws Exception {
        when(conversionService.convert(any())).thenThrow(new InvalidCurrencyException("US1"));

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"monto": 100, "monedaOrigen": "USD", "monedaDestino": "PEN"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_CURRENCY"))
                .andExpect(jsonPath("$.message").value(containsString("US1")));
    }

    @Test
    void returns422WhenCurrencyIsNotSupportedByProvider() throws Exception {
        when(conversionService.convert(any())).thenThrow(new UnsupportedCurrencyException("XYZ"));

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"monto": 100, "monedaOrigen": "USD", "monedaDestino": "XYZ"}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("UNSUPPORTED_CURRENCY"))
                .andExpect(jsonPath("$.message").value(containsString("XYZ")));
    }

    @Test
    void returns503WhenExternalProviderIsUnavailable() throws Exception {
        when(conversionService.convert(any())).thenThrow(new ExchangeRateUnavailableException("HTTP 503"));

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"monto": 100, "monedaOrigen": "USD", "monedaDestino": "PEN"}
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("EXCHANGE_RATE_UNAVAILABLE"));
    }

    @Test
    void returns500WithoutLeakingDetailsOnUnexpectedErrors() throws Exception {
        when(conversionService.convert(any())).thenThrow(new IllegalStateException("secret internal detail"));

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"monto": 100, "monedaOrigen": "USD", "monedaDestino": "PEN"}
                                """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.not(containsString("secret"))));
    }
}
