package com.example.prm392_finalproject.views;

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

            System.out.println("ProductListActivity: All views found successfully");

        } catch (Exception e) {
            System.out.println("ProductListActivity: ERROR in initializeViews: " + e.getMessage());
            throw e; // Re-throw để onCreate catch
        }
    }

    private void loadProductsFromDatabase() {
        try {
            System.out.println("ProductListActivity: Loading products from database...");

            ProductDAO productDAO = new ProductDAO();
            List<Product> products = productDAO.getAllProducts();

            if (products.isEmpty()) {
                Toast.makeText(this, "No products found in database", Toast.LENGTH_SHORT).show();
            } else {
                ProductAdapter adapter = new ProductAdapter(this, products);
                listViewProducts.setAdapter(adapter);
                Toast.makeText(this, "Loaded " + products.size() + " products from DB", Toast.LENGTH_SHORT).show();
            }

            System.out.println("ProductListActivity: Products loaded from database successfully");

        } catch (Exception e) {
            System.out.println("ProductListActivity: Error loading products from database: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Database Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

}