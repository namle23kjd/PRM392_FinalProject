package com.example.prm392_finalproject.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.utils.CartManager;
import com.example.prm392_finalproject.views.adapters.CartAdapter;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartItemListener {

    private RecyclerView rvCartProducts;
    private TextView tvCartTotal;
    private MaterialButton btnCheckout;
    private CartAdapter cartAdapter;
    private CartManager cartManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        cartManager = CartManager.getInstance();
        
        initViews();
        setupToolbar();
        setupRecyclerView();
        updateUI();
    }

    private void initViews() {
        rvCartProducts = findViewById(R.id.rvCartProducts);
        tvCartTotal = findViewById(R.id.tvCartTotal);
        btnCheckout = findViewById(R.id.btnCheckout);
        
        btnCheckout.setOnClickListener(v -> proceedToCheckout());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Giỏ hàng");
            }
        }
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(cartManager.getCartItems(), this);
        rvCartProducts.setLayoutManager(new LinearLayoutManager(this));
        rvCartProducts.setAdapter(cartAdapter);
    }

    private void updateUI() {
        if (cartManager.isEmpty()) {
            tvCartTotal.setText("Giỏ hàng trống");
            btnCheckout.setEnabled(false);
        } else {
            double total = cartManager.getTotalAmount();
            tvCartTotal.setText(String.format(Locale.getDefault(), "Tổng: $%.2f", total));
            btnCheckout.setEnabled(true);
        }
        
        cartAdapter.updateData(cartManager.getCartItems());
    }

    @Override
    public void onQuantityChanged(int productId, int newQuantity) {
        cartManager.updateQuantity(productId, newQuantity);
        updateUI();
        
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
                updateUI();
                Toast.makeText(this, "Đã xóa sản phẩm khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void proceedToCheckout() {
        if (cartManager.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            return;
        }

        // Chuyển sang màn hình thanh toán
        Intent intent = new Intent(this, CreateOrderActivity.class);
        startActivity(intent);
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
    protected void onResume() {
        super.onResume();
        // Cập nhật UI khi quay lại từ màn hình thanh toán
        updateUI();
    }
} 