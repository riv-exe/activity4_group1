package com.example.activity4_group1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.activity4_group1.databinding.ActivityLoginBinding;
import com.example.activity4_group1.utils.Validation;
import com.example.activity4_group1.utils.ValidationResult;
import com.example.activity4_group1.viewmodels.AuthState;
import com.example.activity4_group1.viewmodels.AuthViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthViewModel authViewModel;

    // Google Sign Up Launcher (for new account creation)
    private final ActivityResultLauncher<Intent> googleSignUpLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        android.util.Log.d("GOOGLE_AUTH",
                                "Result code: " + result.getResultCode());

                        Intent data = result.getData();

                        Task<GoogleSignInAccount> task =
                                GoogleSignIn.getSignedInAccountFromIntent(data);

                        try {
                            GoogleSignInAccount account =
                                    task.getResult(ApiException.class);

                            if (account.getIdToken() != null) {
                                authViewModel.signUpWithGoogle(
                                        account.getIdToken());
                            }

                        } catch (ApiException e) {
                            android.util.Log.e("GOOGLE_AUTH",
                                    "Google Sign-In error code: "
                                            + e.getStatusCode(), e);
                        }
                    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        authViewModel.setupGoogleSignUp(this); // Setup for Google Sign Up only

        setupClickListeners();
        observeAuthState();
    }

    private void setupClickListeners() {
        // Email/Password Sign In
        binding.btnSignIn.setOnClickListener(v -> performSignIn());

        // Google Sign Up Button
        binding.btnGoogleSignUp.setOnClickListener(v -> {
            authViewModel.getGoogleSignUpIntent(intent ->
                    googleSignUpLauncher.launch(intent)
            );
        });

        // Navigate to Sign Up screen (for email sign up)
        binding.tvSignUp.setOnClickListener(v ->
                startActivity(new Intent(this, SignUpActivity.class)));
    }

    private void performSignIn() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString();

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

        authViewModel.signIn(email, password);
    }

    private void observeAuthState() {
        authViewModel.getAuthState().observe(this, state -> {
            if (state instanceof AuthState.Loading) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.btnSignIn.setEnabled(false);
                binding.btnGoogleSignUp.setEnabled(false);

            } else if (state instanceof AuthState.Success) {
                String name = ((AuthState.Success) state).getName();
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSignIn.setEnabled(true);
                binding.btnGoogleSignUp.setEnabled(true);

                Toast.makeText(this, "Welcome, " + name + "!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, HomeActivity.class);
                intent.putExtra("USER_NAME", name);
                startActivity(intent);
                finish();

            } else if (state instanceof AuthState.Error) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSignIn.setEnabled(true);
                binding.btnGoogleSignUp.setEnabled(true);
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