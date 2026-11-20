package com.example.activity4_group1
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.activity4_group1.databinding.ActivitySignUpBinding
import com.example.activity4_group1.utils.Validation
import com.example.activity4_group1.utils.ValidationResult
import com.example.activity4_group1.viewmodels.AuthState
import com.example.activity4_group1
.viewmodels.AuthViewModel
class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var authViewModel: AuthViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        setupClickListeners()
        observeAuthState()
    }
    private fun setupClickListeners() {
// Email/Password Sign Up only
        binding.btnSignUp.setOnClickListener {
            performSignUp()
        }
// Go back to Login
        binding.tvLogin.setOnClickListener {
            finish()
        }
    }
    private fun performSignUp() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()
        val nameValidation = Validation.validateName(name)
        if (nameValidation is ValidationResult.Error) {
            showError(nameValidation.message)
            return
        }
        val emailValidation = Validation.validateEmail(email)
        if (emailValidation is ValidationResult.Error) {
            showError(emailValidation.message)
            return
        }
        val passwordValidation = Validation.validatePassword(password)
        if (passwordValidation is ValidationResult.Error) {
            showError(passwordValidation.message)
            return
        }
        val confirmValidation = Validation.validateConfirmPassword(password, confirmPassword)
        if (confirmValidation is ValidationResult.Error) {
            showError(confirmValidation.message)
            return
        }
        authViewModel.signUp(email, password, name)
    }
    private fun observeAuthState() {
        authViewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> {
                    binding.progressBar.visibility = android.view.View.VISIBLE
                    binding.btnSignUp.isEnabled = false
                }
                is AuthState.Success -> {
                    binding.progressBar.visibility = android.view.View.GONE
                    Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.putExtra("USER_NAME", state.name)
                    startActivity(intent)
                    finish()
                }
                is AuthState.Error -> {
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.btnSignUp.isEnabled = true
                    showError(state.message)
                }
                else -> {}
            }
        }
    }
    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = android.view.View.VISIBLE
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
