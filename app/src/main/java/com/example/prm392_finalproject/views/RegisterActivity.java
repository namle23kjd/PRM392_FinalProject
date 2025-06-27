package com.example.prm392_finalproject.views;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.controllers.UserRepository;
import com.example.prm392_finalproject.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    
    private TextInputEditText etFirstName, etLastName, etEmail, etPhone, etAddress, etDateOfBirth, etPassword, etConfirmPassword;
    private RadioGroup rgGender;
    private MaterialButton btnRegister;
    private ProgressBar progressBar;
    private UserRepository userRepository;
    private Calendar selectedDate;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userRepository = new UserRepository();
        mAuth = FirebaseAuth.getInstance();
        initViews();
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
        btnRegister.setOnClickListener(v -> attemptRegister());
        etDateOfBirth.setOnClickListener(v -> showDatePicker());
        findViewById(R.id.tvBackToLogin).setOnClickListener(v -> finish());
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
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void attemptRegister() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String dateOfBirth = etDateOfBirth.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        String gender;
        if (selectedGenderId == R.id.rbMale) gender = "Male";
        else if (selectedGenderId == R.id.rbFemale) gender = "Female";
        else if (selectedGenderId == R.id.rbOther) gender = "Other";
        else gender = null;

        if (gender == null) {
            Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!validateInput(firstName, lastName, email, phone, address, gender, dateOfBirth, password, confirmPassword)) {
            return;
        }

        showProgress(true);

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    User user = new User(gender, firstName, lastName, email, phone, address, selectedDate.getTime(), password);
                    new Thread(() -> {
                        boolean success = userRepository.registerUser(user);
                        runOnUiThread(() -> {
                            showProgress(false);
                            if (success) {
                                Toast.makeText(this, "Registration successful!", Toast.LENGTH_LONG).show();
                                finish();
                            } else {
                                Toast.makeText(this, "Registration failed. Email might already exist.", Toast.LENGTH_LONG).show();
                            }
                        });
                    }).start();
                } else {
                    showProgress(false);
                    Toast.makeText(this, "Firebase registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
    }

    private boolean validateInput(String firstName, String lastName, String email, 
                                 String phone, String address, String gender, String dateOfBirth,
                                 String password, String confirmPassword) {
        if (firstName.isEmpty()) { etFirstName.setError("First name is required"); etFirstName.requestFocus(); return false; }
        if (lastName.isEmpty()) { etLastName.setError("Last name is required"); etLastName.requestFocus(); return false; }
        if (email.isEmpty()) { etEmail.setError("Email is required"); etEmail.requestFocus(); return false; }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { etEmail.setError("Please enter a valid email address"); etEmail.requestFocus(); return false; }
        if (phone.isEmpty()) { etPhone.setError("Phone number is required"); etPhone.requestFocus(); return false; }
        if (address.isEmpty()) { etAddress.setError("Address is required"); etAddress.requestFocus(); return false; }
        if (gender.isEmpty()) { Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show(); return false; }
        if (dateOfBirth.isEmpty()) { etDateOfBirth.setError("Date of birth is required"); etDateOfBirth.requestFocus(); return false; }
        if (password.isEmpty()) { etPassword.setError("Password is required"); etPassword.requestFocus(); return false; }
        if (password.length() < 6) { etPassword.setError("Password must be at least 6 characters"); etPassword.requestFocus(); return false; }
        if (confirmPassword.isEmpty()) { etConfirmPassword.setError("Confirm password is required"); etConfirmPassword.requestFocus(); return false; }
        if (!password.equals(confirmPassword)) { etConfirmPassword.setError("Passwords do not match"); etConfirmPassword.requestFocus(); return false; }
        return true;
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!show);
    }
} 