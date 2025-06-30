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
import com.example.prm392_finalproject.models.Product;
import com.example.prm392_finalproject.adapters.ProductAdapter;

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

            // Test với fake data
            testWithFakeData();

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

    private void testWithFakeData() {
        try {
            System.out.println("ProductListActivity: Testing with Product objects");

            // Tạo fake Product objects
            List<Product> testProducts = new ArrayList<>();

            Product p1 = new Product();
            p1.setProductId(1);
            p1.setName("Test Product 1");
            p1.setProductCode("TEST001");
            p1.setPrice(99.99);
            p1.setQuantityInStock(10);
            testProducts.add(p1);

            Product p2 = new Product();
            p2.setProductId(2);
            p2.setName("Test Product 2");
            p2.setProductCode("TEST002");
            p2.setPrice(149.99);
            p2.setQuantityInStock(5);
            testProducts.add(p2);

            // Sử dụng ProductAdapter
            ProductAdapter adapter = new ProductAdapter(this, testProducts);
            listViewProducts.setAdapter(adapter);

            System.out.println("ProductListActivity: Product data loaded successfully");
            Toast.makeText(this, "Loaded " + testProducts.size() + " products", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            System.out.println("ProductListActivity: Error in testWithFakeData: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}