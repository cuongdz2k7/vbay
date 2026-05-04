package com.vbay.server.security;

public class PasswordPolicy {
    private final int minLength;
    private final boolean requireUppercase;
    private final boolean requireLowercase;
    private final boolean requireDigit;
    private final boolean requireSpecial;
    private final double minStrengthToCreate;

    public PasswordPolicy(int minLength,
                          boolean requireUppercase,
                          boolean requireLowercase,
                          boolean requireDigit,
                          boolean requireSpecial,
                          double minStrengthToCreate) {
        this.minLength = minLength;
        this.requireUppercase = requireUppercase;
        this.requireLowercase = requireLowercase;
        this.requireDigit = requireDigit;
        this.requireSpecial = requireSpecial;
        this.minStrengthToCreate = minStrengthToCreate;
    }

    public boolean hasUppercase(char[] password) {
        if (password == null) {
            return false;
        }
        for (char c : password) {
            if (Character.isUpperCase(c)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasLowercase(char[] password) {
        if (password == null) {
            return false;
        }
        for (char c : password) {
            if (Character.isLowerCase(c)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasDigit(char[] password) {
        if (password == null) {
            return false;
        }
        for (char c : password) {
            if (Character.isDigit(c)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasSpecial(char[] password) {
        if (password == null) {
            return false;
        }
        for (char c : password) {
            if (!Character.isLetterOrDigit(c)) {
                return true;
            }
        }
        return false;
    }

    public ValidationResult validateBasicRules(char[] password) {
        boolean lengthOk = password != null && password.length >= minLength;
        boolean upperOk = !requireUppercase || hasUppercase(password);
        boolean lowerOk = !requireLowercase || hasLowercase(password);
        boolean digitOk = !requireDigit || hasDigit(password);
        boolean specialOk = !requireSpecial || hasSpecial(password);

        boolean valid = lengthOk && upperOk && lowerOk && digitOk && specialOk;

        return new ValidationResult(lengthOk, upperOk, lowerOk, digitOk, specialOk, valid);
    }

    public double evaluateStrength(char[] password) {
        if (password == null || password.length == 0) {
            return 0.0;
        }

        int idealLength = Math.max(minLength + 4, 12);

        double lengthScore = Math.min((double) password.length / idealLength, 1.0);

        int groupCount = 0;
        if (hasLowercase(password)) groupCount++;
        if (hasUppercase(password)) groupCount++;
        if (hasDigit(password)) groupCount++;
        if (hasSpecial(password)) groupCount++;

        double varietyScore = groupCount / 4.0;

        double bonusScore = 0.0;
        if (groupCount == 4) {
            bonusScore += 0.5;
        }
        if (password.length >= minLength + 4) {
            bonusScore += 0.5;
        }

        double score = lengthScore * 0.4 + varietyScore * 0.4 + bonusScore * 0.2;
        return Math.max(0.0, Math.min(score, 1.0));
    }

    public boolean canCreateAccount(char[] password) {
        ValidationResult validation = validateBasicRules(password);
        double strength = evaluateStrength(password);
        return validation.isValid() && strength > minStrengthToCreate;
    }

    public static class ValidationResult {
        private final boolean lengthOk;
        private final boolean uppercaseOk;
        private final boolean lowercaseOk;
        private final boolean digitOk;
        private final boolean specialOk;
        private final boolean valid;

        public ValidationResult(boolean lengthOk,
                                boolean uppercaseOk,
                                boolean lowercaseOk,
                                boolean digitOk,
                                boolean specialOk,
                                boolean valid) {
            this.lengthOk = lengthOk;
            this.uppercaseOk = uppercaseOk;
            this.lowercaseOk = lowercaseOk;
            this.digitOk = digitOk;
            this.specialOk = specialOk;
            this.valid = valid;
        }

        public boolean isLengthOk() {
            return lengthOk;
        }

        public boolean isUppercaseOk() {
            return uppercaseOk;
        }

        public boolean isLowercaseOk() {
            return lowercaseOk;
        }

        public boolean isDigitOk() {
            return digitOk;
        }

        public boolean isSpecialOk() {
            return specialOk;
        }

        public boolean isValid() {
            return valid;
        }
    }
}
/*
Ý nghĩa công thức strength
Điểm 0 -> 1 ở đây gồm:

40% từ độ dài
40% từ độ đa dạng nhóm ký tự
20% bonus nếu đủ mạnh hơn mức tối thiểu


*/
