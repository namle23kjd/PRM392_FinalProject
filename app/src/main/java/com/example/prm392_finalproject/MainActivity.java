package com.example.prm392_finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.prm392_finalproject.controllers.UserRepository;
import com.example.prm392_finalproject.db.ConnectionClass;
import com.example.prm392_finalproject.models.User;
import com.example.prm392_finalproject.views.LoginActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.sql.Connection;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private UserRepository userRepository;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private Button btnSignOut;
    private TextView welcomeText;
    private Button btnCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UserRepository
        userRepository = new UserRepository();

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Initialize Google Sign In Client
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize views
        btnSignOut = findViewById(R.id.btnSignOut);
        welcomeText = findViewById(R.id.textView);
        btnCategory = findViewById(R.id.btnCategory);

        if (currentUser != null) {
            welcomeText.setText("Welcome, " + currentUser.getDisplayName() + "!");
        }

        // Set on-click listener for sign-out button
        btnSignOut.setOnClickListener(v -> signOut());
        btnCategory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, com.example.prm392_finalproject.views.CategoryActivity.class);
            startActivity(intent);
        });

        // Test database connection when app starts
        testDatabaseConnection();
    }

    private void testDatabaseConnection() {
        // Run database connection on background thread
        new Thread(() -> {
            try {
                Log.d(TAG, "Attempting to connect to database...");
                
                // Create instance of ConnectionClass and call CONN() method
                ConnectionClass connectionClass = new ConnectionClass();
                Connection connection = connectionClass.CONN();
                
                if (connection != null && !connection.isClosed()) {
                    Log.d(TAG, "✅ DATABASE CONNECTION SUCCESSFUL!");
                    
                    // Show success message on UI thread
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, 
                            "Database connected successfully!", 
                            Toast.LENGTH_LONG).show();
                    });
                    
                    // Close the connection
                    connection.close();
                } else {
                    Log.e(TAG, "❌ DATABASE CONNECTION FAILED: Connection is null or closed");
                    
                    // Show error message on UI thread
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, 
                            "Database connection failed!", 
                            Toast.LENGTH_LONG).show();
                    });
                }
                
            } catch (Exception e) {
                Log.e(TAG, "❌ DATABASE CONNECTION ERROR: " + e.getMessage());
                e.printStackTrace();
                
                // Show error message on UI thread
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, 
                        "Database error: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void signOut() {
        // Sign out from Firebase
        mAuth.signOut();

        // Sign out from Google
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                task -> {
                    // After signing out, navigate to LoginActivity
                    Toast.makeText(MainActivity.this, "You have been signed out.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish(); // Close MainActivity
                });
    }
}