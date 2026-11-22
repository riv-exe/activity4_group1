package com.example.activity4_group1.viewmodels;

import androidx.annotation.NonNull;

import java.util.Objects;

// Replaces Kotlin's sealed class (same pattern as ValidationResult)
public abstract class AuthState {

    private AuthState() {
    }

    public static final class Loading extends AuthState {
        public static final Loading INSTANCE = new Loading();

        private Loading() {
        }
    }

    public static final class Success extends AuthState {
        private final String name;

        public Success(@NonNull String name) {
            this.name = name;
        }

        @NonNull
        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            return Objects.equals(name, ((Success) o).name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }

    public static final class Error extends AuthState {
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
    }

    public static final class SignedOut extends AuthState {
        public static final SignedOut INSTANCE = new SignedOut();

        private SignedOut() {
        }
    }
}