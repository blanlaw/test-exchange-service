package pe.test.exchange.domain.exception;

public class InvalidCurrencyException extends DomainException {

    public InvalidCurrencyException(String code) {
        super("Invalid currency code: '" + code + "'. Expected a 3-letter ISO 4217 code (e.g. USD, PEN)");
    }
}
