package com.example.prm392_finalproject.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.controllers.OrderController;
import com.example.prm392_finalproject.models.Order;
import com.example.prm392_finalproject.models.OrderRequest;
import com.example.prm392_finalproject.utils.CartManager;
import com.example.prm392_finalproject.views.adapters.CartAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateOrderActivity extends AppCompatActivity implements CartAdapter.OnCartItemListener {

    private EditText editTextShippingAddress, editTextShippingPersonName, editTextNote;
    private AutoCompleteTextView autoCompleteShippingMethod;
    private RecyclerView recyclerViewProducts;
    private TextView textViewTotalAmount;
    private Button buttonCreateOrder;
    
    private CartAdapter cartAdapter;
    private CartManager cartManager;
    private OrderController orderController;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_order);

        // Initialize controllers
        orderController = new OrderController();
        cartManager = CartManager.getInstance();
        executorService = Executors.newSingleThreadExecutor();

        // Initialize views
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();
    }

    private void initViews() {
        editTextShippingAddress = findViewById(R.id.editTextShippingAddress);
        editTextShippingPersonName = findViewById(R.id.editTextShippingPersonName);
        editTextNote = findViewById(R.id.editTextNote);
        autoCompleteShippingMethod = findViewById(R.id.autoCompleteShippingMethod);
        recyclerViewProducts = findViewById(R.id.recyclerViewProducts);
        textViewTotalAmount = findViewById(R.id.textViewTotalAmount);
        buttonCreateOrder = findViewById(R.id.buttonCreateOrder);

        // Setup shipping method dropdown
        String[] shippingMethods = {"Standard", "Express", "Same Day"};
        ArrayAdapter<String> shippingAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_dropdown_item_1line, shippingMethods);
        autoCompleteShippingMethod.setAdapter(shippingAdapter);
        autoCompleteShippingMethod.setText(shippingMethods[0], false);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Tạo đơn hàng mới");
        }
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(cartManager.getCartItems(), this);
        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewProducts.setAdapter(cartAdapter);
        updateTotalAmount();
    }

    private void setupListeners() {
        buttonCreateOrder.setOnClickListener(v -> validateAndCreateOrder());
    }

    @Override
    public void onQuantityChanged(int productId, int newQuantity) {
        cartManager.updateQuantity(productId, newQuantity);
        updateTotalAmount();
        cartAdapter.updateData(cartManager.getCartItems());
        
        if (newQuantity <= 0) {
            Toast.makeText(this, "Đã xóa sản phẩm khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Đã cập nhật số lượng", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRemoveItem(int productId) {
        new AlertDialog.Builder(this)
            .setTitle("Xác nhận")
            .setMessage("Bạn có chắc muốn xóa sản phẩm này khỏi giỏ hàng?")
            .setPositiveButton("Xóa", (dialog, which) -> {
                cartManager.removeFromCart(productId);
                updateTotalAmount();
                cartAdapter.updateData(cartManager.getCartItems());
                Toast.makeText(this, "Đã xóa sản phẩm khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void updateTotalAmount() {
        double total = cartManager.getTotalAmount();
        textViewTotalAmount.setText(String.format(Locale.getDefault(), "Tổng tiền: $%.2f", total));
    }

    private void validateAndCreateOrder() {
        if (cartManager.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất một sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        String shippingAddress = editTextShippingAddress.getText().toString().trim();
        String shippingPersonName = editTextShippingPersonName.getText().toString().trim();
        String shippingMethod = autoCompleteShippingMethod.getText().toString();
        String note = editTextNote.getText().toString().trim();

        if (shippingAddress.isEmpty()) {
            editTextShippingAddress.setError("Vui lòng nhập địa chỉ giao hàng");
            return;
        }

        if (shippingPersonName.isEmpty()) {
            editTextShippingPersonName.setError("Vui lòng nhập tên người nhận");
            return;
        }

        // Calculate total amount
        double totalAmount = cartManager.getTotalAmount();

        // Create order request
        List<OrderRequest.OrderItemRequest> items = new ArrayList<>();
        for (CartManager.CartItem cartItem : cartManager.getCartItems()) {
            items.add(new OrderRequest.OrderItemRequest(
                cartItem.getProduct().getProductId(),
                cartItem.getQuantity(),
                cartItem.getProduct().getPrice()
            ));
        }

        OrderRequest orderRequest = new OrderRequest(
            1, // TODO: Get actual customer ID from session
            totalAmount,
            note,
            shippingAddress,
            shippingMethod,
            shippingPersonName,
            items
        );

        createOrder(orderRequest);
    }

    private void createOrder(OrderRequest orderRequest) {
        executorService.execute(() -> {
            try {
                // Sử dụng method mới để tạo order từ cart với đầy đủ thông tin
                boolean success = orderController.createOrderFromCart(orderRequest);
                
                runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(this, "Đơn hàng đã được tạo thành công!", Toast.LENGTH_LONG).show();
                        
                        // Clear cart after successful order
                        cartManager.clearCart();
                        
                        // Navigate to order management instead of payment
                        Intent intent = new Intent(this, OrderManagementActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Có lỗi xảy ra khi tạo đơn hàng", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
} 