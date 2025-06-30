package com.example.prm392_finalproject.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.prm392_finalproject.R;

import java.util.ArrayList;
import java.util.List;

import com.example.prm392_finalproject.dao.ProductDAO;
import com.example.prm392_finalproject.models.Product;
import com.example.prm392_finalproject.views.adapters.ProductAdapter;

public class ProductListActivity extends AppCompatActivity {

    private ListView listViewProducts;
    private SearchView searchViewProducts;
    private Button btnAddProduct;
    private ProductDAO productDAO;
    private ProductAdapter adapter;
    private List<Product> productList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("ProductListActivity: onCreate() started");

        try {
            super.onCreate(savedInstanceState);
            System.out.println("ProductListActivity: super.onCreate() completed");

            setContentView(R.layout.activity_product_list);
            System.out.println("ProductListActivity: setContentView completed");

            // Initialize views
            initializeViews();
            System.out.println("ProductListActivity: initializeViews completed");

            // Setup listeners
            setupEventListeners();
            System.out.println("ProductListActivity: setupEventListeners completed");

            // Load products from database
            loadProductsFromDatabase();

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

            listViewProducts = findViewById(R.id.listViewProducts);
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

            // Initialize DAO and product list
            productDAO = new ProductDAO();
            productList = new ArrayList<>();

            System.out.println("ProductListActivity: All views found successfully");

        } catch (Exception e) {
            System.out.println("ProductListActivity: ERROR in initializeViews: " + e.getMessage());
            throw e; // Re-throw so onCreate can catch it
        }
    }

    private void setupEventListeners() {
        try {
            // Add product button click listener
            btnAddProduct.setOnClickListener(v -> {
                Intent intent = new Intent(ProductListActivity.this, AddEditProductActivity.class);
                intent.putExtra("IS_EDIT", false);
                startActivity(intent);
            });

            // Search functionality
            searchViewProducts.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    searchProducts(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (newText.trim().isEmpty()) {
                        // If search is cleared, reload all products
                        loadProductsFromDatabase();
                    } else if (newText.length() >= 2) {
                        // Search when user types at least 2 characters
                        searchProducts(newText);
                    }
                    return true;
                }
            });

            // List item click listener
            listViewProducts.setOnItemClickListener((parent, view, position, id) -> {
                Product selectedProduct = productList.get(position);
                Intent intent = new Intent(ProductListActivity.this, ProductDetailActivity.class);
                intent.putExtra("PRODUCT_ID", selectedProduct.getProductId());
                startActivity(intent);
            });

            System.out.println("ProductListActivity: Event listeners setup completed");

        } catch (Exception e) {
            System.out.println("ProductListActivity: ERROR in setupEventListeners: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadProductsFromDatabase() {
        try {
            System.out.println("ProductListActivity: Loading products from database...");

            // Show loading message
            Toast.makeText(this, "Loading products...", Toast.LENGTH_SHORT).show();

            productDAO.getAllProductsAsync(new ProductDAO.ProductListCallback() {
                @Override
                public void onSuccess(List<Product> products) {
                    System.out.println("ProductListActivity: Products loaded successfully - Count: " + products.size());

                    productList.clear();
                    productList.addAll(products);

                    if (products.isEmpty()) {
                        Toast.makeText(ProductListActivity.this, "No products found in database", Toast.LENGTH_SHORT).show();
                    } else {
                        // Create and set adapter
                        adapter = new ProductAdapter(ProductListActivity.this, productList);
                        listViewProducts.setAdapter(adapter);
                        Toast.makeText(ProductListActivity.this, "Loaded " + products.size() + " products", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(String error) {
                    System.out.println("ProductListActivity: Error loading products: " + error);
                    Toast.makeText(ProductListActivity.this, "Database Error: " + error, Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            System.out.println("ProductListActivity: Exception in loadProductsFromDatabase: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void searchProducts(String searchTerm) {
        try {
            System.out.println("ProductListActivity: Searching products with term: " + searchTerm);

            productDAO.searchProductsAsync(searchTerm, new ProductDAO.ProductListCallback() {
                @Override
                public void onSuccess(List<Product> products) {
                    System.out.println("ProductListActivity: Search completed - Found: " + products.size() + " products");

                    productList.clear();
                    productList.addAll(products);

                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    } else {
                        adapter = new ProductAdapter(ProductListActivity.this, productList);
                        listViewProducts.setAdapter(adapter);
                    }

                    Toast.makeText(ProductListActivity.this, "Found " + products.size() + " products", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String error) {
                    System.out.println("ProductListActivity: Search error: " + error);
                    Toast.makeText(ProductListActivity.this, "Search Error: " + error, Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            System.out.println("ProductListActivity: Exception in searchProducts: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Search Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the product list when returning to this activity
        System.out.println("ProductListActivity: onResume - Refreshing product list");
        loadProductsFromDatabase();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up DAO resources
        if (productDAO != null) {
            productDAO.shutdown();
        }
        System.out.println("ProductListActivity: onDestroy completed");
    }
}