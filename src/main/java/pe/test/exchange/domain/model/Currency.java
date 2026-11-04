package pe.test.exchange.domain.model;

import pe.test.exchange.domain.exception.InvalidCurrencyException;

import java.util.regex.Pattern;

/**
 * Value Object representing an ISO 4217 currency code (e.g. USD, PEN).
 * Immutable and validated on construction.
 */
public record Currency(String code) {

    private static final Pattern ISO_4217 = Pattern.compile("^[A-Z]{3}$");

    public Currency {
        if (code == null || !ISO_4217.matcher(code).matches()) {
            throw new InvalidCurrencyException(code);
        }
    }

    public static Currency of(String rawCode) {
        if (rawCode == null) {
            throw new InvalidCurrencyException(null);
        }
        return new Currency(rawCode.trim().toUpperCase());
    }

    @Override
    public String toString() {
        return code;
    }
}
