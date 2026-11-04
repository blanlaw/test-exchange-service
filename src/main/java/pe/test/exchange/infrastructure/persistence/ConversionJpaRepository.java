package pe.test.exchange.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository for {@link ConversionJpaEntity}. Infrastructure only.
 */
public interface ConversionJpaRepository extends JpaRepository<ConversionJpaEntity, Long> {
}
