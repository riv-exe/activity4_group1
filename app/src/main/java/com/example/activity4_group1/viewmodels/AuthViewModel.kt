package com.example.activity4_group1.viewmodels
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.activity4_group1.models.UserData
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database = Firebase.database.reference
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState
    private lateinit var googleSignInClient: GoogleSignInClient
    init {
// Check if user is already logged in
        if (auth.currentUser != null) {
            _authState.value = AuthState.Success(auth.currentUser?.displayName ?: "User")
        }
    }
    // ============ GOOGLE SIGN UP METHODS ============
    fun setupGoogleSignUp(context: Context) {
        val webClientId = getWebClientId(context)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .requestProfile()
            .build()
        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }
    private fun getWebClientId(context: Context): String {
        return try {
            val resId = context.resources.getIdentifier(
                "default_web_client_id",
                "string",
                context.packageName
            )
            if (resId != 0) {
                context.getString(resId)
            } else {
// If not found, you need to manually add your Web Client ID
// Get this from google-services.json (client_type: 3)
                "PUT_YOUR_"
            }
        } catch (e: Exception) {
            ""
        }
    }
    fun getGoogleSignUpIntent(): Intent {
        return googleSignInClient.signInIntent
    }
    fun signUpWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(credential).await()
                val user = authResult.user
                if (user != null) {
// Check if user already exists in database
                    val userExists = checkIfUserExists(user.uid)
                    if (userExists) {
// User already has an account - show error and sign out
                        auth.signOut()
                        _authState.value = AuthState.Error("Account already exists. Please sign in with  email instead.")
                    } else {
// New user - save to database
                        val name = user.displayName ?: user.email?.split("@")?.first() ?: "User"
                        saveUserToDatabase(user, name)
                        _authState.value = AuthState.Success(name)
                    }
                }
            } catch (e: Exception) {
                auth.signOut()
                _authState.value = AuthState.Error(e.message ?: "Google sign up failed")
            }
        }
    }
    // ============ EMAIL SIGN UP METHODS ============
    fun signUp(email: String, password: String, name: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                authResult.user?.updateProfile(profileUpdates)?.await()
                authResult.user?.reload()?.await()
                val updatedUser = auth.currentUser
                if (updatedUser != null) {
                    saveUserToDatabase(updatedUser, name)
                }
                _authState.value = AuthState.Success(name)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Sign up failed")
            }
        }
    }
    // ============ EMAIL SIGN IN METHODS ============
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                val user = authResult.user
                if (user != null) {
// Update last login only
                    database.child("users").child(user.uid).child("lastLogin")
                        .setValue(System.currentTimeMillis()).await()
                    _authState.value = AuthState.Success(user.displayName ?: "User")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Sign in failed")
            }
        }
    }
    // ============ DATABASE METHODS ============
    private suspend fun saveUserToDatabase(user: FirebaseUser, name: String) {
        try {
            val userData = UserData(
                userId = user.uid,
                name = name,
                email = user.email ?: "",
                createdAt = System.currentTimeMillis(),
                lastLogin = System.currentTimeMillis()
            )
            database.child("users").child(user.uid).setValue(userData).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private suspend fun checkIfUserExists(userId: String): Boolean {
        val snapshot = database.child("users").child(userId).get().await()
        return snapshot.exists()
    }
    suspend fun getUserData(userId: String): UserData? {
        return try {
            val snapshot = database.child("users").child(userId).get().await()
            snapshot.getValue(UserData::class.java)
        } catch (e: Exception) {
            null
        }
    }
    // ============ SIGN OUT METHOD ============
    fun signOut() {
        auth.signOut()
        if (::googleSignInClient.isInitialized) {
            googleSignInClient.signOut()
        }
        _authState.value = AuthState.SignedOut
    }
}
// ============ AUTH STATE SEALED CLASS ============
sealed class AuthState {
    object Loading : AuthState()
    data class Success(val name: String) : AuthState()
    data class Error(val message: String) : AuthState()
    object SignedOut : AuthState()
}