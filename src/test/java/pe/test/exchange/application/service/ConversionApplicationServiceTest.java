package pe.test.exchange.application.service;

import org.junit.jupiter.api.Test;
import pe.test.exchange.application.dto.ConversionRequest;
import pe.test.exchange.application.dto.ConversionResponse;
import pe.test.exchange.domain.exception.InvalidCurrencyException;
import pe.test.exchange.domain.model.Conversion;
import pe.test.exchange.domain.model.ExchangeRate;
import pe.test.exchange.domain.repository.ConversionRepository;
import pe.test.exchange.domain.service.CurrencyConversionService;
import pe.test.exchange.domain.service.ExchangeRateProvider;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConversionApplicationServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-03T12:00:00Z"), ZoneOffset.UTC);

    /** In-memory fake standing in for the JPA adapter. */
    private static class InMemoryConversionRepository implements ConversionRepository {
        final List<Conversion> saved = new ArrayList<>();

        @Override
        public Conversion save(Conversion conversion) {
            Conversion withId = Conversion.reconstitute(
                    (long) saved.size() + 1,
                    conversion.originalAmount(), conversion.rate(),
                    conversion.convertedAmount(), conversion.occurredAt());
            saved.add(withId);
            return withId;
        }
    }

    private final InMemoryConversionRepository repository = new InMemoryConversionRepository();
    private final ExchangeRateProvider provider =
            (source, target) -> ExchangeRate.of(source, target, new BigDecimal("3.70"));
    private final ConversionApplicationService service = new ConversionApplicationService(
            new CurrencyConversionService(provider, CLOCK), repository);

    @Test
    void convertsAndReturnsTheResponseContract() {
        ConversionRequest request = new ConversionRequest(new BigDecimal("100"), "USD", "PEN");

        ConversionResponse response = service.convert(request);

        assertThat(response.monto()).isEqualByComparingTo("100");
        assertThat(response.montoConvertido()).isEqualByComparingTo("370.00");
        assertThat(response.monedaOrigen()).isEqualTo("USD");
        assertThat(response.monedaDestino()).isEqualTo("PEN");
        assertThat(response.tipoCambio()).isEqualByComparingTo("3.70");
    }

    @Test
    void persistsEveryConversion() {
        service.convert(new ConversionRequest(new BigDecimal("100"), "usd", "pen"));

        assertThat(repository.saved).hasSize(1);
        Conversion stored = repository.saved.get(0);
        assertThat(stored.id()).isEqualTo(1L);
        assertThat(stored.sourceCurrency().code()).isEqualTo("USD");
        assertThat(stored.targetCurrency().code()).isEqualTo("PEN");
        assertThat(stored.convertedAmount().amount()).isEqualByComparingTo("370.00");
        assertThat(stored.occurredAt()).isEqualTo(Instant.parse("2026-11-03T12:00:00Z").atOffset(ZoneOffset.UTC).toLocalDateTime());
    }

    @Test
    void propagatesDomainValidationErrorsWithoutPersisting() {
        assertThatThrownBy(() -> service.convert(new ConversionRequest(new BigDecimal("100"), "USD", "SOLES")))
                .isInstanceOf(InvalidCurrencyException.class);

        assertThat(repository.saved).isEmpty();
    }
}
