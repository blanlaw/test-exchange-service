package pe.test.exchange.application.dto;

import java.math.BigDecimal;

/**
 * Outbound contract of {@code POST /api/conversion}.
 */
public record ConversionResponse(
        BigDecimal monto,
        BigDecimal montoConvertido,
        String monedaOrigen,
        String monedaDestino,
        BigDecimal tipoCambio
) {
}
