package pe.test.exchange.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

/**
 * Inbound contract of {@code POST /api/conversion}. Field names are kept in
 * Spanish because they are part of the public API specified by the challenge.
 */
public record ConversionRequest(
        @NotNull(message = "monto es obligatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "monto debe ser mayor que cero")
        BigDecimal monto,

        @NotBlank(message = "monedaOrigen es obligatoria")
        @Pattern(regexp = "^\s*[A-Za-z]{3}\s*$", message = "monedaOrigen debe ser un código ISO 4217 de 3 letras")
        String monedaOrigen,

        @NotBlank(message = "monedaDestino es obligatoria")
        @Pattern(regexp = "^\s*[A-Za-z]{3}\s*$", message = "monedaDestino debe ser un código ISO 4217 de 3 letras")
        String monedaDestino
) {
}
