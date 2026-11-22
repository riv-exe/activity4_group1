package com.example.activity4_group1.utils;

import android.util.Patterns;

import androidx.annotation.NonNull;

public final class Validation {

    // Replaces Kotlin's "object": static methods only, no instances
    private Validation() {
    }

    @NonNull
    public static ValidationResult validateEmail(@NonNull String email) {
        if (email.isEmpty()) {
            return new ValidationResult.Error("Email cannot be empty");
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return new ValidationResult.Error("Invalid email format");
        }
        return ValidationResult.Success.INSTANCE;
    }

    @NonNull
    public static ValidationResult validatePassword(@NonNull String password) {
        if (password.isEmpty()) {
            return new ValidationResult.Error("Password cannot be empty");
        } else if (password.length() < 6) {
            return new ValidationResult.Error("Password must be at least 6 characters");
        } else if (!password.matches(".*[A-Z].*")) {
            return new ValidationResult.Error("Password must contain at least one uppercase letter");
        } else if (!password.matches(".*[0-9].*")) {
            return new ValidationResult.Error("Password must contain at least one number");
        }
        return ValidationResult.Success.INSTANCE;
    }

    @NonNull
    public static ValidationResult validateName(@NonNull String name) {
        if (name.isEmpty()) {
            return new ValidationResult.Error("Name cannot be empty");
        } else if (name.length() < 2) {
            return new ValidationResult.Error("Name must be at least 2 characters");
        }
        return ValidationResult.Success.INSTANCE;
    }

    @NonNull
    public static ValidationResult validateConfirmPassword(@NonNull String password,
                                                           @NonNull String confirmPassword) {
        if (password.equals(confirmPassword)) {
            return ValidationResult.Success.INSTANCE;
        }
        return new ValidationResult.Error("Passwords do not match");
    }
}