package pe.test.exchange.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Uniform error body returned by every endpoint.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        List<String> details
) {

    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, List.of());
    }

    public static ErrorResponse of(int status, String error, String message, List<String> details) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, details);
    }
}
