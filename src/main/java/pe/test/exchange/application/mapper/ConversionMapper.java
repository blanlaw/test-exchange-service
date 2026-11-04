package pe.test.exchange.application.mapper;

import pe.test.exchange.application.dto.ConversionRequest;
import pe.test.exchange.application.dto.ConversionResponse;
import pe.test.exchange.domain.model.Conversion;
import pe.test.exchange.domain.model.Currency;
import pe.test.exchange.domain.model.Money;

/**
 * Translates between the REST contract and domain objects. Stateless.
 */
public final class ConversionMapper {

    private ConversionMapper() {
    }

    public static Money toMoney(ConversionRequest request) {
        return Money.of(request.monto(), Currency.of(request.monedaOrigen()));
    }

    public static Currency toTargetCurrency(ConversionRequest request) {
        return Currency.of(request.monedaDestino());
    }

    public static ConversionResponse toResponse(Conversion conversion) {
        return new ConversionResponse(
                conversion.originalAmount().amount(),
                conversion.convertedAmount().amount(),
                conversion.sourceCurrency().code(),
                conversion.targetCurrency().code(),
                conversion.rate().value()
        );
    }
}
