package com.example.activity4_group1.utils
import android.util.Patterns
object Validation {
    fun validateEmail(email: String): ValidationResult {
        return when {
            email.isEmpty() -> ValidationResult.Error("Email cannot be empty")
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> ValidationResult.Error("Invalid email format")
                else -> ValidationResult.Success
        }
    }
    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isEmpty() -> ValidationResult.Error("Password cannot be empty")
            password.length < 6 -> ValidationResult.Error("Password must be at least 6 characters")
            !password.matches(Regex(".*[A-Z].*")) -> ValidationResult.Error("Password must contain at least one uppercase letter")
                !password.matches(Regex(".*[0-9].*")) -> ValidationResult.Error("Password must contain at least one number")
                else -> ValidationResult.Success
        }
    }
    fun validateName(name: String): ValidationResult {
        return when {
            name.isEmpty() -> ValidationResult.Error("Name cannot be empty")
            name.length < 2 -> ValidationResult.Error("Name must be at least 2 characters")
            else -> ValidationResult.Success
        }
    }
    fun validateConfirmPassword(password: String, confirmPassword: String): ValidationResult {
        return if (password == confirmPassword) {
            ValidationResult.Success
        } else {
            ValidationResult.Error("Passwords do not match")
        }
    }
}
sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}