package com.example.activity4_group1.viewmodels;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.activity4_group1.models.UserData;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AuthViewModel extends ViewModel {

    // Replaces the suspend function getUserData(): results arrive through this callback
    public interface UserDataCallback {
        void onResult(@Nullable UserData userData);
    }

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    private final MutableLiveData<AuthState> authState = new MutableLiveData<>();

    private GoogleSignInClient googleSignInClient;

    public AuthViewModel() {
        // Check if user is already logged in
        FirebaseUser current = auth.getCurrentUser();
        if (current != null) {
            String displayName = current.getDisplayName();
            authState.setValue(new AuthState.Success(displayName != null ? displayName : "User"));
        }
    }

    public LiveData<AuthState> getAuthState() {
        return authState;
    }

    // ============ GOOGLE SIGN UP METHODS ============

    public void setupGoogleSignUp(@NonNull Context context) {
        String webClientId = getWebClientId(context);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .requestProfile()
                .build();
        // Application context so the ViewModel doesn't hold on to the Activity
        googleSignInClient = GoogleSignIn.getClient(context.getApplicationContext(), gso);
    }

    private String getWebClientId(Context context) {
        try {
            int resId = context.getResources().getIdentifier(
                    "default_web_client_id",
                    "string",
                    context.getPackageName());
            if (resId != 0) {
                return context.getString(resId);
            } else {
                // If not found, you need to manually add your Web Client ID
                // Get this from google-services.json (client_type: 3)
                return "PUT_YOUR_";
            }
        } catch (Exception e) {
            return "";
        }
    }

    public Intent getGoogleSignUpIntent() {
        return googleSignInClient.getSignInIntent();
    }

    public void signUpWithGoogle(@NonNull String idToken) {
        authState.setValue(AuthState.Loading.INSTANCE);

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user == null) {
                        failGoogleSignUp(null);
                        return;
                    }

                    // Check if user already exists in database
                    database.child("users").child(user.getUid()).get()
                            .addOnSuccessListener(snapshot -> {
                                if (snapshot.exists()) {
                                    // User already has an account - show error and sign out
                                    auth.signOut();
                                    authState.setValue(new AuthState.Error(
                                            "Account already exists. Please sign in with  email instead."));
                                } else {
                                    // New user - save to database
                                    String name = resolveGoogleName(user);
                                    saveUserToDatabase(user, name, () ->
                                            authState.setValue(new AuthState.Success(name)));
                                }
                            })
                            .addOnFailureListener(this::failGoogleSignUp);
                })
                .addOnFailureListener(this::failGoogleSignUp);
    }

    private void failGoogleSignUp(@Nullable Exception e) {
        auth.signOut();
        authState.setValue(new AuthState.Error(errorMessage(e, "Google sign up failed")));
    }

    private String resolveGoogleName(FirebaseUser user) {
        String name = user.getDisplayName();
        if (name != null) return name;
        String email = user.getEmail();
        if (email != null) return email.split("@")[0];
        return "User";
    }

    // ============ EMAIL SIGN UP METHODS ============

    public void signUp(@NonNull String email, @NonNull String password, @NonNull String name) {
        authState.setValue(AuthState.Loading.INSTANCE);

        auth.createUserWithEmailAndPassword(email, password)
                .onSuccessTask(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user == null) {
                        return Tasks.forResult((Void) null);
                    }
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build();
                    return user.updateProfile(profileUpdates)
                            .onSuccessTask(unused -> user.reload());
                })
                .addOnSuccessListener(unused -> {
                    FirebaseUser updatedUser = auth.getCurrentUser();
                    if (updatedUser != null) {
                        saveUserToDatabase(updatedUser, name, () ->
                                authState.setValue(new AuthState.Success(name)));
                    } else {
                        authState.setValue(new AuthState.Success(name));
                    }
                })
                .addOnFailureListener(e ->
                        authState.setValue(new AuthState.Error(errorMessage(e, "Sign up failed"))));
    }

    // ============ EMAIL SIGN IN METHODS ============

    public void signIn(@NonNull String email, @NonNull String password) {
        authState.setValue(AuthState.Loading.INSTANCE);

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user == null) {
                        authState.setValue(new AuthState.Error("Sign in failed"));
                        return;
                    }

                    // Update last login only
                    database.child("users").child(user.getUid()).child("lastLogin")
                            .setValue(System.currentTimeMillis())
                            .addOnSuccessListener(unused -> {
                                String displayName = user.getDisplayName();
                                authState.setValue(new AuthState.Success(
                                        displayName != null ? displayName : "User"));
                            })
                            .addOnFailureListener(e ->
                                    authState.setValue(new AuthState.Error(
                                            errorMessage(e, "Sign in failed"))));
                })
                .addOnFailureListener(e ->
                        authState.setValue(new AuthState.Error(errorMessage(e, "Sign in failed"))));
    }

    // ============ DATABASE METHODS ============

    // Like the original, a failed save is only logged; onDone runs either way
    private void saveUserToDatabase(@NonNull FirebaseUser user,
                                    @NonNull String name,
                                    @NonNull Runnable onDone) {
        try {
            long now = System.currentTimeMillis();
            String email = user.getEmail() != null ? user.getEmail() : "";
            UserData userData = new UserData(user.getUid(), name, email, now, now);

            database.child("users").child(user.getUid()).setValue(userData)
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful() && task.getException() != null) {
                            task.getException().printStackTrace();
                        }
                        onDone.run();
                    });
        } catch (Exception e) {
            e.printStackTrace();
            onDone.run();
        }
    }

    // Callback runs on the main thread. userData is null if the read fails or no data exists.
    public void getUserData(@NonNull String userId, @NonNull UserDataCallback callback) {
        database.child("users").child(userId).get()
                .addOnSuccessListener(snapshot -> {
                    UserData data = null;
                    try {
                        data = snapshot.getValue(UserData.class);
                    } catch (Exception ignored) {
                    }
                    callback.onResult(data);
                })
                .addOnFailureListener(e -> callback.onResult(null));
    }

    // ============ SIGN OUT METHOD ============

    public void signOut() {
        auth.signOut();
        if (googleSignInClient != null) {
            googleSignInClient.signOut();
        }
        authState.setValue(AuthState.SignedOut.INSTANCE);
    }

    private String errorMessage(@Nullable Exception e, @NonNull String fallback) {
        return (e != null && e.getMessage() != null) ? e.getMessage() : fallback;
    }
}