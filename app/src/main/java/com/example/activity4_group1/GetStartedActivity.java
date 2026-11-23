
package com.example.activity4_group1;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.activity4_group1.databinding.ActivityGetStartedBinding;

public class GetStartedActivity extends AppCompatActivity {

    private ActivityGetStartedBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityGetStartedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        binding.btnSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(GetStartedActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        binding.btnSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(GetStartedActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }
}
