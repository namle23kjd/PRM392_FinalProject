package com.example.prm392_finalproject.views;

import android.content.Intent;
import android.content.SharedPreferences;
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

import android.app.AlertDialog;
import android.text.InputType;
import android.widget.EditText;

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

        findViewById(R.id.tvForgotPassword).setOnClickListener(v -> showForgotPasswordDialog());
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

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                showProgress(false);
                if (task.isSuccessful()) {
                    // Đăng nhập Firebase thành công
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    if (firebaseUser != null) {
                        // Kiểm tra user trong MySQL
                        new Thread(() -> {
                            User user = userRepository.getUserByEmail(firebaseUser.getEmail());
                            if (user == null) {
                                // Nếu chưa có, tạo mới user trong MySQL
                                User newUser = new User();
                                newUser.setEmail(firebaseUser.getEmail());
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
                                    String emailPrefix = firebaseUser.getEmail().split("@")[0];
                                    newUser.setFirstName(emailPrefix);
                                    newUser.setLastName("");
                                }
                                newUser.setGender("Other");
                                newUser.setPhone("");
                                newUser.setAddress("");
                                newUser.setDob(new java.util.Date());
                                newUser.setPassword("firebase_auth_" + System.currentTimeMillis());
                                newUser.setRole("customer");
                                userRepository.registerUser(newUser);
                            }
                            runOnUiThread(() -> {
                                Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_LONG).show();
                                navigateToMainActivity();
                            });
                        }).start();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
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
        // Lấy user hiện tại từ MySQL (theo email Firebase)
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            new Thread(() -> {
                User user = userRepository.getUserByEmail(firebaseUser.getEmail());
                runOnUiThread(() -> {
                    if (user != null) {
                        String userRole = user.getRole().toLowerCase().trim();

                        if ("admin".equals(userRole)) {
                            // Navigate to AdminActivity for admin users
                            Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
                            intent.putExtra("currentUser", user);
                            startActivity(intent);

                        } else if ("staff".equals(userRole)) {
                            // Navigate to StaffActivity for staff users
                            Intent intent = new Intent(LoginActivity.this, StaffActivity.class);
                            intent.putExtra("currentUser", user);

                            // Lưu staff info vào SharedPreferences
                            android.content.SharedPreferences prefs = getSharedPreferences("staff_session", MODE_PRIVATE);
                            android.content.SharedPreferences.Editor editor = prefs.edit();
                            editor.putInt("staff_id", user.getCustomerId()); // hoặc user.getUserId() nếu có
                            editor.putString("staff_name", user.getFullName());
                            editor.putString("staff_email", user.getEmail());
                            editor.apply();

                            startActivity(intent);

                        } else {
                            // Navigate to UserDashboardActivity for customer users
                            // Lưu customer_id vào SharedPreferences cho customer
                            android.content.SharedPreferences prefs = getSharedPreferences("customer_session", MODE_PRIVATE);
                            android.content.SharedPreferences.Editor editor = prefs.edit();
                            editor.putInt("customer_id", user.getCustomerId());
                            editor.putString("customer_name", user.getFullName());
                            editor.putString("customer_address", user.getAddress());
                            editor.apply();

                            Intent intent = new Intent(LoginActivity.this, UserDashboardActivity.class);
                            startActivity(intent);
                        }
                    } else {
                      
                        Intent intent = new Intent(LoginActivity.this, UserDashboardActivity.class);
                        startActivity(intent);
                    }
                    finish();
                });
            }).start();
        } else {
            // Không có Firebase user, redirect về customer dashboard
            Intent intent = new Intent(LoginActivity.this, UserDashboardActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Password");
        builder.setMessage("Enter your email to receive a password reset link:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        input.setHint("Email");
        builder.setView(input);

        builder.setPositiveButton("Send", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }
            sendPasswordResetEmail(email);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void sendPasswordResetEmail(String email) {
        showProgress(true);
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener(task -> {
                showProgress(false);
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Check your email to reset your password.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Email not found or error occurred.", Toast.LENGTH_LONG).show();
                }
            });
    }
    private void performLogout() {
        try {
            // Show loading message
            Toast.makeText(this, "Đang đăng xuất...", Toast.LENGTH_SHORT).show();

            // 1. Logout khỏi Firebase Auth
            FirebaseAuth.getInstance().signOut();

            // 2. Logout khỏi Google Sign-In (nếu có)
            try {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
                googleSignInClient.signOut();
            } catch (Exception e) {
                Log.w(TAG, "Google sign out error (can be ignored): " + e.getMessage());
            }

            // 3. Xóa session data trong SharedPreferences
            clearAllSessions();

            // 4. Chuyển về LoginActivity và clear back stack
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            // 5. Hiển thị thông báo logout thành công
            Toast.makeText(this, "✅ Đăng xuất thành công!", Toast.LENGTH_SHORT).show();

            // 6. Finish activity hiện tại
            finish();

        } catch (Exception e) {
            Log.e(TAG, "Error during logout", e);
            Toast.makeText(this, "❌ Lỗi đăng xuất: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void clearAllSessions() {
        try {
            // Clear staff session
            SharedPreferences staffPrefs = getSharedPreferences("staff_session", MODE_PRIVATE);
            staffPrefs.edit().clear().apply();
            Log.d(TAG, "Staff session cleared");

            // Clear customer session (nếu có)
            SharedPreferences customerPrefs = getSharedPreferences("customer_session", MODE_PRIVATE);
            customerPrefs.edit().clear().apply();
            Log.d(TAG, "Customer session cleared");

            // Clear any other app preferences if needed
            SharedPreferences defaultPrefs = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
            defaultPrefs.edit().clear().apply();
            Log.d(TAG, "Default preferences cleared");

            // Clear any login-related preferences
            SharedPreferences loginPrefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
            loginPrefs.edit().clear().apply();

            Log.d(TAG, "All sessions cleared successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error clearing sessions", e);
        }
    }
} 