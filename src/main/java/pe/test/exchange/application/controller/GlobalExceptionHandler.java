package pe.test.exchange.application.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pe.test.exchange.application.dto.ErrorResponse;
import pe.test.exchange.domain.exception.DomainException;
import pe.test.exchange.domain.exception.ExchangeRateUnavailableException;
import pe.test.exchange.domain.exception.InvalidAmountException;
import pe.test.exchange.domain.exception.InvalidCurrencyException;
import pe.test.exchange.domain.exception.InvalidExchangeRateException;
import pe.test.exchange.domain.exception.UnsupportedCurrencyException;

import java.util.List;

/**
 * Maps domain and framework exceptions to HTTP responses with a uniform body.
 *
 * <ul>
 *   <li>400 – the client sent something the service cannot interpret (malformed JSON, invalid fields, invalid currency/amount)</li>
 *   <li>422 – the request is well formed but the provider does not quote that currency</li>
 *   <li>503 – the external exchange-rate provider is down or answered with an error</li>
 *   <li>500 – anything unexpected; details are logged, never exposed</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(GlobalExceptionHandler::describe)
                .sorted()
                .toList();
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "La solicitud contiene campos inválidos", details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "El cuerpo de la solicitud no es un JSON válido", List.of());
    }

    @ExceptionHandler({InvalidCurrencyException.class})
    public ResponseEntity<ErrorResponse> handleInvalidCurrency(DomainException ex) {
        return build(HttpStatus.BAD_REQUEST, "INVALID_CURRENCY", ex.getMessage(), List.of());
    }

    @ExceptionHandler({InvalidAmountException.class, InvalidExchangeRateException.class})
    public ResponseEntity<ErrorResponse> handleInvalidAmount(DomainException ex) {
        return build(HttpStatus.BAD_REQUEST, "INVALID_AMOUNT", ex.getMessage(), List.of());
    }

    @ExceptionHandler(UnsupportedCurrencyException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedCurrency(UnsupportedCurrencyException ex) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "UNSUPPORTED_CURRENCY", ex.getMessage(), List.of());
    }

    @ExceptionHandler(ExchangeRateUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleProviderUnavailable(ExchangeRateUnavailableException ex) {
        log.error("Exchange rate provider unavailable", ex);
        return build(HttpStatus.SERVICE_UNAVAILABLE, "EXCHANGE_RATE_UNAVAILABLE",
                "El servicio de tipo de cambio no está disponible. Intente nuevamente más tarde", List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unexpected error handling request", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Ocurrió un error inesperado", List.of());
    }

    private static String describe(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }

    private static ResponseEntity<ErrorResponse> build(HttpStatus status, String error, String message, List<String> details) {
        return ResponseEntity.status(status).body(ErrorResponse.of(status.value(), error, message, details));
    }
}
