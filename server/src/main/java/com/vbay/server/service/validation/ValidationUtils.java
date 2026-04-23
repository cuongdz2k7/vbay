package com.vbay.server.service.validation;

import com.vbay.server.exception.ValidationException;

public class ValidationUtils {
    public static void requireNotNull(Object value, String message) {
        if (value == null) {
            throw new ValidationException(message);
        }
    }

    public static void requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(message);
        }
    }

    public static void requirePositive(Long value, String message) {
        if (value == null || value <= 0) {
            throw new ValidationException(message);
        }
    }

    public static void requirePositive(Double value, String message) {
        if (value == null || value <= 0) {
            throw new ValidationException(message);
        }
    }
}
