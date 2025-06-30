package com.example.prm392_finalproject.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.controllers.ProductController;
import com.example.prm392_finalproject.models.Product;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductDetailActivity extends AppCompatActivity {
    private ImageView imageViewProduct;
    private TextView textViewProductName, textViewProductCode, textViewDescription;
    private TextView textViewPrice, textViewCost, textViewStock, textViewSpecifications;
    private TextView textViewColor, textViewWeight, textViewDimensions;
    private TextView textViewWarranty, textViewOrigin, textViewReleaseDate;
    private TextView textViewStatus, textViewCreatedAt, textViewUpdatedAt;

    private ProductController productController;
    private ExecutorService executorService;
    private Product currentProduct;
    private int productId;
    private NumberFormat currencyFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        initializeViews();
        setupToolbar();
        setupExecutorService();
        getProductId();
        loadProductDetails();
    }

    private void initializeViews() {
        imageViewProduct = findViewById(R.id.imageViewProduct);
        textViewProductName = findViewById(R.id.textViewProductName);
        textViewProductCode = findViewById(R.id.textViewProductCode);
        textViewDescription = findViewById(R.id.textViewDescription);
        textViewPrice = findViewById(R.id.textViewPrice);
        textViewCost = findViewById(R.id.textViewCost);
        textViewStock = findViewById(R.id.textViewStock);
        textViewSpecifications = findViewById(R.id.textViewSpecifications);
        textViewColor = findViewById(R.id.textViewColor);
        textViewWeight = findViewById(R.id.textViewWeight);
        textViewDimensions = findViewById(R.id.textViewDimensions);
        textViewWarranty = findViewById(R.id.textViewWarranty);
        textViewOrigin = findViewById(R.id.textViewOrigin);
        textViewReleaseDate = findViewById(R.id.textViewReleaseDate);
        textViewStatus = findViewById(R.id.textViewStatus);
        textViewCreatedAt = findViewById(R.id.textViewCreatedAt);
        textViewUpdatedAt = findViewById(R.id.textViewUpdatedAt);

        productController = new ProductController();
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Product Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupExecutorService() {
        executorService = Executors.newSingleThreadExecutor();
    }

    private void getProductId() {
        productId = getIntent().getIntExtra("PRODUCT_ID", -1);
        if (productId == -1) {
            Toast.makeText(this, "Invalid product ID", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadProductDetails() {
        executorService.execute(() -> {
            try {
                currentProduct = productController.getProductById(productId);
                if (currentProduct != null) {
                    runOnUiThread(() -> displayProductDetails(currentProduct));
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error loading product: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void displayProductDetails(Product product) {
        // Basic information
        textViewProductName.setText(product.getName());
        textViewProductCode.setText(product.getProductCode());
        textViewDescription.setText(product.getDescription() != null ?
                product.getDescription() : "No description available");

        // Pricing and stock
        textViewPrice.setText("Price: " + currencyFormat.format(product.getPrice()));
        textViewCost.setText("Cost: " + (product.getCost() != null ?
                currencyFormat.format(product.getCost()) : "N/A"));

        int stock = product.getQuantityInStock();
        textViewStock.setText("Stock: " + stock);

        // Color code stock levels
        if (stock <= 5) {
            textViewStock.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else if (stock <= 20) {
            textViewStock.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        } else {
            textViewStock.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }

        // Product specifications
        textViewSpecifications.setText("Specifications: " + (product.getSpecifications() != null ?
                product.getSpecifications() : "N/A"));
        textViewColor.setText("Color: " + (product.getColor() != null ?
                product.getColor() : "N/A"));
        textViewWeight.setText("Weight: " + (product.getWeight() != null ?
                product.getWeight() + " kg" : "N/A"));
        textViewDimensions.setText("Dimensions: " + (product.getDimensions() != null ?
                product.getDimensions() : "N/A"));

        // Additional information
        textViewWarranty.setText("Warranty: " + (product.getWarrantyPeriod() != null ?
                product.getWarrantyPeriod() + " months" : "N/A"));
        textViewOrigin.setText("Origin: " + (product.getOriginCountry() != null ?
                product.getOriginCountry() : "N/A"));
        textViewReleaseDate.setText("Release Date: " + (product.getReleaseDate() != null ?
                product.getReleaseDate() : "N/A"));

        // Status and timestamps
        textViewStatus.setText("Status: " + (product.isActive() ? "Active" : "Inactive"));
        textViewCreatedAt.setText("Created: " + (product.getCreatedAt() != null ?
                product.getCreatedAt() : "N/A"));
        textViewUpdatedAt.setText("Updated: " + (product.getUpdatedAt() != null ?
                product.getUpdatedAt() : "N/A"));

        // Load product image
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.ic_product_placeholder)
                    .error(R.drawable.ic_product_placeholder)
                    .into(imageViewProduct);
        } else {
            imageViewProduct.setImageResource(R.drawable.ic_product_placeholder);
        }

        // Update toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(product.getName());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_product_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_edit) {
            editProduct();
            return true;
        } else if (id == R.id.action_delete) {
            showDeleteConfirmationDialog();
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void editProduct() {
        Intent intent = new Intent(this, AddEditProductActivity.class);
        intent.putExtra("PRODUCT_ID", productId);
        intent.putExtra("IS_EDIT", true);
        startActivity(intent);
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete this product? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteProduct())
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteProduct() {
        executorService.execute(() -> {
            try {
                productController.deleteProduct(productId);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Product deleted successfully", Toast.LENGTH_SHORT).show();
                    finish();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error deleting product: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProductDetails(); // Refresh product details when returning to this activity
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}