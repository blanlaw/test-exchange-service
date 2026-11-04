package pe.test.exchange.domain.exception;

import java.math.BigDecimal;

public class InvalidAmountException extends DomainException {

    public InvalidAmountException(BigDecimal amount) {
        super("Invalid amount: '" + amount + "'. Amount must be greater than zero");
    }
}
