package com.example.activity4_group1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.activity4_group1.databinding.ActivitySignUpBinding;
import com.example.activity4_group1.utils.Validation;
import com.example.activity4_group1.utils.ValidationResult;
import com.example.activity4_group1.viewmodels.AuthState;
import com.example.activity4_group1.viewmodels.AuthViewModel;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();


        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupClickListeners();
        observeAuthState();
    }

    private void setupClickListeners() {
        // Back to GetStartedActivity
        binding.btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, GetStartedActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // Email/Password Sign Up only
        binding.btnSignUp.setOnClickListener(v -> performSignUp());

        // Go back to Login
        binding.tvLogin.setOnClickListener(v -> finish());
    }

    private void performSignUp() {
        String name = binding.etName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString();
        String confirmPassword = binding.etConfirmPassword.getText().toString();

        ValidationResult nameValidation = Validation.validateName(name);
        if (nameValidation instanceof ValidationResult.Error) {
            showError(((ValidationResult.Error) nameValidation).getMessage());
            return;
        }

        ValidationResult emailValidation = Validation.validateEmail(email);
        if (emailValidation instanceof ValidationResult.Error) {
            showError(((ValidationResult.Error) emailValidation).getMessage());
            return;
        }

        ValidationResult passwordValidation = Validation.validatePassword(password);
        if (passwordValidation instanceof ValidationResult.Error) {
            showError(((ValidationResult.Error) passwordValidation).getMessage());
            return;
        }

        ValidationResult confirmValidation =
                Validation.validateConfirmPassword(password, confirmPassword);
        if (confirmValidation instanceof ValidationResult.Error) {
            showError(((ValidationResult.Error) confirmValidation).getMessage());
            return;
        }

        authViewModel.signUp(email, password, name);
    }

    private void observeAuthState() {
        authViewModel.getAuthState().observe(this, state -> {
            if (state instanceof AuthState.Loading) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.btnSignUp.setEnabled(false);

            } else if (state instanceof AuthState.Success) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(this, HomeActivity.class);
                intent.putExtra("USER_NAME", ((AuthState.Success) state).getName());
                startActivity(intent);
                finish();

            } else if (state instanceof AuthState.Error) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSignUp.setEnabled(true);
                showError(((AuthState.Error) state).getMessage());
            }
        });
    }

    private void showError(@NonNull String message) {
        binding.tvError.setText(message);
        binding.tvError.setVisibility(View.VISIBLE);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}