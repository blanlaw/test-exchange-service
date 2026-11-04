package pe.test.exchange.domain.exception;

public class ExchangeRateUnavailableException extends DomainException {

    public ExchangeRateUnavailableException(String detail, Throwable cause) {
        super("Exchange rate service is unavailable: " + detail, cause);
    }

    public ExchangeRateUnavailableException(String detail) {
        super("Exchange rate service is unavailable: " + detail);
    }
}
