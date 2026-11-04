package pe.test.exchange.domain.repository;

import pe.test.exchange.domain.model.Conversion;

/**
 * Outbound port for persisting conversions. Implemented in infrastructure.
 */
public interface ConversionRepository {

    /**
     * Persists the conversion and returns it with its generated identifier.
     */
    Conversion save(Conversion conversion);
}
