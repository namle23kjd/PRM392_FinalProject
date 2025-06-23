package com.example.prm392_finalproject.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_finalproject.MainActivity;
import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.controllers.UserRepository;
import com.example.prm392_finalproject.models.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Date;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnGoogleSignIn;
    private ProgressBar progressBar;
    private UserRepository userRepository;

    // Firebase & Google Sign-In
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize dependencies
        userRepository = new UserRepository();
        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign-In
        configureGoogleSignIn();

        // Initialize views
        initViews();
        
        // Set click listeners
        setClickListeners();
        
        // Register ActivityResultLauncher for Google Sign-In
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        handleGoogleSignInResult(task);
                    } else {
                        Log.w(TAG, "Google sign in failed or was cancelled.");
                        showProgress(false);
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            navigateToMainActivity();
        }
    }

    private void configureGoogleSignIn() {
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setClickListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());

        findViewById(R.id.tvRegister).setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.tvForgotPassword).setOnClickListener(v -> {
            Toast.makeText(this, "Forgot password feature coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    private void signInWithGoogle() {
        showProgress(true);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
            firebaseAuthWithGoogle(account.getIdToken());
        } catch (ApiException e) {
            Log.w(TAG, "Google sign in failed", e);
            Toast.makeText(this, "Google sign in failed: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            showProgress(false);
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        
                        if (firebaseUser != null) {
                            // Save Google user to MySQL database
                            saveGoogleUserToDatabase(firebaseUser);
                        }
                        
                        Toast.makeText(this, "Authentication successful.", Toast.LENGTH_SHORT).show();
                        navigateToMainActivity();
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                        showProgress(false);
                    }
                });
    }

    private void saveGoogleUserToDatabase(FirebaseUser firebaseUser) {
        // Run database operation on background thread
        new Thread(() -> {
            try {
                // Check if user already exists in database
                User existingUser = userRepository.getUserByEmail(firebaseUser.getEmail());
                
                if (existingUser == null) {
                    // Create new user in database
                    User newUser = new User();
                    newUser.setEmail(firebaseUser.getEmail());
                    
                    // Extract first and last name from display name
                    String displayName = firebaseUser.getDisplayName();
                    if (displayName != null && !displayName.isEmpty()) {
                        String[] nameParts = displayName.split(" ", 2);
                        if (nameParts.length >= 2) {
                            newUser.setFirstName(nameParts[0]);
                            newUser.setLastName(nameParts[1]);
                        } else {
                            newUser.setFirstName(displayName);
                            newUser.setLastName("");
                        }
                    } else {
                        // If no display name, use email prefix as first name
                        String email = firebaseUser.getEmail();
                        if (email != null) {
                            String emailPrefix = email.split("@")[0];
                            newUser.setFirstName(emailPrefix);
                            newUser.setLastName("");
                        }
                    }
                    
                    // Set default values for required fields
                    newUser.setGender("Other"); // Default gender
                    newUser.setPhone(""); // Empty phone
                    newUser.setAddress(""); // Empty address
                    newUser.setDob(new Date()); // Current date as DOB
                    newUser.setPassword("google_auth_" + System.currentTimeMillis()); // Generate a unique password
                    newUser.setRole("customer");
                    
                    // Save to database
                    boolean success = userRepository.registerUser(newUser);
                    
                    if (success) {
                        Log.d(TAG, "Google user saved to database: " + newUser.getEmail());
                        runOnUiThread(() -> {
                            Toast.makeText(LoginActivity.this, 
                                "Welcome, " + newUser.getFirstName() + "!", 
                                Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        Log.e(TAG, "Failed to save Google user to database");
                        runOnUiThread(() -> {
                            Toast.makeText(LoginActivity.this, 
                                "Warning: Could not save user to database", 
                                Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    Log.d(TAG, "Google user already exists in database: " + existingUser.getEmail());
                    runOnUiThread(() -> {
                        Toast.makeText(LoginActivity.this, 
                            "Welcome back, " + existingUser.getFirstName() + "!", 
                            Toast.LENGTH_SHORT).show();
                    });
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error saving Google user to database: " + e.getMessage());
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, 
                        "Error saving user data: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!validateInput(email, password)) {
            return;
        }

        showProgress(true);

        // Regular login with your own DB
        new Thread(() -> {
            User user = userRepository.loginUser(email, password);
            runOnUiThread(() -> {
                showProgress(false);
                if (user != null) {
                    Toast.makeText(LoginActivity.this, "Welcome back, " + user.getFirstName() + "!", Toast.LENGTH_LONG).show();
                    // You might want to navigate to MainActivity here too
                    // navigateToMainActivity(); 
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }

    private boolean validateInput(String email, String password) {
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email address");
            etEmail.requestFocus();
            return false;
        }
        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return false;
        }
        return true;
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
        btnGoogleSignIn.setEnabled(!show);
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
} 