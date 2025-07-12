package com.example.prm392_finalproject.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.models.Product;
import com.example.prm392_finalproject.utils.CartManager;
import com.google.android.material.snackbar.Snackbar;

import java.text.NumberFormat;
import java.util.Locale;

public class CustomerProductDetailActivity extends AppCompatActivity {

    private static final String TAG = "CustomerProductDetail";

    // UI Components
    private ImageView ivProductImage;
    private TextView tvProductName, tvProductPrice, tvProductCategory;
    private TextView tvStockStatus, tvStockQuantity;
    private TextView tvProductDescription, tvProductColor, tvProductOrigin, tvProductWarranty;
    private Button btnAddToCart, btnBuyNow;

    private Product product;
    private NumberFormat currencyFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_product_detail);

        try {
            // Initialize views
            initViews();

            // Setup toolbar
            setupToolbar();

            // Get product from intent
            getProductFromIntent();

            // Display product data
            if (product != null) {
                displayProductData();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error loading product details", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        // Initialize UI components
        ivProductImage = findViewById(R.id.ivProductImage);
        tvProductName = findViewById(R.id.tvProductName);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        tvProductCategory = findViewById(R.id.tvProductCategory);
        tvStockStatus = findViewById(R.id.tvStockStatus);
        tvStockQuantity = findViewById(R.id.tvStockQuantity);
        tvProductDescription = findViewById(R.id.tvProductDescription);
        tvProductColor = findViewById(R.id.tvProductColor);
        tvProductOrigin = findViewById(R.id.tvProductOrigin);
        tvProductWarranty = findViewById(R.id.tvProductWarranty);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnBuyNow = findViewById(R.id.btnBuyNow);

        // Setup button listeners
        btnAddToCart.setOnClickListener(v -> addToCart());
        btnBuyNow.setOnClickListener(v -> buyNow());

        // Initialize currency format
        currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

        Log.d(TAG, "Views initialized successfully");
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Product Details");
        }
    }

    private void getProductFromIntent() {
        try {
            product = (Product) getIntent().getSerializableExtra("product");
            if (product == null) {
                Log.e(TAG, "Product is null from intent");
                Toast.makeText(this, "Error: Product not found", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Log.d(TAG, "Product received: " + product.getName());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting product from intent: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error loading product", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void displayProductData() {
        try {
            // Basic information
            tvProductName.setText(product.getName());
            tvProductPrice.setText(currencyFormat.format(product.getPrice()));

            // Category
            if (product.getCategory() != null && !product.getCategory().isEmpty()) {
                tvProductCategory.setText(product.getCategory());
            } else {
                tvProductCategory.setText("General");
            }

            // Stock status
            int stock = product.getQuantityInStock();
            if (stock > 0) {
                tvStockStatus.setText("In Stock");
                tvStockStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                tvStockQuantity.setText("(" + stock + " available)");

                // Enable buttons
                btnAddToCart.setEnabled(true);
                btnBuyNow.setEnabled(true);
            } else {
                tvStockStatus.setText("Out of Stock");
                tvStockStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                tvStockQuantity.setText("(Not available)");

                // Disable buttons
                btnAddToCart.setEnabled(false);
                btnBuyNow.setEnabled(false);
            }

            // Description
            if (product.getDescription() != null && !product.getDescription().isEmpty()) {
                tvProductDescription.setText(product.getDescription());
            } else {
                tvProductDescription.setText("No description available.");
            }

            // Specifications
            tvProductColor.setText(product.getColor() != null ? product.getColor() : "N/A");
            tvProductOrigin.setText(product.getOriginCountry() != null ? product.getOriginCountry() : "N/A");

            if (product.getWarrantyPeriod() != null) {
                tvProductWarranty.setText(product.getWarrantyPeriod() + " months");
            } else {
                tvProductWarranty.setText("N/A");
            }

            // TODO: Load product image with Picasso or Glide
            // For now, using default image
            ivProductImage.setImageResource(R.drawable.ic_menu_business);

            // Update toolbar title
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(product.getName());
            }

            Log.d(TAG, "Product data displayed successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error displaying product data: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error displaying product information", Toast.LENGTH_SHORT).show();
        }
    }

    private void addToCart() {
        try {
            if (product != null) {
                CartManager.getInstance().addToCart(product);
                Snackbar.make(btnAddToCart, "Added to cart: " + product.getName(), Snackbar.LENGTH_SHORT).show();
                Log.d(TAG, "Product added to cart: " + product.getName());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding to cart: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error adding to cart", Toast.LENGTH_SHORT).show();
        }
    }

    private void buyNow() {
        try {
            if (product != null) {
                // Add to cart first
                CartManager.getInstance().addToCart(product);

                // Navigate to CartActivity
                Intent intent = new Intent(this, CartActivity.class);
                startActivity(intent);

                Log.d(TAG, "Buy now for product: " + product.getName());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in buy now: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error processing purchase", Toast.LENGTH_SHORT).show();
        }
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