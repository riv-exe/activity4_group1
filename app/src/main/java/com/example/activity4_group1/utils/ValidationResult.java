package com.example.activity4_group1.utils;

import androidx.annotation.NonNull;

import java.util.Objects;

// Replaces Kotlin's sealed class. The private constructor means only the
// nested classes below can extend it, which mimics "sealed" without needing Java 17.
public abstract class ValidationResult {

    private ValidationResult() {
    }

    // Replaces: object Success : ValidationResult()
    public static final class Success extends ValidationResult {
        public static final Success INSTANCE = new Success();

        private Success() {
        }

        @NonNull
        @Override
        public String toString() {
            return "Success";
        }
    }

    // Replaces: data class Error(val message: String) : ValidationResult()
    public static final class Error extends ValidationResult {
        private final String message;

        public Error(@NonNull String message) {
            this.message = message;
        }

        @NonNull
        public String getMessage() {
            return message;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            return Objects.equals(message, ((Error) o).message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(message);
        }

        @NonNull
        @Override
        public String toString() {
            return "Error(message=" + message + ")";
        }
    }
}