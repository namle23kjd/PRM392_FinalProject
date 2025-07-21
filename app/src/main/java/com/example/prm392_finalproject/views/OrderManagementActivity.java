package com.example.prm392_finalproject.views;

import android.content.Intent;
import android.os.Bundle;
import android.app.AlertDialog;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.dao.OrderDAO;
import com.example.prm392_finalproject.dao.PaymentDAO;
import com.example.prm392_finalproject.models.zalopay.Order;
import com.example.prm392_finalproject.views.adapters.OrderAdapter;
import com.example.prm392_finalproject.views.PaymentHistoryActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrderManagementActivity extends AppCompatActivity implements OrderAdapter.OnPayClickListener {
    private RecyclerView rvOrders;
    private OrderAdapter orderAdapter;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_management);

        rvOrders = findViewById(R.id.rvOrders);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));

        // Nút Lịch sử thanh toán
        MaterialButton btnPaymentHistory = findViewById(R.id.btnPaymentHistory);
        btnPaymentHistory.setOnClickListener(v -> {
            Intent intent = new Intent(this, PaymentHistoryActivity.class);
            startActivity(intent);
        });

        // Nút tạo đơn hàng mới (FloatingActionButton)
        FloatingActionButton btnCreateOrder = findViewById(R.id.btnCreateOrder);
        btnCreateOrder.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateOrderActivity.class);
            startActivity(intent);
        });

        // Load danh sách đơn hàng của user (ví dụ userId = 1)
        int userId = 1;
        executorService.execute(() -> {
            OrderDAO orderDAO = new OrderDAO();
            List<Order> orders = orderDAO.getOrdersByUserId(userId);
            runOnUiThread(() -> {
                orderAdapter = new OrderAdapter(orders, this);
                rvOrders.setAdapter(orderAdapter);
            });
        });
    }

    @Override
    public void onPayClick(Order order) {
        showPaymentConfirmationDialog(order);
    }

    private void showPaymentConfirmationDialog(Order order) {
        executorService.execute(() -> {
            PaymentDAO paymentDAO = new PaymentDAO();
            boolean hasCompleted = paymentDAO.hasCompletedPayment(Integer.parseInt(order.getOrderId()));
            runOnUiThread(() -> {
                if (hasCompleted) {
                    new AlertDialog.Builder(this)
                            .setTitle("Không thể thanh toán")
                            .setMessage("Đơn hàng này đã được thanh toán thành công trước đó!")
                            .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle("Xác nhận thanh toán")
                            .setMessage("Bạn có muốn thanh toán đơn hàng #" + order.getOrderId() +
                                    "\nTổng tiền: " + String.format("%,.0f", order.getTotalAmount()) + " VND" +
                                    "\n\nBằng ZaloPay?")
                            .setPositiveButton("Thanh toán", (dialog, which) -> {
                                Intent intent = new Intent(this, PaymentActivity.class);
                                intent.putExtra("order", order);
                                startActivity(intent);
                            })
                            .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .show();
                }
            });
        });
    }
}
