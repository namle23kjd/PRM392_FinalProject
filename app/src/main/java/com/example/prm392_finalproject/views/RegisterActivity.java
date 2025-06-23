package com.example.prm392_finalproject.views;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.controllers.UserRepository;
import com.example.prm392_finalproject.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    
    private TextInputEditText etFirstName, etLastName, etEmail, etPhone, etAddress, etDateOfBirth, etPassword, etConfirmPassword;
    private RadioGroup rgGender;
    private MaterialButton btnRegister;
    private ProgressBar progressBar;
    private UserRepository userRepository;
    private Calendar selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize UserRepository
        userRepository = new UserRepository();

        // Initialize views
        initViews();
        
        // Set click listeners
        setClickListeners();
    }

    private void initViews() {
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etDateOfBirth = findViewById(R.id.etDateOfBirth);
        rgGender = findViewById(R.id.rgGender);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        
        selectedDate = Calendar.getInstance();
    }

    private void setClickListeners() {
        // Register button click
        btnRegister.setOnClickListener(v -> attemptRegister());

        // Date picker click
        etDateOfBirth.setOnClickListener(v -> showDatePicker());

        // Back to login click
        findViewById(R.id.tvBackToLogin).setOnClickListener(v -> {
            finish(); // Go back to previous activity
        });
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                selectedDate.set(year, month, dayOfMonth);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                etDateOfBirth.setText(sdf.format(selectedDate.getTime()));
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        
        // Set max date to today (user can't be born in the future)
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void attemptRegister() {
        // Get input values
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String dateOfBirth = etDateOfBirth.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Get selected gender
        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        String gender = "";
        if (selectedGenderId == R.id.rbMale) {
            gender = "Male";
        } else if (selectedGenderId == R.id.rbFemale) {
            gender = "Female";
        } else if (selectedGenderId == R.id.rbOther) {
            gender = "Other";
        }

        // Validate input
        if (!validateInput(firstName, lastName, email, phone, address, gender, dateOfBirth, password, confirmPassword)) {
            return;
        }

        // Show progress
        showProgress(true);

        // Create user object
        User user = new User(gender, firstName, lastName, email, phone, address, selectedDate.getTime(), password);

        // Perform registration on background thread
        new Thread(() -> {
            try {
                boolean success = userRepository.registerUser(user);
                
                if (success) {
                    Log.d(TAG, "Registration successful for user: " + user.getFullName());
                    
                    runOnUiThread(() -> {
                        showProgress(false);
                        Toast.makeText(RegisterActivity.this, 
                            "Registration successful! Welcome, " + user.getFirstName() + "!", 
                            Toast.LENGTH_LONG).show();
                        
                        // Navigate back to login
                        finish();
                    });
                    
                } else {
                    Log.e(TAG, "Registration failed");
                    
                    runOnUiThread(() -> {
                        showProgress(false);
                        Toast.makeText(RegisterActivity.this, 
                            "Registration failed. Email might already exist.", 
                            Toast.LENGTH_LONG).show();
                    });
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Registration error: " + e.getMessage());
                e.printStackTrace();
                
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(RegisterActivity.this, 
                        "Registration failed: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private boolean validateInput(String firstName, String lastName, String email, 
                                 String phone, String address, String gender, String dateOfBirth,
                                 String password, String confirmPassword) {
        
        // Check first name
        if (firstName.isEmpty()) {
            etFirstName.setError("First name is required");
            etFirstName.requestFocus();
            return false;
        }

        // Check last name
        if (lastName.isEmpty()) {
            etLastName.setError("Last name is required");
            etLastName.requestFocus();
            return false;
        }

        // Check email
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

        // Check phone
        if (phone.isEmpty()) {
            etPhone.setError("Phone number is required");
            etPhone.requestFocus();
            return false;
        }

        // Check address
        if (address.isEmpty()) {
            etAddress.setError("Address is required");
            etAddress.requestFocus();
            return false;
        }

        // Check gender
        if (gender.isEmpty()) {
            Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check date of birth
        if (dateOfBirth.isEmpty()) {
            etDateOfBirth.setError("Date of birth is required");
            etDateOfBirth.requestFocus();
            return false;
        }

        // Check password
        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }

        // Check confirm password
        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError("Confirm password is required");
            etConfirmPassword.requestFocus();
            return false;
        }

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return false;
        }

        // Check password length
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!show);
    }
} 