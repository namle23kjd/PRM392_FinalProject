package com.example.prm392_finalproject.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_finalproject.MainActivity;
import com.example.prm392_finalproject.R;

public class PaymentSuccessActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_success);

        TextView tvMessage = findViewById(R.id.tvSuccessMessage);
        String message = getIntent().getStringExtra("message");
        if (message != null) {
            tvMessage.setText(message);
        }

        Button btnBack = findViewById(R.id.btnBackToHomeSuccess);
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(PaymentSuccessActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }
} 