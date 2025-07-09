package com.example.prm392_finalproject.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.controllers.OrderController;
import com.example.prm392_finalproject.models.zalopay.Order;
import com.example.prm392_finalproject.views.adapters.OrderItemAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView textViewOrderId, textViewOrderDate, textViewStatus, textViewTotalAmount;
    private TextView textViewShippingAddress, textViewShippingMethod, textViewNote;
    private RecyclerView recyclerViewOrderItems;
    
    private OrderController orderController;
    private ExecutorService executorService;
    private Order currentOrder;
    private int orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        // Get order ID from intent
        orderId = getIntent().getIntExtra("order_id", -1);
        if (orderId == -1) {
            Toast.makeText(this, "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize controllers
        orderController = new OrderController();
        executorService = Executors.newSingleThreadExecutor();

        // Initialize views
        initViews();
        setupToolbar();
        loadOrderDetails();
    }

    private void initViews() {
        textViewOrderId = findViewById(R.id.textViewOrderId);
        textViewOrderDate = findViewById(R.id.textViewOrderDate);
        textViewStatus = findViewById(R.id.textViewStatus);
        textViewTotalAmount = findViewById(R.id.textViewTotalAmount);
        textViewShippingAddress = findViewById(R.id.textViewShippingAddress);
        textViewShippingMethod = findViewById(R.id.textViewShippingMethod);
        textViewNote = findViewById(R.id.textViewNote);
        recyclerViewOrderItems = findViewById(R.id.recyclerViewOrderItems);

        recyclerViewOrderItems.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết đơn hàng");
        }
    }

    private void loadOrderDetails() {
        executorService.execute(() -> {
            // TODO: Implement get order by ID in OrderController
            // For now, we'll use a mock order
            Order mockOrder = createMockOrder();
            
            runOnUiThread(() -> {
                currentOrder = mockOrder;
                displayOrderDetails(mockOrder);
            });
        });
    }

    private Order createMockOrder() {
        Order order = new Order();
        order.setOrderId(String.valueOf(orderId));
        order.setCustomerId(1);
        order.setOrderDate(new Date().toString());
        order.setStatus("pending");
        order.setTotalAmount(25000000);
        order.setNote("Đơn hàng iPhone 15 Pro");
        return order;
    }

    private void displayOrderDetails(Order order) {
        // Display basic order information
        textViewOrderId.setText("Mã đơn hàng: #" + order.getOrderId());
        textViewOrderDate.setText("Ngày đặt: " + formatDate(order.getOrderDate()));
        textViewStatus.setText("Trạng thái: " + order.getStatusDisplay());
        textViewTotalAmount.setText("Tổng tiền: " + String.format("%,d VND", (long) order.getTotalAmount()));
        textViewNote.setText("Ghi chú: " + (order.getNote() != null ? order.getNote() : "Không có"));

        // Display shipping information (mock data for now)
        textViewShippingAddress.setText("Địa chỉ: 123 Đường ABC, Quận 1, TP.HCM");
        textViewShippingMethod.setText("Phương thức: Express");

        // Display order items (mock data for now)
        displayOrderItems();
    }

    private void displayOrderItems() {
        // Mock order items
        List<OrderItemAdapter.OrderItem> orderItems = new ArrayList<>();
        orderItems.add(new OrderItemAdapter.OrderItem(1, 1, "iPhone 15 Pro", 1, 25000000, 25000000));
        OrderItemAdapter adapter = new OrderItemAdapter(orderItems);
        recyclerViewOrderItems.setAdapter(adapter);
    }

    private String formatDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("vi", "VN"));
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateString;
        }
    }

    private void showStatusUpdateDialog() {
        String[] statuses = {"pending", "confirmed", "processing", "shipping", "completed", "cancelled"};
        String[] statusDisplay = {"Chờ xác nhận", "Đã xác nhận", "Đang xử lý", "Đang giao hàng", "Hoàn thành", "Đã hủy"};

        new AlertDialog.Builder(this)
            .setTitle("Cập nhật trạng thái đơn hàng")
            .setItems(statusDisplay, (dialog, which) -> {
                String newStatus = statuses[which];
                updateOrderStatus(newStatus);
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void updateOrderStatus(String newStatus) {
        executorService.execute(() -> {
            orderController.updateOrderStatus(currentOrder.getOrderId(), newStatus);
            runOnUiThread(() -> {
                currentOrder.setStatus(newStatus);
                textViewStatus.setText("Trạng thái: " + currentOrder.getStatusDisplay());
                Toast.makeText(this, "Cập nhật trạng thái thành công", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa đơn hàng này?")
            .setPositiveButton("Xóa", (dialog, which) -> deleteOrder())
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void deleteOrder() {
        executorService.execute(() -> {
            boolean success = orderController.deleteOrder(orderId);
            runOnUiThread(() -> {
                if (success) {
                    Toast.makeText(this, "Xóa đơn hàng thành công", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, OrderManagementActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Xóa đơn hàng thất bại", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_order_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_edit_status) {
            showStatusUpdateDialog();
            return true;
        } else if (item.getItemId() == R.id.action_delete) {
            showDeleteConfirmationDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
} 