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
import com.example.prm392_finalproject.dao.ProductDAO;
import com.example.prm392_finalproject.models.Product;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddEditProductActivity extends AppCompatActivity {

    private EditText editTextProductName, editTextProductCode, editTextDescription;
    private EditText editTextPrice, editTextStock;
    private AutoCompleteTextView autoCompleteCategory;
    private Switch switchIsActive;

    private ExecutorService executorService;
    private ProductDAO productDAO;
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
            productDAO = new ProductDAO();

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
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        if (productDAO != null) {
            productDAO.shutdown();
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

        editTextDescription = new EditText(this);
        editTextDescription.setHint("Description");
        layout.addView(editTextDescription);

        editTextPrice = new EditText(this);
        editTextPrice.setHint("Price");
        editTextPrice.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(editTextPrice);

        editTextStock = new EditText(this);
        editTextStock.setHint("Stock Quantity");
        editTextStock.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(editTextStock);

        switchIsActive = new Switch(this);
        switchIsActive.setText("Active");
        switchIsActive.setChecked(true);
        layout.addView(switchIsActive);

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

            // Setup category dropdown with your actual categories
            if (autoCompleteCategory != null) {
                String[] categories = {"Smartphone", "Laptop", "Tablet", "Accessories"};
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
                productId = getIntent().getIntExtra("product_id", -1);
                System.out.println("AddEditProductActivity: Edit mode, product ID: " + productId);

                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Edit Product");
                }

                // Load product data from database
                loadProductDataFromDatabase();
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

    private void loadProductDataFromDatabase() {
        if (productId == -1) {
            Toast.makeText(this, "Invalid product ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use ProductDAO to load product data
        productDAO.getProductByIdAsync(productId, new ProductDAO.ProductCallback() {
            @Override
            public void onSuccess(Product product) {
                // Update UI on main thread
                runOnUiThread(() -> {
                    populateFields(product);
                    Toast.makeText(AddEditProductActivity.this, "Product loaded successfully", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(AddEditProductActivity.this, "Error loading product: " + error, Toast.LENGTH_LONG).show();
                    System.out.println("AddEditProductActivity: Error loading product: " + error);
                });
            }
        });
    }

    private void populateFields(Product product) {
        try {
            if (editTextProductName != null) editTextProductName.setText(product.getName() != null ? product.getName() : "");
            if (editTextProductCode != null) editTextProductCode.setText(product.getProductCode() != null ? product.getProductCode() : "");
            if (editTextDescription != null) editTextDescription.setText(product.getDescription() != null ? product.getDescription() : "");
            if (editTextPrice != null) editTextPrice.setText(String.valueOf(product.getPrice()));
            if (editTextStock != null) editTextStock.setText(String.valueOf(product.getQuantityInStock()));
            if (switchIsActive != null) switchIsActive.setChecked(product.isActive());

            // Set category if available
            if (autoCompleteCategory != null && product.getCategoryId() != null) {
                String[] categories = {"Smartphone", "Laptop", "Tablet", "Accessories"};
                int categoryIndex = product.getCategoryId() - 1; // Assuming category IDs start from 1
                if (categoryIndex >= 0 && categoryIndex < categories.length) {
                    autoCompleteCategory.setText(categories[categoryIndex]);
                }
            }
        } catch (Exception e) {
            System.out.println("AddEditProductActivity: Error populating fields: " + e.getMessage());
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

            // Set product ID for edit mode
            if (isEditMode) {
                product.setProductId(productId);
            }

            product.setName(editTextProductName.getText().toString().trim());

            if (editTextProductCode != null && !TextUtils.isEmpty(editTextProductCode.getText().toString().trim())) {
                product.setProductCode(editTextProductCode.getText().toString().trim());
            }

            if (editTextDescription != null && !TextUtils.isEmpty(editTextDescription.getText().toString().trim())) {
                product.setDescription(editTextDescription.getText().toString().trim());
            }

            try {
                double price = Double.parseDouble(editTextPrice.getText().toString().trim());
                product.setPrice(price);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid price format", Toast.LENGTH_SHORT).show();
                if (editTextPrice != null) editTextPrice.requestFocus();
                return;
            }

            if (editTextStock != null && !TextUtils.isEmpty(editTextStock.getText().toString().trim())) {
                try {
                    int stock = Integer.parseInt(editTextStock.getText().toString().trim());
                    product.setQuantityInStock(stock);
                } catch (NumberFormatException e) {
                    product.setQuantityInStock(0);
                }
            } else {
                product.setQuantityInStock(0);
            }

            if (switchIsActive != null) {
                product.setActive(switchIsActive.isChecked());
            } else {
                product.setActive(true);
            }

            // Set category ID based on selection
            if (autoCompleteCategory != null && !TextUtils.isEmpty(autoCompleteCategory.getText().toString().trim())) {
                String selectedCategory = autoCompleteCategory.getText().toString().trim();
                String[] categories = {"Smartphone", "Laptop", "Tablet", "Accessories"};
                for (int i = 0; i < categories.length; i++) {
                    if (categories[i].equals(selectedCategory)) {
                        product.setCategoryId(i + 1); // Category IDs start from 1
                        break;
                    }
                }
            } else {
                product.setCategoryId(1); // Default to first category
            }

            // Save product using DAO
            if (isEditMode) {
                // Update existing product
                productDAO.updateProductAsync(product, new ProductDAO.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(() -> {
                            Toast.makeText(AddEditProductActivity.this, "Product updated successfully", Toast.LENGTH_SHORT).show();
                            System.out.println("AddEditProductActivity: Product updated: " + product.getName());
                            setResult(RESULT_OK);
                            finish();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(AddEditProductActivity.this, "Error updating product: " + error, Toast.LENGTH_LONG).show();
                            System.out.println("AddEditProductActivity: Error updating product: " + error);
                        });
                    }
                });
            } else {
                // Add new product
                productDAO.addProductAsync(product, new ProductDAO.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(() -> {
                            Toast.makeText(AddEditProductActivity.this, "Product added successfully", Toast.LENGTH_SHORT).show();
                            System.out.println("AddEditProductActivity: Product added: " + product.getName());
                            setResult(RESULT_OK);
                            finish();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(AddEditProductActivity.this, "Error adding product: " + error, Toast.LENGTH_LONG).show();
                            System.out.println("AddEditProductActivity: Error adding product: " + error);
                        });
                    }
                });
            }

        } catch (Exception e) {
            System.out.println("AddEditProductActivity: Error saving product: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error saving product: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}