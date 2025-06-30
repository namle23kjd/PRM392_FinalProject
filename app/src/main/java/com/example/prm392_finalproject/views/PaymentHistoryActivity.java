package com.example.prm392_finalproject.views;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.dao.PaymentDAO;
import com.example.prm392_finalproject.models.Payment;
import com.example.prm392_finalproject.views.adapters.PaymentHistoryAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PaymentHistoryActivity extends AppCompatActivity {
    private RecyclerView rvPaymentHistory;
    private PaymentHistoryAdapter paymentHistoryAdapter;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private List<Payment> allPayments = new ArrayList<>();
    private EditText etSearchPayment;
    private Spinner spinnerStatus, spinnerMethod;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_history);

        rvPaymentHistory = findViewById(R.id.rvPaymentHistory);
        rvPaymentHistory.setLayoutManager(new LinearLayoutManager(this));
        etSearchPayment = findViewById(R.id.etSearchPayment);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        spinnerMethod = findViewById(R.id.spinnerMethod);

        int userId = 1; // Lấy userId thực tế từ session nếu có
        executorService.execute(() -> {
            PaymentDAO paymentDAO = new PaymentDAO();
            List<Payment> payments = paymentDAO.getPaymentsByUserId(userId);
            runOnUiThread(() -> {
                allPayments = payments;
                paymentHistoryAdapter = new PaymentHistoryAdapter(payments);
                rvPaymentHistory.setAdapter(paymentHistoryAdapter);
                setupFilter();
            });
        });
    }

    private void setupFilter() {
        etSearchPayment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPayments();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                filterPayments();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };
        spinnerStatus.setOnItemSelectedListener(listener);
        spinnerMethod.setOnItemSelectedListener(listener);
    }

    private void filterPayments() {
        String search = etSearchPayment.getText().toString().trim().toLowerCase();
        String status = spinnerStatus.getSelectedItem().toString();
        String method = spinnerMethod.getSelectedItem().toString();
        List<Payment> filtered = new ArrayList<>();
        for (Payment p : allPayments) {
            boolean matchSearch = search.isEmpty() ||
                ("#" + p.getOrderId()).toLowerCase().contains(search) ||
                String.valueOf((int)p.getAmount()).contains(search);
            boolean matchStatus = status.equals("Tất cả") || p.getPaymentStatus().equalsIgnoreCase(status);
            boolean matchMethod = method.equals("Tất cả") || p.getPaymentMethod().equalsIgnoreCase(method);
            if (matchSearch && matchStatus && matchMethod) {
                filtered.add(p);
            }
        }
        paymentHistoryAdapter.updateData(filtered);
    }
} 