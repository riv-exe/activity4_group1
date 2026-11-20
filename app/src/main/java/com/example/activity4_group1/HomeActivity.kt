package com.example.activity4_group1
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.activity4_group1.databinding.ActivityHomeBinding
import com.example.activity4_group1.viewmodels.AuthState
import com.example.activity4_group1.viewmodels.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var authViewModel: AuthViewModel
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        auth = FirebaseAuth.getInstance()
        val userName = intent.getStringExtra("USER_NAME") ?: "User"
        binding.tvWelcome.text = "Hi, $userName!"
        loadUserData()
        setupClickListeners()
        observeAuthState()
    }
    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            lifecycleScope.launch {
                val userData = authViewModel.getUserData(currentUser.uid)
                if (userData != null) {
                    binding.tvUserInfo.text = """
Email: ${userData.email}
Member since: ${java.text.SimpleDateFormat("MMM dd, yyyy",
                        java.util.Locale.getDefault())
                        .format(java.util.Date(userData.createdAt))}
Last login: ${java.text.SimpleDateFormat("MMM dd, yyyy HH:mm",
                        java.util.Locale.getDefault())
                        .format(java.util.Date(userData.lastLogin))}
""".trimIndent()
                    binding.tvUserInfo.visibility = android.view.View.VISIBLE
                }
            }
        }
    }
    private fun setupClickListeners() {
        binding.btnLogout.setOnClickListener {
            authViewModel.signOut()
        }
    }
    private fun observeAuthState() {
        authViewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.SignedOut -> {
                    Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                else -> {}
            }
        }
    }
}