package pe.test.exchange.infrastructure.persistence;

import org.springframework.stereotype.Repository;
import pe.test.exchange.domain.model.Conversion;
import pe.test.exchange.domain.model.Currency;
import pe.test.exchange.domain.model.ExchangeRate;
import pe.test.exchange.domain.model.Money;
import pe.test.exchange.domain.repository.ConversionRepository;

/**
 * Adapter implementing the domain {@link ConversionRepository} port on top of
 * Spring Data JPA. Maps between the aggregate and its JPA entity.
 */
@Repository
public class ConversionRepositoryAdapter implements ConversionRepository {

    private final ConversionJpaRepository jpaRepository;

    public ConversionRepositoryAdapter(ConversionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Conversion save(Conversion conversion) {
        ConversionJpaEntity saved = jpaRepository.save(toEntity(conversion));
        return toDomain(saved);
    }

    private static ConversionJpaEntity toEntity(Conversion conversion) {
        return new ConversionJpaEntity(
                conversion.id(),
                conversion.originalAmount().amount(),
                conversion.sourceCurrency().code(),
                conversion.targetCurrency().code(),
                conversion.rate().value(),
                conversion.convertedAmount().amount(),
                conversion.occurredAt()
        );
    }

    private static Conversion toDomain(ConversionJpaEntity entity) {
        Currency source = Currency.of(entity.getSourceCurrency());
        Currency target = Currency.of(entity.getTargetCurrency());
        return Conversion.reconstitute(
                entity.getId(),
                Money.of(entity.getOriginalAmount(), source),
                ExchangeRate.of(source, target, entity.getExchangeRate()),
                Money.of(entity.getConvertedAmount(), target),
                entity.getOccurredAt()
        );
    }
}
