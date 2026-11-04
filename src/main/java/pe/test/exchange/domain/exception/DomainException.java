package pe.test.exchange.domain.exception;

/**
 * Base class for all domain-level errors. Keeps the domain free of
 * framework-specific exception types.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
