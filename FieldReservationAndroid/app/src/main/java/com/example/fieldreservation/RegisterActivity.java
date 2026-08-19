package com.example.fieldreservation;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private EditText etEmail, etPassword;
    private Button btnRegister;
    private Switch switchManager;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private UserApi userApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeComponents();
        setupClickListeners();
    }
    
    private void initializeComponents() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userApi = RetrofitClient.getClient().create(UserApi.class);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        switchManager = findViewById(R.id.switchManager);
        btnRegister = findViewById(R.id.btnRegister);
        
        // Assume a progress bar with id progressBar exists in XML layout
        progressBar = findViewById(R.id.progressBar);
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }
    
    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        boolean isManager = switchManager.isChecked();

        Log.d(TAG, "=== Starting registration ===");
        Log.d(TAG, "Email: " + email);
        Log.d(TAG, "Manager role enabled: " + isManager);

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show progress and disable button
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        btnRegister.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    // Hide progress and enable button
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    btnRegister.setEnabled(true);
                    
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            Log.d(TAG, "Firebase user created successfully. UID: " + firebaseUser.getUid());
                            saveUserToFirestore(firebaseUser.getUid(), email, isManager);
                        } else {
                            Log.e(TAG, "FirebaseUser is null after successful authentication");
                            Toast.makeText(RegisterActivity.this, "Registration error", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        handleRegistrationError(task.getException());
                    }
                });
    }
    
    private void saveUserToFirestore(String uid, String email, boolean isManager) {
        // Create a user document in Firestore
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("role", isManager ? "manager" : "user");
        
        Log.d(TAG, "Data to save in Firestore: " + user);

        db.collection("users")
            .document(uid)
            .set(user)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "=== User data saved successfully in Firestore ===");
                Log.d(TAG, "Registered role: " + user.get("role"));
                // Optionally sync with backend
                // syncUserWithBackend(uid, email, isManager);
                navigateToMainActivity();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "=== Error while saving to Firestore ===", e);
                Log.e(TAG, "Error message: " + e.getMessage());
                Toast.makeText(RegisterActivity.this, "Error saving user data: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            });
    }
    
    private void handleRegistrationError(Exception exception) {
        Log.e(TAG, "=== Registration failed ===", exception);
        String errorMessage = "Registration failed";
        
        if (exception instanceof FirebaseAuthUserCollisionException) {
            errorMessage = "This email is already registered";
        } else if (exception != null) {
            errorMessage = exception.getMessage();
        }
        
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }
    
    private void navigateToMainActivity() {
        Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
        finish();
    }

    private void syncUserWithBackend(String uid, String email, boolean isManager) {
        User user = new User(uid, email);
        user.setRole(isManager ? "manager" : "user");

        userApi.createUser(user).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    navigateToMainActivity();
                } else {
                    String errorMessage = "User sync failed! ";
                    try {
                        errorMessage += "Code: " + response.code() + ", Message: " + response.errorBody().string();
                    } catch (Exception e) {
                        errorMessage += "Could not get error details";
                    }
                    Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    Log.e(TAG, errorMessage);
                    
                    // Delete the Firebase user if backend sync fails
                    if (mAuth.getCurrentUser() != null) {
                        mAuth.getCurrentUser().delete()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Firebase user deleted after failed sync");
                                } else {
                                    Log.e(TAG, "Failed to delete Firebase user after failed sync");
                                }
                            });
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                String errorMessage = "Error: " + t.getMessage();
                Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Sync failed: " + errorMessage, t);
                
                // Delete the Firebase user if backend sync fails
                if (mAuth.getCurrentUser() != null) {
                    mAuth.getCurrentUser().delete()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Firebase user deleted after failed sync");
                            } else {
                                Log.e(TAG, "Failed to delete Firebase user after failed sync");
                            }
                        });
                }
            }
        });
    }
}
