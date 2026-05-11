package com.vbay.ui.util;

import java.math.BigDecimal;
import java.util.function.UnaryOperator;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

public final class MoneyInput {
    private static final String MONEY_PATTERN = "\\d*(\\.\\d{0,2})?";

    private MoneyInput() {
    }

    public static void install(TextField textField) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            return newText.matches(MONEY_PATTERN) ? change : null;
        };
        textField.setTextFormatter(new TextFormatter<>(filter));
    }

    public static BigDecimal parseRequired(TextField textField, String fieldName) {
        String value = textField.getText();
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return parse(value, fieldName);
    }

    public static BigDecimal parseOptional(TextField textField, String fieldName) {
        String value = textField.getText();
        if (value == null || value.isBlank()) {
            return null;
        }
        return parse(value, fieldName);
    }

    public static BigDecimal parse(String rawValue, String fieldName) {
        String normalized = rawValue.trim().replace(",", "");
        if (!normalized.matches("\\d+(\\.\\d{1,2})?")) {
            throw new IllegalArgumentException(fieldName + " must be a valid amount with at most 2 decimal places.");
        }
        return new BigDecimal(normalized);
    }

    public static String toInputText(BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }
}
