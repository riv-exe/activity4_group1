package com.example.activity4_group1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.activity4_group1.databinding.ActivityHomeBinding;
import com.example.activity4_group1.viewmodels.AuthState;
import com.example.activity4_group1.viewmodels.AuthViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private AuthViewModel authViewModel;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        auth = FirebaseAuth.getInstance();

        String userName = getIntent().getStringExtra("USER_NAME");
        if (userName == null) {
            userName = "User";
        }
        binding.tvWelcome.setText("Hi, " + userName + "!");

        loadUserData();
        setupClickListeners();
        observeAuthState();
    }

    private void loadUserData() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            // Assumes getUserData now takes a callback instead of being a suspend function
            authViewModel.getUserData(currentUser.getUid(), userData -> {
                if (userData != null && !isFinishing() && !isDestroyed()) {
                    SimpleDateFormat createdFormat =
                            new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                    SimpleDateFormat loginFormat =
                            new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

                    String info = "Email: " + userData.getEmail() + "\n"
                            + "Member since: " + createdFormat.format(new Date(userData.getCreatedAt())) + "\n"
                            + "Last login: " + loginFormat.format(new Date(userData.getLastLogin()));

                    binding.tvUserInfo.setText(info);
                    binding.tvUserInfo.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private void setupClickListeners() {
        binding.btnLogout.setOnClickListener(v -> authViewModel.signOut());
    }

    private void observeAuthState() {
        authViewModel.getAuthState().observe(this, state -> {
            if (state instanceof AuthState.SignedOut) {
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}