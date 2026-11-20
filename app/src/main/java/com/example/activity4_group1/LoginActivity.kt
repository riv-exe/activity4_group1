package com.example.activity4_group1
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.example.activity4_group1.databinding.ActivityLoginBinding
import com.example.activity4_group1.utils.Validation
import com.example.activity4_group1.utils.ValidationResult
import com.example.activity4_group1.viewmodels.AuthState
import com.example.activity4_group1.viewmodels.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var authViewModel: AuthViewModel
    // Google Sign Up Launcher (for new account creation)
    private val googleSignUpLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.idToken?.let { idToken ->
                    authViewModel.signUpWithGoogle(idToken) // Sign Up only
                }
            } catch (e: ApiException) {
                showError("Google sign up failed: ${e.message}")
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        authViewModel.setupGoogleSignUp(this) // Setup for Google Sign Up only
        setupClickListeners()
        observeAuthState()
    }
    private fun setupClickListeners() {
// Email/Password Sign In
        binding.btnSignIn.setOnClickListener {
            performSignIn()
        }
// Google Sign Up Button
        binding.btnGoogleSignUp.setOnClickListener {
            val signUpIntent = authViewModel.getGoogleSignUpIntent()
            googleSignUpLauncher.launch(signUpIntent)
        }
// Navigate to Sign Up screen (for email sign up)
        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
    private fun performSignIn() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
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
        authViewModel.signIn(email, password)
    }
    private fun observeAuthState() {
        authViewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> {
                    binding.progressBar.visibility = android.view.View.VISIBLE
                    binding.btnSignIn.isEnabled = false
                    binding.btnGoogleSignUp.isEnabled = false
                }
                is AuthState.Success -> {
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.btnSignIn.isEnabled = true
                    binding.btnGoogleSignUp.isEnabled = true
                    Toast.makeText(this, "Welcome, ${state.name}!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.putExtra("USER_NAME", state.name)
                    startActivity(intent)
                    finish()
                }
                is AuthState.Error -> {
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.btnSignIn.isEnabled = true
                    binding.btnGoogleSignUp.isEnabled = true
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