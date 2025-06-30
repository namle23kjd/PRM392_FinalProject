package com.example.prm392_finalproject.views;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.models.Product;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddEditProductActivity extends AppCompatActivity {

    private EditText editTextProductName, editTextProductCode, editTextDescription;
    private EditText editTextPrice, editTextStock;
    private AutoCompleteTextView autoCompleteCategory;
    private Switch switchIsActive;

    private ExecutorService executorService;
    private boolean isEditMode = false;
    private int productId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("AddEditProductActivity: onCreate started");

        try {
            super.onCreate(savedInstanceState);

            // Test setContentView
            try {
                setContentView(R.layout.activity_add_edit_product);
                System.out.println("AddEditProductActivity: Layout set successfully");
            } catch (Exception layoutError) {
                System.out.println("AddEditProductActivity: Layout error: " + layoutError.getMessage());
                // Create minimal layout programmatically
                createMinimalLayout();
            }

            executorService = Executors.newSingleThreadExecutor();

            // Initialize views safely
            initializeViewsSafely();

            // Setup toolbar safely
            setupToolbarSafely();

            // Check edit mode
            checkEditMode();

            System.out.println("AddEditProductActivity: onCreate completed");

        } catch (Exception e) {
            System.out.println("AddEditProductActivity: Critical error in onCreate: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            // Don't finish() - let user see the error
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    private void createMinimalLayout() {
        // Create simple layout programmatically if XML fails
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);

        // Add basic views
        editTextProductName = new EditText(this);
        editTextProductName.setHint("Product Name");
        layout.addView(editTextProductName);

        editTextProductCode = new EditText(this);
        editTextProductCode.setHint("Product Code");
        layout.addView(editTextProductCode);

        editTextPrice = new EditText(this);
        editTextPrice.setHint("Price");
        editTextPrice.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(editTextPrice);

        android.widget.Button saveButton = new android.widget.Button(this);
        saveButton.setText("Save");
        saveButton.setOnClickListener(v -> saveProductSafely());
        layout.addView(saveButton);

        setContentView(layout);
        System.out.println("AddEditProductActivity: Minimal layout created");
    }

    private void initializeViewsSafely() {
        try {
            System.out.println("AddEditProductActivity: Initializing views");

            // Try to find views, set to null if not found
            try { editTextProductName = findViewById(R.id.editTextProductName); } catch (Exception e) { System.out.println("editTextProductName not found"); }
            try { editTextProductCode = findViewById(R.id.editTextProductCode); } catch (Exception e) { System.out.println("editTextProductCode not found"); }
            try { editTextDescription = findViewById(R.id.editTextDescription); } catch (Exception e) { System.out.println("editTextDescription not found"); }
            try { editTextPrice = findViewById(R.id.editTextPrice); } catch (Exception e) { System.out.println("editTextPrice not found"); }
            try { editTextStock = findViewById(R.id.editTextStock); } catch (Exception e) { System.out.println("editTextStock not found"); }
            try { autoCompleteCategory = findViewById(R.id.autoCompleteCategory); } catch (Exception e) { System.out.println("autoCompleteCategory not found"); }
            try { switchIsActive = findViewById(R.id.switchIsActive); } catch (Exception e) { System.out.println("switchIsActive not found"); }

            // Setup simple category dropdown if available
            if (autoCompleteCategory != null) {
                String[] categories = {"Electronics", "Clothing", "Books", "Home & Garden", "Sports"};
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
                autoCompleteCategory.setAdapter(adapter);
            }

            System.out.println("AddEditProductActivity: Views initialized safely");

        } catch (Exception e) {
            System.out.println("AddEditProductActivity: Error initializing views: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupToolbarSafely() {
        try {
            Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setTitle(isEditMode ? "Edit Product" : "Add Product");
                }
            }
        } catch (Exception e) {
            System.out.println("AddEditProductActivity: Toolbar setup failed: " + e.getMessage());
        }
    }

    private void checkEditMode() {
        try {
            if (getIntent().hasExtra("IS_EDIT") && getIntent().getBooleanExtra("IS_EDIT", false)) {
                isEditMode = true;
                productId = getIntent().getIntExtra("PRODUCT_ID", -1);
                System.out.println("AddEditProductActivity: Edit mode, product ID: " + productId);

                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Edit Product");
                }

                // Load product data (simplified)
                loadProductDataSafely();
            } else {
                System.out.println("AddEditProductActivity: Add mode");
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Add Product");
                }
            }
        } catch (Exception e) {
            System.out.println("AddEditProductActivity: Error checking edit mode: " + e.getMessage());
        }
    }

    private void loadProductDataSafely() {
        // For now, just show test data in edit mode
        try {
            if (editTextProductName != null) editTextProductName.setText("Sample Product " + productId);
            if (editTextProductCode != null) editTextProductCode.setText("PRD" + String.format("%03d", productId));
            if (editTextDescription != null) editTextDescription.setText("Sample description for product " + productId);
            if (editTextPrice != null) editTextPrice.setText("99.99");
            if (editTextStock != null) editTextStock.setText("10");
            if (switchIsActive != null) switchIsActive.setChecked(true);

            Toast.makeText(this, "Loaded test data for product " + productId, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            System.out.println("AddEditProductActivity: Error loading test data: " + e.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            getMenuInflater().inflate(R.menu.menu_save, menu);
            return true;
        } catch (Exception e) {
            System.out.println("AddEditProductActivity: Menu creation failed: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            saveProductSafely();
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveProductSafely() {
        System.out.println("AddEditProductActivity: Save product started");

        try {
            // Validate basic fields
            if (editTextProductName == null || TextUtils.isEmpty(editTextProductName.getText().toString().trim())) {
                Toast.makeText(this, "Product name is required", Toast.LENGTH_SHORT).show();
                if (editTextProductName != null) editTextProductName.requestFocus();
                return;
            }

            if (editTextPrice == null || TextUtils.isEmpty(editTextPrice.getText().toString().trim())) {
                Toast.makeText(this, "Price is required", Toast.LENGTH_SHORT).show();
                if (editTextPrice != null) editTextPrice.requestFocus();
                return;
            }

            // Create product object
            Product product = new Product();
            product.setName(editTextProductName.getText().toString().trim());

            if (editTextProductCode != null) {
                product.setProductCode(editTextProductCode.getText().toString().trim());
            }

            if (editTextDescription != null) {
                product.setDescription(editTextDescription.getText().toString().trim());
            }

            try {
                double price = Double.parseDouble(editTextPrice.getText().toString().trim());
                product.setPrice(price);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid price format", Toast.LENGTH_SHORT).show();
                editTextPrice.requestFocus();
                return;
            }

            if (editTextStock != null && !TextUtils.isEmpty(editTextStock.getText().toString().trim())) {
                try {
                    int stock = Integer.parseInt(editTextStock.getText().toString().trim());
                    product.setQuantityInStock(stock);
                } catch (NumberFormatException e) {
                    product.setQuantityInStock(0);
                }
            }

            if (switchIsActive != null) {
                product.setActive(switchIsActive.isChecked());
            } else {
                product.setActive(true);
            }

            // For now, just show success message (no database save)
            String message = isEditMode ? "Product updated successfully (test mode)" : "Product added successfully (test mode)";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

            System.out.println("AddEditProductActivity: Product saved: " + product.getName());

            // Close activity
            finish();

        } catch (Exception e) {
            System.out.println("AddEditProductActivity: Error saving product: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error saving product: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}