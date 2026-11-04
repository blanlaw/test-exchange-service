package pe.test.exchange.domain.exception;

import java.math.BigDecimal;

public class InvalidExchangeRateException extends DomainException {

    public InvalidExchangeRateException(BigDecimal rate) {
        super("Invalid exchange rate: '" + rate + "'. Rate must be greater than zero");
    }
}
