package pe.test.exchange.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.test.exchange.application.dto.ConversionRequest;
import pe.test.exchange.application.dto.ConversionResponse;
import pe.test.exchange.application.mapper.ConversionMapper;
import pe.test.exchange.domain.model.Conversion;
import pe.test.exchange.domain.model.Currency;
import pe.test.exchange.domain.model.Money;
import pe.test.exchange.domain.repository.ConversionRepository;
import pe.test.exchange.domain.service.CurrencyConversionService;

/**
 * Use case: convert an amount between two currencies and record the operation.
 * Orchestrates the domain; contains no business rules itself.
 */
@Service
public class ConversionApplicationService {

    private final CurrencyConversionService currencyConversionService;
    private final ConversionRepository conversionRepository;

    public ConversionApplicationService(CurrencyConversionService currencyConversionService,
                                        ConversionRepository conversionRepository) {
        this.currencyConversionService = currencyConversionService;
        this.conversionRepository = conversionRepository;
    }

    @Transactional
    public ConversionResponse convert(ConversionRequest request) {
        Money original = ConversionMapper.toMoney(request);
        Currency target = ConversionMapper.toTargetCurrency(request);

        Conversion conversion = currencyConversionService.convert(original, target);
        Conversion persisted = conversionRepository.save(conversion);

        return ConversionMapper.toResponse(persisted);
    }
}
