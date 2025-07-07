package com.example.prm392_finalproject.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.controllers.UserRepository;
import com.example.prm392_finalproject.models.User;
import com.google.android.material.textfield.TextInputEditText;

public class UserProfileActivity extends AppCompatActivity {

    private static final String TAG = "UserProfileActivity";
    
    private UserRepository userRepository;
    private User currentUser;
    
    // UI Components
    private Toolbar toolbar;
    private TextInputEditText etFirstName, etLastName, etEmail, etPhone, etAddress;
    private AutoCompleteTextView spinnerGender;
    private Button btnUpdateProfile;
    private ProgressBar progressBar;
    
    private String[] genderOptions = {"Male", "Female", "Other"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Initialize dependencies
        userRepository = new UserRepository();
        
        // Get current user from intent
        currentUser = (User) getIntent().getSerializableExtra("currentUser");
        if (currentUser == null) {
            Toast.makeText(this, "User information not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        initViews();
        
        // Setup toolbar
        setupToolbar();
        
        // Setup gender spinner
        setupGenderSpinner();
        
        // Load user data
        loadUserData();
        
        // Set click listeners
        setClickListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        spinnerGender = findViewById(R.id.spinnerGender);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("User Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupGenderSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
            android.R.layout.simple_dropdown_item_1line, genderOptions);
        spinnerGender.setAdapter(adapter);
        spinnerGender.setOnClickListener(v -> spinnerGender.showDropDown());
        spinnerGender.setFocusable(false);
        spinnerGender.setLongClickable(false);
    }

    private void loadUserData() {
        if (currentUser != null) {
            etFirstName.setText(currentUser.getFirstName());
            etLastName.setText(currentUser.getLastName());
            etEmail.setText(currentUser.getEmail());
            etPhone.setText(currentUser.getPhone());
            etAddress.setText(currentUser.getAddress());
            
            // Set gender spinner
            String gender = currentUser.getGender();
            if (gender != null) {
                spinnerGender.setText(gender, false);
            }
            
            // Disable email and name fields (read-only)
            etFirstName.setEnabled(false);
            etLastName.setEnabled(false);
            etEmail.setEnabled(false);
        }
    }

    private void setClickListeners() {
        btnUpdateProfile.setOnClickListener(v -> updateProfile());
    }

    private void updateProfile() {
        // Validate input
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String gender = spinnerGender.getText().toString();
        
        if (phone.isEmpty()) {
            etPhone.setError("Phone number is required");
            etPhone.requestFocus();
            return;
        }
        
        if (address.isEmpty()) {
            etAddress.setError("Address is required");
            etAddress.requestFocus();
            return;
        }
        
        // Show progress
        showProgress(true);
        
        // Update user object
        currentUser.setPhone(phone);
        currentUser.setAddress(address);
        currentUser.setGender(gender);
        
        // Update in database
        new Thread(() -> {
            try {
                boolean success = userRepository.updateUser(currentUser);
                runOnUiThread(() -> {
                    showProgress(false);
                    if (success) {
                        Toast.makeText(UserProfileActivity.this, 
                            "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("updatedUser", currentUser);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    } else {
                        Toast.makeText(UserProfileActivity.this, 
                            "Failed to update profile", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error updating profile: " + e.getMessage());
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(UserProfileActivity.this, 
                        "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnUpdateProfile.setEnabled(!show);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 