package pe.test.exchange.domain.exception;

public class UnsupportedCurrencyException extends DomainException {

    public UnsupportedCurrencyException(String code) {
        super("Currency '" + code + "' is not supported by the exchange rate provider");
    }
}
