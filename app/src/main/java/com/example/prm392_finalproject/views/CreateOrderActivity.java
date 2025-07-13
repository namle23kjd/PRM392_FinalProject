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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.controllers.OrderController;
import com.example.prm392_finalproject.dao.DiscountDAO;
import com.example.prm392_finalproject.models.Discount;
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
    private AutoCompleteTextView autoCompleteShippingMethod, spinnerDiscountCode;
    private RecyclerView recyclerViewProducts;
    private TextView textViewTotalAmount, textViewSubtotal, textViewDiscountAmount, textViewDiscountInfo, textViewDiscountType;
    private Button buttonCreateOrder, buttonRemoveDiscount;
    private LinearLayout layoutDiscountInfo;
    private ProgressBar progressBarDiscount;
    
    private CartAdapter cartAdapter;
    private CartManager cartManager;
    private OrderController orderController;
    private DiscountDAO discountDAO;
    private ExecutorService executorService;
    
    // Discount variables
    private Discount appliedDiscount = null;
    private double discountAmount = 0.0;
    private List<Discount> availableDiscounts = new ArrayList<>();
    private ArrayAdapter<String> discountAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_order);

        // Initialize controllers
        orderController = new OrderController();
        cartManager = CartManager.getInstance();
        discountDAO = new DiscountDAO();
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
        spinnerDiscountCode = findViewById(R.id.spinnerDiscountCode);
        autoCompleteShippingMethod = findViewById(R.id.autoCompleteShippingMethod);
        recyclerViewProducts = findViewById(R.id.recyclerViewProducts);
        textViewTotalAmount = findViewById(R.id.textViewTotalAmount);
        textViewSubtotal = findViewById(R.id.textViewSubtotal);
        textViewDiscountAmount = findViewById(R.id.textViewDiscountAmount);
        textViewDiscountInfo = findViewById(R.id.textViewDiscountInfo);
        textViewDiscountType = findViewById(R.id.textViewDiscountType);
        buttonCreateOrder = findViewById(R.id.buttonCreateOrder);
        buttonRemoveDiscount = findViewById(R.id.buttonRemoveDiscount);
        layoutDiscountInfo = findViewById(R.id.layoutDiscountInfo);
        progressBarDiscount = findViewById(R.id.progressBarDiscount);

        // Setup shipping method dropdown
        String[] shippingMethods = {"Standard", "Express", "Same Day"};
        ArrayAdapter<String> shippingAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_dropdown_item_1line, shippingMethods);
        autoCompleteShippingMethod.setAdapter(shippingAdapter);
        autoCompleteShippingMethod.setText(shippingMethods[0], false);
        
        // Setup discount spinner
        setupDiscountSpinner();
        
        // Load available discounts
        loadAvailableDiscounts();
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
        buttonRemoveDiscount.setOnClickListener(v -> removeDiscount());
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

    private void setupDiscountSpinner() {
        discountAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        spinnerDiscountCode.setAdapter(discountAdapter);
        
        // Add loading option initially
        discountAdapter.add("Đang tải mã giảm giá...");
        
        spinnerDiscountCode.setOnItemClickListener((parent, view, position, id) -> {
            if (position > 0 && position <= availableDiscounts.size()) {
                Discount selectedDiscount = availableDiscounts.get(position - 1);
                applyDiscount(selectedDiscount);
            }
        });
    }

    private void loadAvailableDiscounts() {
        // Show loading state
        progressBarDiscount.setVisibility(View.VISIBLE);
        spinnerDiscountCode.setEnabled(false);
        
        executorService.execute(() -> {
            try {
                List<Discount> allDiscounts = discountDAO.getAllDiscounts();
                List<Discount> validDiscounts = new ArrayList<>();
                
                // Filter only valid discounts
                for (Discount discount : allDiscounts) {
                    if (discountDAO.isDiscountValid(discount.getCode())) {
                        validDiscounts.add(discount);
                    }
                }
                
                runOnUiThread(() -> {
                    availableDiscounts.clear();
                    availableDiscounts.addAll(validDiscounts);
                    updateDiscountSpinner();
                    progressBarDiscount.setVisibility(View.GONE);
                    spinnerDiscountCode.setEnabled(true);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi khi tải mã giảm giá: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBarDiscount.setVisibility(View.GONE);
                    spinnerDiscountCode.setEnabled(true);
                    // Show error state in spinner
                    discountAdapter.clear();
                    discountAdapter.add("Lỗi tải mã giảm giá");
                    discountAdapter.notifyDataSetChanged();
                });
            }
        });
    }

    private void updateDiscountSpinner() {
        discountAdapter.clear();
        
        if (availableDiscounts.isEmpty()) {
            discountAdapter.add("Không có mã giảm giá khả dụng");
        } else {
            discountAdapter.add("-- Chọn mã giảm giá --");
            
            for (Discount discount : availableDiscounts) {
                String displayText = String.format("%s - %s", 
                    discount.getCode(), 
                    discount.getDiscount_type().equals("Percentage") ? 
                        discount.getDiscount_value() + "%" : 
                        String.format(Locale.getDefault(), "%.0f VND", discount.getDiscount_value()));
                discountAdapter.add(displayText);
            }
        }
        
        discountAdapter.notifyDataSetChanged();
    }

    private void updateTotalAmount() {
        double subtotal = cartManager.getTotalAmount();
        double total = subtotal - discountAmount;
        
        // Update subtotal
        textViewSubtotal.setText(String.format(Locale.getDefault(), "Tạm tính: %.0f VND", subtotal));
        
        // Update discount amount if applied
        if (appliedDiscount != null) {
            textViewDiscountAmount.setVisibility(View.VISIBLE);
            textViewDiscountAmount.setText(String.format(Locale.getDefault(), "Giảm giá: %.0f VND", discountAmount));
        } else {
            textViewDiscountAmount.setVisibility(View.GONE);
        }
        
        // Update total amount
        textViewTotalAmount.setText(String.format(Locale.getDefault(), "Tổng tiền: %.0f VND", total));
    }

    private void applyDiscount(Discount discount) {
        if (discount == null) {
            Toast.makeText(this, "Vui lòng chọn mã giảm giá", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if already applied a discount
        if (appliedDiscount != null) {
            Toast.makeText(this, "Chỉ được áp dụng 1 mã giảm giá cho 1 đơn hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Apply discount
        appliedDiscount = discount;
        calculateDiscountAmount();
        showDiscountInfo();
        updateTotalAmount();
        
        // Reset spinner to default
        spinnerDiscountCode.setText("-- Chọn mã giảm giá --", false);
        
        Toast.makeText(this, "Đã áp dụng mã giảm giá thành công!", Toast.LENGTH_SHORT).show();
    }

    private void removeDiscount() {
        appliedDiscount = null;
        discountAmount = 0.0;
        
        // Reset spinner to appropriate default
        if (availableDiscounts.isEmpty()) {
            spinnerDiscountCode.setText("Không có mã giảm giá khả dụng", false);
        } else {
            spinnerDiscountCode.setText("-- Chọn mã giảm giá --", false);
        }
        
        hideDiscountInfo();
        updateTotalAmount();
        Toast.makeText(this, "Đã xóa mã giảm giá", Toast.LENGTH_SHORT).show();
    }

    private void calculateDiscountAmount() {
        if (appliedDiscount == null) {
            discountAmount = 0.0;
            return;
        }
        
        double subtotal = cartManager.getTotalAmount();
        double maxDiscount = subtotal * 0.5; // Maximum 50% of subtotal
        
        if ("Percentage".equals(appliedDiscount.getDiscount_type())) {
            discountAmount = subtotal * (appliedDiscount.getDiscount_value() / 100.0);
        } else if ("Amount".equals(appliedDiscount.getDiscount_type())) {
            discountAmount = appliedDiscount.getDiscount_value();
        }
        
        // Apply 50% maximum discount rule
        if (discountAmount > maxDiscount) {
            discountAmount = maxDiscount;
        }
        
        // Ensure discount doesn't exceed subtotal
        if (discountAmount > subtotal) {
            discountAmount = subtotal;
        }
    }

    private void showDiscountInfo() {
        if (appliedDiscount != null) {
            layoutDiscountInfo.setVisibility(View.VISIBLE);
            textViewDiscountInfo.setText(String.format(Locale.getDefault(), "Giảm giá: %.0f VND", discountAmount));
            textViewDiscountType.setText(String.format("Loại: %s", appliedDiscount.getDiscount_type()));
        }
    }

    private void hideDiscountInfo() {
        layoutDiscountInfo.setVisibility(View.GONE);
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

        // Calculate total amount after discount
        double subtotal = cartManager.getTotalAmount();
        double totalAmount = subtotal - discountAmount;

        // Lấy customer_id từ SharedPreferences
        android.content.SharedPreferences prefs = getSharedPreferences("customer_session", MODE_PRIVATE);
        int customerId = prefs.getInt("customer_id", -1);
        if (customerId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng!", Toast.LENGTH_SHORT).show();
            return;
        }

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
            customerId,
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