package com.example.prm392_finalproject.views;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.prm392_finalproject.R;

import java.util.ArrayList;
import java.util.List;

import com.example.prm392_finalproject.dao.ProductDAO;
import com.example.prm392_finalproject.models.Product;
import androidx.cardview.widget.CardView;

public class ProductListActivity extends AppCompatActivity {

    private LinearLayout listViewProducts;
    private SearchView searchViewProducts;
    private Button btnAddProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("ProductListActivity: MINIMAL VERSION - onCreate() started");

        try {
            super.onCreate(savedInstanceState);
            System.out.println("ProductListActivity: super.onCreate() completed");

            setContentView(R.layout.activity_product_list);
            System.out.println("ProductListActivity: setContentView completed");

            // Test chỉ initialize views
            initializeViews();
            System.out.println("ProductListActivity: initializeViews completed");

            // Set click listeners
            setClickListeners();
            System.out.println("ProductListActivity: setClickListeners completed");

            // Tự động load sản phẩm khi vào activity
            loadProductsFromDatabase();

            // Test message
            Toast.makeText(this, "Activity loaded successfully", Toast.LENGTH_SHORT).show();
            System.out.println("ProductListActivity: Toast shown");

            System.out.println("ProductListActivity: onCreate completed successfully");

        } catch (Exception e) {
            System.out.println("ProductListActivity: CRITICAL ERROR in onCreate: " + e.getMessage());
            e.printStackTrace();

            // Try to show error without crashing
            try {
                Toast.makeText(this, "Critical Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            } catch (Exception toastError) {
                System.out.println("ProductListActivity: Even Toast failed: " + toastError.getMessage());
            }
        }
    }

    private void initializeViews() {
        try {
            System.out.println("ProductListActivity: Finding views...");

            listViewProducts = findViewById(R.id.linearLayoutProducts);
            if (listViewProducts == null) {
                System.out.println("ProductListActivity: ERROR - listViewProducts is NULL");
                throw new RuntimeException("listViewProducts not found in layout");
            }
            System.out.println("ProductListActivity: listViewProducts found");

            searchViewProducts = findViewById(R.id.searchViewProducts);
            if (searchViewProducts == null) {
                System.out.println("ProductListActivity: ERROR - searchViewProducts is NULL");
                throw new RuntimeException("searchViewProducts not found in layout");
            }
            System.out.println("ProductListActivity: searchViewProducts found");

            btnAddProduct = findViewById(R.id.btnAddProduct);
            if (btnAddProduct == null) {
                System.out.println("ProductListActivity: ERROR - btnAddProduct is NULL");
                throw new RuntimeException("btnAddProduct not found in layout");
            }
            System.out.println("ProductListActivity: btnAddProduct found");

            System.out.println("ProductListActivity: All views found successfully");

        } catch (Exception e) {
            System.out.println("ProductListActivity: ERROR in initializeViews: " + e.getMessage());
            throw e; // Re-throw để onCreate catch
        }
    }

    private void setClickListeners() {
        try {
            btnAddProduct.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    System.out.println("ProductListActivity: Add Product button clicked");
                    Toast.makeText(ProductListActivity.this, "Add Product feature coming soon", Toast.LENGTH_SHORT).show();
                }
            });

            System.out.println("ProductListActivity: Click listeners set successfully");

        } catch (Exception e) {
            System.out.println("ProductListActivity: ERROR in setClickListeners: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadProductsFromDatabase() {
        System.out.println("ProductListActivity: Starting to load products from database...");
        
        // Chạy trong background thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("ProductListActivity: Loading products from database in background...");

                    ProductDAO productDAO = new ProductDAO();
                    List<Product> products = productDAO.getAllProducts();

                    System.out.println("ProductListActivity: Products loaded from database successfully. Count: " + (products != null ? products.size() : "null"));

                    // Update UI trên main thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (products != null && !products.isEmpty()) {
                                System.out.println("ProductListActivity: Displaying " + products.size() + " products");
                                displayProducts(products);
                                Toast.makeText(ProductListActivity.this, "Loaded " + products.size() + " products", Toast.LENGTH_SHORT).show();
                            } else {
                                System.out.println("ProductListActivity: No products found or null list");
                                Toast.makeText(ProductListActivity.this, "No products found", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } catch (Exception e) {
                    System.out.println("ProductListActivity: Error loading products from database: " + e.getMessage());
                    e.printStackTrace();
                    
                    // Show error trên main thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ProductListActivity.this, "Database Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }

    private void displayProducts(List<Product> products) {
        try {
            System.out.println("ProductListActivity: Displaying products...");
            
            // Clear existing views
            listViewProducts.removeAllViews();
            
            // Add each product as a card view
            for (Product product : products) {
                // Create a card layout for each product
                CardView cardView = new CardView(this);
                CardView.LayoutParams cardParams = new CardView.LayoutParams(
                    CardView.LayoutParams.MATCH_PARENT,
                    CardView.LayoutParams.WRAP_CONTENT
                );
                cardParams.setMargins(16, 8, 16, 8);
                cardView.setLayoutParams(cardParams);
                cardView.setCardElevation(4);
                cardView.setRadius(8);
                
                // Create linear layout inside card
                android.widget.LinearLayout linearLayout = new android.widget.LinearLayout(this);
                linearLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
                linearLayout.setPadding(16, 16, 16, 16);
                
                // Product name
                android.widget.TextView nameTextView = new android.widget.TextView(this);
                nameTextView.setText(product.getName());
                nameTextView.setTextSize(18);
                nameTextView.setTextColor(android.graphics.Color.BLACK);
                nameTextView.setTypeface(null, android.graphics.Typeface.BOLD);
                
                // Product description
                android.widget.TextView descTextView = new android.widget.TextView(this);
                descTextView.setText(product.getDescription() != null ? product.getDescription() : "No description");
                descTextView.setTextSize(14);
                descTextView.setTextColor(android.graphics.Color.GRAY);
                descTextView.setMaxLines(2);
                descTextView.setEllipsize(android.text.TextUtils.TruncateAt.END);
                
                // Product price
                android.widget.TextView priceTextView = new android.widget.TextView(this);
                priceTextView.setText("$" + String.format("%.2f", product.getPrice()));
                priceTextView.setTextSize(16);
                priceTextView.setTextColor(android.graphics.Color.parseColor("#FF5722"));
                priceTextView.setTypeface(null, android.graphics.Typeface.BOLD);
                
                // Stock info
                android.widget.TextView stockTextView = new android.widget.TextView(this);
                stockTextView.setText("Stock: " + product.getQuantityInStock() + " units");
                stockTextView.setTextSize(12);
                stockTextView.setTextColor(android.graphics.Color.DKGRAY);
                
                // Add views to linear layout
                linearLayout.addView(nameTextView);
                linearLayout.addView(descTextView);
                linearLayout.addView(priceTextView);
                linearLayout.addView(stockTextView);
                
                // Add linear layout to card
                cardView.addView(linearLayout);
                
                // Add card to main layout
                listViewProducts.addView(cardView);
            }
            
            System.out.println("ProductListActivity: Products displayed successfully");
            
        } catch (Exception e) {
            System.out.println("ProductListActivity: Error displaying products: " + e.getMessage());
            e.printStackTrace();
        }
    }

}