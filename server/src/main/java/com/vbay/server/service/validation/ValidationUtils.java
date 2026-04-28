package com.vbay.server.service.validation;

import java.math.BigDecimal;
import java.util.List;

import com.vbay.server.exception.ValidationException;

public class ValidationUtils {
    public static void requireNotNull(Object value, String message) {
        if (value == null) {
            throw new ValidationException(message);
        }
    }

    public static void requireNotEmpty(List<?> value, String message) {
    if (value == null || value.isEmpty()) {
        throw new ValidationException(message);
    }
}

    public static void requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(message);
        }
    }
    public static void requireNotBlank(char[] value, String message) {
        if (value == null || value.length == 0) {
            throw new ValidationException(message);
        }
    }

    public static void requirePositive(Long value, String message) {
        if (value == null || value <= 0) {
            throw new ValidationException(message);
        }
    }

    public static void requirePositive(BigDecimal value, String message) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(message);
        }
    }
}
