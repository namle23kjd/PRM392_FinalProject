package com.example.prm392_finalproject.views;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject.dao.ProductDAO;
import com.example.prm392_finalproject.dao.CategoryDAO;
import com.example.prm392_finalproject.models.Product;
import com.example.prm392_finalproject.models.CategoryResponse;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StaffActivity extends AppCompatActivity {

    private static final String TAG = "StaffActivity";

    // UI Components
    private TextView tvHeader;
    private Button btnProducts, btnCategories, btnAddStock, btnLogout;
    private EditText etSearch;
    private RecyclerView recyclerView;
    private StaffProductAdapter productAdapter;
    private StaffCategoryAdapter categoryAdapter;

    // Data
    private ProductDAO productDAO;
    private CategoryDAO categoryDAO;
    private List<Product> productList;
    private List<CategoryResponse> categoryList;
    private boolean showingProducts = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createUI();
        initData();
        setupEvents();
        loadProducts();

        Log.d(TAG, "StaffActivity created successfully");
    }

    private void createUI() {
        // Main container
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(20, 20, 20, 20);
        mainLayout.setBackgroundColor(0xFFF8F9FA);

        // Header
        tvHeader = new TextView(this);
        tvHeader.setText("🏪 QUẢN LÝ NHÂN VIÊN");
        tvHeader.setTextSize(22f);
        tvHeader.setTypeface(null, android.graphics.Typeface.BOLD);
        tvHeader.setTextColor(0xFF2C3E50);
        tvHeader.setGravity(Gravity.CENTER);
        tvHeader.setBackgroundColor(0xFFECF0F1);
        tvHeader.setPadding(20, 20, 20, 20);
        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        headerParams.setMargins(0, 0, 0, 30);
        tvHeader.setLayoutParams(headerParams);

        // Top buttons container (Products/Categories)
        LinearLayout topButtonContainer = new LinearLayout(this);
        topButtonContainer.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams topButtonContainerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        topButtonContainerParams.setMargins(0, 0, 0, 20);
        topButtonContainer.setLayoutParams(topButtonContainerParams);

        // Products button
        btnProducts = createButton("📦 SẢN PHẨM", 0xFF3498DB);
        LinearLayout.LayoutParams productBtnParams = new LinearLayout.LayoutParams(
                0, 100, 1f);
        productBtnParams.setMargins(0, 0, 10, 0);
        btnProducts.setLayoutParams(productBtnParams);

        // Categories button
        btnCategories = createButton("📂 THỂ LOẠI", 0xFF95A5A6);
        LinearLayout.LayoutParams categoryBtnParams = new LinearLayout.LayoutParams(
                0, 100, 1f);
        categoryBtnParams.setMargins(10, 0, 0, 0);
        btnCategories.setLayoutParams(categoryBtnParams);

        topButtonContainer.addView(btnProducts);
        topButtonContainer.addView(btnCategories);

        // Action buttons container (Add Stock / Logout)
        LinearLayout actionButtonContainer = new LinearLayout(this);
        actionButtonContainer.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams actionButtonContainerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        actionButtonContainerParams.setMargins(0, 0, 0, 25);
        actionButtonContainer.setLayoutParams(actionButtonContainerParams);

        // Add stock button - Nổi bật hơn
        btnAddStock = new Button(this);
        btnAddStock.setText("📦 NHẬP KHO");
        btnAddStock.setTextColor(0xFFFFFFFF);
        btnAddStock.setBackgroundColor(0xFF27AE60); // Màu xanh lá
        btnAddStock.setTextSize(16f);
        btnAddStock.setTypeface(null, android.graphics.Typeface.BOLD);
        btnAddStock.setAllCaps(false);
        LinearLayout.LayoutParams addStockParams = new LinearLayout.LayoutParams(
                0, 110, 1f); // Cao hơn một chút
        addStockParams.setMargins(0, 0, 10, 0);
        btnAddStock.setLayoutParams(addStockParams);

        // Logout button
        btnLogout = new Button(this);
        btnLogout.setText("🚪 ĐĂNG XUẤT");
        btnLogout.setTextColor(0xFFFFFFFF);
        btnLogout.setBackgroundColor(0xFFE74C3C); // Màu đỏ
        btnLogout.setTextSize(14f);
        btnLogout.setTypeface(null, android.graphics.Typeface.BOLD);
        btnLogout.setAllCaps(false);
        LinearLayout.LayoutParams logoutParams = new LinearLayout.LayoutParams(
                0, 110, 1f);
        logoutParams.setMargins(10, 0, 0, 0);
        btnLogout.setLayoutParams(logoutParams);

        actionButtonContainer.addView(btnAddStock);
        actionButtonContainer.addView(btnLogout);

        // Search section với label rõ ràng
        TextView searchLabel = new TextView(this);
        searchLabel.setText("🔍 TÌM KIẾM:");
        searchLabel.setTextSize(16f);
        searchLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        searchLabel.setTextColor(0xFF2C3E50);
        LinearLayout.LayoutParams searchLabelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        searchLabelParams.setMargins(0, 0, 0, 10);
        searchLabel.setLayoutParams(searchLabelParams);

        // Search field
        etSearch = new EditText(this);
        etSearch.setHint("Nhập từ khóa tìm kiếm...");
        etSearch.setBackgroundColor(0xFFFFFFFF);
        etSearch.setPadding(20, 20, 20, 20);
        etSearch.setTextSize(14f);
        etSearch.setSingleLine(true);
        LinearLayout.LayoutParams searchParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 100);
        searchParams.setMargins(0, 0, 0, 20);
        etSearch.setLayoutParams(searchParams);

        // RecyclerView
        recyclerView = new RecyclerView(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setBackgroundColor(0xFFFFFFFF);
        LinearLayout.LayoutParams recyclerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        recyclerParams.setMargins(0, 0, 0, 0);
        recyclerView.setLayoutParams(recyclerParams);

        // Add all to main layout
        mainLayout.addView(tvHeader);
        mainLayout.addView(topButtonContainer);
        mainLayout.addView(actionButtonContainer);
        mainLayout.addView(searchLabel);
        mainLayout.addView(etSearch);
        mainLayout.addView(recyclerView);

        setContentView(mainLayout);

        Log.d(TAG, "UI created with separated sections");
    }

    private Button createButton(String text, int color) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(0xFFFFFFFF);
        button.setBackgroundColor(color);
        button.setTextSize(14f);
        button.setTypeface(null, android.graphics.Typeface.BOLD);
        return button;
    }

    private void initData() {
        productDAO = new ProductDAO();
        categoryDAO = new CategoryDAO();
        productList = new ArrayList<>();
        categoryList = new ArrayList<>();
    }

    private void setupEvents() {
        btnProducts.setOnClickListener(v -> {
            if (!showingProducts) {
                showingProducts = true;
                updateUI();
                loadProducts();
            }
        });

        btnCategories.setOnClickListener(v -> {
            if (showingProducts) {
                showingProducts = false;
                updateUI();
                loadCategories();
            }
        });

        btnAddStock.setOnClickListener(v -> showAddStockDialog());

        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Đăng xuất")
                    .setMessage("Bạn có chắc muốn đăng xuất?")
                    .setPositiveButton("Có", (dialog, which) -> finish())
                    .setNegativeButton("Không", null)
                    .show();
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (showingProducts) {
                    searchProducts(query);
                } else {
                    searchCategories(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void updateUI() {
        if (showingProducts) {
            btnProducts.setBackgroundColor(0xFF3498DB);
            btnCategories.setBackgroundColor(0xFF95A5A6);
            tvHeader.setText("🏪 QUẢN LÝ NHÂN VIÊN - SẢN PHẨM");
            etSearch.setHint("🔍 Tìm kiếm sản phẩm...");
        } else {
            btnProducts.setBackgroundColor(0xFF95A5A6);
            btnCategories.setBackgroundColor(0xFF3498DB);
            tvHeader.setText("🏪 QUẢN LÝ NHÂN VIÊN - THỂ LOẠI");
            etSearch.setHint("🔍 Tìm kiếm thể loại...");
        }
        etSearch.setText("");
    }

    private void loadProducts() {
        new AsyncTask<Void, Void, List<Product>>() {
            @Override
            protected List<Product> doInBackground(Void... voids) {
                try {
                    return productDAO.getAllProducts();
                } catch (Exception e) {
                    Log.e(TAG, "Error loading products", e);
                    return new ArrayList<>();
                }
            }

            @Override
            protected void onPostExecute(List<Product> products) {
                productList.clear();
                productList.addAll(products);

                if (productAdapter == null) {
                    productAdapter = new StaffProductAdapter(productList);
                    recyclerView.setAdapter(productAdapter);
                } else {
                    productAdapter.notifyDataSetChanged();
                }

                Toast.makeText(StaffActivity.this, "Đã tải " + products.size() + " sản phẩm", Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }

    private void loadCategories() {
        new AsyncTask<Void, Void, List<CategoryResponse>>() {
            @Override
            protected List<CategoryResponse> doInBackground(Void... voids) {
                try {
                    return categoryDAO.getAllCategories();
                } catch (Exception e) {
                    Log.e(TAG, "Error loading categories", e);
                    return new ArrayList<>();
                }
            }

            @Override
            protected void onPostExecute(List<CategoryResponse> categories) {
                categoryList.clear();
                categoryList.addAll(categories);

                if (categoryAdapter == null) {
                    categoryAdapter = new StaffCategoryAdapter(categoryList);
                    recyclerView.setAdapter(categoryAdapter);
                } else {
                    categoryAdapter.notifyDataSetChanged();
                }

                Toast.makeText(StaffActivity.this, "Đã tải " + categories.size() + " thể loại", Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }

    private void searchProducts(String query) {
        if (query.isEmpty()) {
            loadProducts();
            return;
        }

        new AsyncTask<String, Void, List<Product>>() {
            @Override
            protected List<Product> doInBackground(String... params) {
                try {
                    return productDAO.searchProducts(params[0]);
                } catch (Exception e) {
                    Log.e(TAG, "Error searching products", e);
                    return new ArrayList<>();
                }
            }

            @Override
            protected void onPostExecute(List<Product> products) {
                productList.clear();
                productList.addAll(products);
                if (productAdapter != null) {
                    productAdapter.notifyDataSetChanged();
                }
            }
        }.execute(query);
    }

    private void searchCategories(String query) {
        if (categoryAdapter != null) {
            categoryAdapter.filter(query);
        }
    }

    private void showAddStockDialog() {
        // Dialog chọn loại nhập kho
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(40, 40, 40, 40);

        // Title
        TextView title = new TextView(this);
        title.setText("📦 NHẬP KHO");
        title.setTextSize(20f);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(0xFF2C3E50);
        title.setBackgroundColor(0xFFECF0F1);
        title.setPadding(24, 24, 24, 24);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(0, 0, 0, 30);
        title.setLayoutParams(titleParams);

        // Option buttons
        Button btnAddNewProduct = new Button(this);
        btnAddNewProduct.setText("➕ THÊM SẢN PHẨM MỚI");
        btnAddNewProduct.setTextColor(0xFFFFFFFF);
        btnAddNewProduct.setBackgroundColor(0xFF27AE60);
        btnAddNewProduct.setTextSize(16f);
        btnAddNewProduct.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams addNewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 120);
        addNewParams.setMargins(0, 0, 0, 20);
        btnAddNewProduct.setLayoutParams(addNewParams);

        Button btnIncreaseStock = new Button(this);
        btnIncreaseStock.setText("📈 TĂNG SỐ LƯỢNG SẢN PHẨM CÓ SẴN");
        btnIncreaseStock.setTextColor(0xFFFFFFFF);
        btnIncreaseStock.setBackgroundColor(0xFF3498DB);
        btnIncreaseStock.setTextSize(16f);
        btnIncreaseStock.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams increaseParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 120);
        increaseParams.setMargins(0, 0, 0, 20);
        btnIncreaseStock.setLayoutParams(increaseParams);

        // Note
        TextView note = new TextView(this);
        note.setText("💡 Chọn loại nhập kho:\n• Thêm mới: Tạo sản phẩm hoàn toàn mới\n• Tăng số lượng: Cập nhật kho sản phẩm đã có");
        note.setTextSize(14f);
        note.setTextColor(0xFF7F8C8D);
        note.setBackgroundColor(0xFFECF0F1);
        note.setPadding(20, 20, 20, 20);

        mainLayout.addView(title);
        mainLayout.addView(btnAddNewProduct);
        mainLayout.addView(btnIncreaseStock);
        mainLayout.addView(note);

        AlertDialog optionDialog = builder.setView(mainLayout)
                .setNegativeButton("❌ HỦY", null)
                .create();

        // Set button click listeners
        btnAddNewProduct.setOnClickListener(v -> {
            optionDialog.dismiss();
            showAddNewProductDialog();
        });

        btnIncreaseStock.setOnClickListener(v -> {
            optionDialog.dismiss();
            showIncreaseStockDialog();
        });

        optionDialog.show();
    }

    private void showAddNewProductDialog() {
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(30, 30, 30, 30);

        // Title
        TextView title = new TextView(this);
        title.setText("➕ THÊM SẢN PHẨM MỚI");
        title.setTextSize(18f);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(0xFF27AE60);
        title.setBackgroundColor(0xFFD5F4E6);
        title.setPadding(20, 20, 20, 20);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(0, 0, 0, 20);
        title.setLayoutParams(titleParams);

        // Create input field containers
        LinearLayout nameContainer = createInputField("Tên sản phẩm *", "Nhập tên sản phẩm");
        LinearLayout codeContainer = createInputField("Mã sản phẩm *", "Nhập mã sản phẩm");
        LinearLayout descContainer = createInputField("Mô tả", "Nhập mô tả sản phẩm");
        LinearLayout priceContainer = createNumberInputField("Giá bán *", "Nhập giá bán");
        LinearLayout costContainer = createNumberInputField("Giá nhập", "Nhập giá nhập");
        LinearLayout quantityContainer = createNumberInputField("Số lượng *", "Nhập số lượng ban đầu");
        LinearLayout colorContainer = createInputField("Màu sắc", "Nhập màu sắc");
        LinearLayout weightContainer = createNumberInputField("Trọng lượng (kg)", "Nhập trọng lượng");
        LinearLayout originContainer = createInputField("Xuất xứ", "Nhập xuất xứ");

        // Add fields to layout
        dialogLayout.addView(title);
        dialogLayout.addView(nameContainer);
        dialogLayout.addView(codeContainer);
        dialogLayout.addView(descContainer);
        dialogLayout.addView(priceContainer);
        dialogLayout.addView(costContainer);
        dialogLayout.addView(quantityContainer);
        dialogLayout.addView(colorContainer);
        dialogLayout.addView(weightContainer);
        dialogLayout.addView(originContainer);

        // Note
        TextView note = new TextView(this);
        note.setText("⚠️ Các trường có dấu (*) là bắt buộc");
        note.setTextSize(12f);
        note.setTextColor(0xFFE67E22);
        note.setBackgroundColor(0xFFFFEAA7);
        note.setPadding(15, 15, 15, 15);
        LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        noteParams.setMargins(0, 10, 0, 0);
        note.setLayoutParams(noteParams);
        dialogLayout.addView(note);

        // Create scrollable dialog
        android.widget.ScrollView scrollView = new android.widget.ScrollView(this);
        scrollView.addView(dialogLayout);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(scrollView)
                .setPositiveButton("✅ THÊM SẢN PHẨM", (d, which) -> {
                    // Get EditTexts from containers
                    EditText etName = getEditTextFromContainer(nameContainer);
                    EditText etCode = getEditTextFromContainer(codeContainer);
                    EditText etDescription = getEditTextFromContainer(descContainer);
                    EditText etPrice = getEditTextFromContainer(priceContainer);
                    EditText etCost = getEditTextFromContainer(costContainer);
                    EditText etQuantity = getEditTextFromContainer(quantityContainer);
                    EditText etColor = getEditTextFromContainer(colorContainer);
                    EditText etWeight = getEditTextFromContainer(weightContainer);
                    EditText etOrigin = getEditTextFromContainer(originContainer);

                    // Validate required fields
                    String name = etName.getText().toString().trim();
                    String code = etCode.getText().toString().trim();
                    String priceStr = etPrice.getText().toString().trim();
                    String quantityStr = etQuantity.getText().toString().trim();

                    if (name.isEmpty() || code.isEmpty() || priceStr.isEmpty() || quantityStr.isEmpty()) {
                        Toast.makeText(this, "❌ Vui lòng nhập đầy đủ các trường bắt buộc", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        // Create new product
                        Product newProduct = new Product();
                        newProduct.setName(name);
                        newProduct.setProductCode(code);
                        newProduct.setDescription(etDescription.getText().toString().trim());
                        newProduct.setPrice(Double.parseDouble(priceStr));

                        String costStr = etCost.getText().toString().trim();
                        if (!costStr.isEmpty()) {
                            newProduct.setCost(Double.parseDouble(costStr));
                        }

                        int quantity = Integer.parseInt(quantityStr);
                        newProduct.setQuantityInStock(quantity);
                        newProduct.setStockQuantity(quantity);
                        newProduct.setColor(etColor.getText().toString().trim());

                        String weightStr = etWeight.getText().toString().trim();
                        if (!weightStr.isEmpty()) {
                            newProduct.setWeight(Double.parseDouble(weightStr));
                        }

                        newProduct.setOriginCountry(etOrigin.getText().toString().trim());
                        newProduct.setActive(true);

                        // Set timestamps
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        String now = sdf.format(new Date());
                        newProduct.setCreatedAt(now);
                        newProduct.setUpdatedAt(now);

                        addNewProduct(newProduct);

                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "❌ Vui lòng nhập số hợp lệ cho giá và số lượng", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("❌ HỦY", null)
                .create();

        dialog.show();
    }

    private void showIncreaseStockDialog() {
        // Show loading message
        Toast.makeText(this, "Đang tải danh sách sản phẩm...", Toast.LENGTH_SHORT).show();

        // Load products for selection
        new AsyncTask<Void, Void, List<Product>>() {
            @Override
            protected List<Product> doInBackground(Void... voids) {
                try {
                    return productDAO.getAllProducts();
                } catch (Exception e) {
                    Log.e(TAG, "Error loading products for selection", e);
                    return new ArrayList<>();
                }
            }

            @Override
            protected void onPostExecute(List<Product> products) {
                if (products.isEmpty()) {
                    Toast.makeText(StaffActivity.this, "❌ Không có sản phẩm nào để tăng số lượng", Toast.LENGTH_SHORT).show();
                    return;
                }
                showProductSelectionDialog(products);
            }
        }.execute();
    }

    private void showProductSelectionDialog(List<Product> products) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(20, 20, 20, 20);

        // Title
        TextView title = new TextView(this);
        title.setText("📈 CHỌN SẢN PHẨM TĂNG SỐ LƯỢNG");
        title.setTextSize(18f);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(0xFF3498DB);
        title.setBackgroundColor(0xFFD6EAF8);
        title.setPadding(20, 20, 20, 20);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(0, 0, 0, 20);
        title.setLayoutParams(titleParams);

        // Search field for filtering products
        EditText etSearchProduct = new EditText(this);
        etSearchProduct.setHint("🔍 Tìm kiếm sản phẩm...");
        etSearchProduct.setBackgroundColor(0xFFFFFFFF);
        etSearchProduct.setPadding(20, 15, 20, 15);
        etSearchProduct.setTextSize(14f);
        LinearLayout.LayoutParams searchParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 80);
        searchParams.setMargins(0, 0, 0, 15);
        etSearchProduct.setLayoutParams(searchParams);

        // ScrollView for product list
        android.widget.ScrollView scrollView = new android.widget.ScrollView(this);
        LinearLayout productListLayout = new LinearLayout(this);
        productListLayout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(productListLayout);

        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 600); // Fixed height
        scrollView.setLayoutParams(scrollParams);

        // Add views to dialog
        dialogLayout.addView(title);
        dialogLayout.addView(etSearchProduct);
        dialogLayout.addView(scrollView);

        // Create dialog
        AlertDialog dialog = builder.setView(dialogLayout)
                .setNegativeButton("❌ HỦY", null)
                .create();

        // Display products
        displayProductsForSelection(products, productListLayout, dialog);

        // Setup search functionality
        etSearchProduct.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase().trim();
                List<Product> filteredProducts = new ArrayList<>();

                for (Product product : products) {
                    if (query.isEmpty() ||
                            product.getName().toLowerCase().contains(query) ||
                            (product.getProductCode() != null && product.getProductCode().toLowerCase().contains(query)) ||
                            (product.getDescription() != null && product.getDescription().toLowerCase().contains(query))) {
                        filteredProducts.add(product);
                    }
                }
                displayProductsForSelection(filteredProducts, productListLayout, dialog);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        dialog.show();
    }

    private void displayProductsForSelection(List<Product> products, LinearLayout container, AlertDialog parentDialog) {
        container.removeAllViews();

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        for (Product product : products) {
            // Create product card
            androidx.cardview.widget.CardView cardView = new androidx.cardview.widget.CardView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            cardParams.setMargins(0, 5, 0, 5);
            cardView.setLayoutParams(cardParams);
            cardView.setCardElevation(4f);
            cardView.setRadius(8f);
            cardView.setCardBackgroundColor(0xFFFFFFFF);

            // Card content
            LinearLayout cardContent = new LinearLayout(this);
            cardContent.setOrientation(LinearLayout.VERTICAL);
            cardContent.setPadding(15, 15, 15, 15);

            // Product name and code
            TextView nameView = new TextView(this);
            String nameText = "📦 " + product.getName();
            if (product.getProductCode() != null) {
                nameText += " (" + product.getProductCode() + ")";
            }
            nameView.setText(nameText);
            nameView.setTextSize(16f);
            nameView.setTypeface(null, android.graphics.Typeface.BOLD);
            nameView.setTextColor(0xFF2C3E50);

            // Product details
            TextView detailsView = new TextView(this);
            String details = "💰 " + currencyFormat.format(product.getPrice()) +
                    " | 📊 Tồn kho: " + product.getQuantityInStock();
            if (product.getCategory() != null) {
                details += " | 📂 " + product.getCategory();
            }
            detailsView.setText(details);
            detailsView.setTextSize(12f);
            detailsView.setTextColor(0xFF7F8C8D);

            // Stock warning
            if (product.getQuantityInStock() <= 5) {
                TextView warningView = new TextView(this);
                if (product.getQuantityInStock() == 0) {
                    warningView.setText("⚠️ HẾT HÀNG");
                    warningView.setTextColor(0xFFE74C3C);
                } else {
                    warningView.setText("⚠️ SẮP HẾT");
                    warningView.setTextColor(0xFFF39C12);
                }
                warningView.setTextSize(12f);
                warningView.setTypeface(null, android.graphics.Typeface.BOLD);
                cardContent.addView(warningView);
            }

            cardContent.addView(nameView);
            cardContent.addView(detailsView);
            cardView.addView(cardContent);

            // Set click listener
            cardView.setOnClickListener(v -> {
                parentDialog.dismiss();
                showQuantityInputDialog(product);
            });

            // Add ripple effect
            cardView.setClickable(true);
            cardView.setFocusable(true);

            container.addView(cardView);
        }

        // Show "no products found" message if list is empty
        if (products.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("🔍 Không tìm thấy sản phẩm nào");
            emptyView.setTextSize(14f);
            emptyView.setTextColor(0xFF95A5A6);
            emptyView.setGravity(Gravity.CENTER);
            emptyView.setPadding(20, 40, 20, 40);
            container.addView(emptyView);
        }
    }

    private void showQuantityInputDialog(Product selectedProduct) {
        // Debug: Log product info
        Log.d(TAG, "Selected product: ID=" + selectedProduct.getProductId() +
                ", Name=" + selectedProduct.getName() +
                ", Stock=" + selectedProduct.getQuantityInStock());

        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(30, 30, 30, 30);

        // Title with selected product info
        TextView title = new TextView(this);
        title.setText("📈 TĂNG SỐ LƯỢNG");
        title.setTextSize(18f);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(0xFF27AE60);
        title.setBackgroundColor(0xFFD5F4E6);
        title.setPadding(20, 20, 20, 20);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(0, 0, 0, 20);
        title.setLayoutParams(titleParams);

        // Selected product info with ID
        TextView productInfo = new TextView(this);
        productInfo.setText("🆔 ID: " + selectedProduct.getProductId() +
                "\n📦 Sản phẩm: " + selectedProduct.getName() +
                "\n🏷️ Mã: " + (selectedProduct.getProductCode() != null ? selectedProduct.getProductCode() : "N/A") +
                "\n📊 Tồn kho hiện tại: " + selectedProduct.getQuantityInStock());
        productInfo.setTextSize(14f);
        productInfo.setTextColor(0xFF2C3E50);
        productInfo.setBackgroundColor(0xFFECF0F1);
        productInfo.setPadding(15, 15, 15, 15);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        infoParams.setMargins(0, 0, 0, 20);
        productInfo.setLayoutParams(infoParams);

        // Quantity input
        TextView quantityLabel = new TextView(this);
        quantityLabel.setText("Số lượng cần thêm:");
        quantityLabel.setTextSize(16f);
        quantityLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        quantityLabel.setTextColor(0xFF2C3E50);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        labelParams.setMargins(0, 0, 0, 10);
        quantityLabel.setLayoutParams(labelParams);

        EditText etQuantity = new EditText(this);
        etQuantity.setHint("Nhập số lượng (ví dụ: 10, 50, 100...)");
        etQuantity.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etQuantity.setBackgroundColor(0xFFFFFFFF);
        etQuantity.setPadding(20, 20, 20, 20);
        etQuantity.setTextSize(16f);
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 100);
        inputParams.setMargins(0, 0, 0, 20);
        etQuantity.setLayoutParams(inputParams);

        // Note
        TextView note = new TextView(this);
        note.setText("💡 Số lượng sẽ được CỘNG vào tồn kho hiện tại\n" +
                "Ví dụ: " + selectedProduct.getQuantityInStock() + " + [số nhập] = [tồn kho mới]");
        note.setTextSize(12f);
        note.setTextColor(0xFF7F8C8D);
        note.setBackgroundColor(0xFFFFEAA7);
        note.setPadding(15, 15, 15, 15);

        dialogLayout.addView(title);
        dialogLayout.addView(productInfo);
        dialogLayout.addView(quantityLabel);
        dialogLayout.addView(etQuantity);
        dialogLayout.addView(note);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogLayout)
                .setPositiveButton("✅ TĂNG SỐ LƯỢNG", (d, which) -> {
                    String quantityStr = etQuantity.getText().toString().trim();

                    if (quantityStr.isEmpty()) {
                        Toast.makeText(this, "❌ Vui lòng nhập số lượng", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        int quantity = Integer.parseInt(quantityStr);

                        if (quantity <= 0) {
                            Toast.makeText(this, "❌ Số lượng phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Use the selected product object directly instead of ID lookup
                        increaseStockDirectly(selectedProduct, quantity);

                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "❌ Vui lòng nhập số hợp lệ", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("❌ HỦY", null)
                .create();

        dialog.show();

        // Focus on quantity input
        etQuantity.requestFocus();
    }

    private void increaseStockDirectly(Product product, int quantity) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    Log.d(TAG, "Increasing stock for product: ID=" + product.getProductId() +
                            ", Name=" + product.getName() +
                            ", Current stock=" + product.getQuantityInStock() +
                            ", Adding=" + quantity);

                    // Calculate new quantities
                    int newQuantity = product.getQuantityInStock() + quantity;
                    int newStock = product.getStockQuantity() + quantity;

                    // Update the product object
                    product.setQuantityInStock(newQuantity);
                    product.setStockQuantity(newStock);

                    // Update timestamp
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    product.setUpdatedAt(sdf.format(new Date()));

                    // Save to database
                    productDAO.updateProduct(product);

                    Log.d(TAG, "Stock updated successfully. New quantity: " + newQuantity);

                    return "✅ Đã tăng " + quantity + " sản phẩm '" + product.getName() + "'\nTồn kho mới: " + newQuantity;

                } catch (Exception e) {
                    Log.e(TAG, "Error increasing stock directly", e);
                    return "❌ Lỗi: " + e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String result) {
                Toast.makeText(StaffActivity.this, result, Toast.LENGTH_LONG).show();
                if (result.startsWith("✅") && showingProducts) {
                    loadProducts(); // Refresh the product list
                }
            }
        }.execute();
    }

    private LinearLayout createInputField(String label, String hint) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        containerParams.setMargins(0, 5, 0, 5);
        container.setLayoutParams(containerParams);

        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextSize(14f);
        labelView.setTypeface(null, android.graphics.Typeface.BOLD);
        labelView.setTextColor(0xFF2C3E50);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        labelParams.setMargins(0, 0, 0, 8);
        labelView.setLayoutParams(labelParams);

        EditText editText = new EditText(this);
        editText.setHint(hint);
        editText.setBackgroundColor(0xFFFFFFFF);
        editText.setPadding(20, 20, 20, 20);
        editText.setTextSize(14f);
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 100);
        editText.setLayoutParams(inputParams);

        container.addView(labelView);
        container.addView(editText);

        return container;
    }

    private LinearLayout createNumberInputField(String label, String hint) {
        LinearLayout container = createInputField(label, hint);
        EditText editText = (EditText) container.getChildAt(1); // Get EditText from container
        editText.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        return container;
    }

    private EditText getEditTextFromContainer(LinearLayout container) {
        return (EditText) container.getChildAt(1);
    }

    private void addNewProduct(Product product) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    productDAO.addProduct(product);
                    return "✅ Đã thêm sản phẩm mới '" + product.getName() + "' thành công!\nSố lượng: " + product.getQuantityInStock();

                } catch (Exception e) {
                    Log.e(TAG, "Error adding new product", e);
                    return "❌ Lỗi thêm sản phẩm: " + e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String result) {
                Toast.makeText(StaffActivity.this, result, Toast.LENGTH_LONG).show();
                if (result.startsWith("✅") && showingProducts) {
                    loadProducts();
                }
            }
        }.execute();
    }

    private void increaseStock(int productId, int quantity) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    Product product = productDAO.getProductById(productId);
                    if (product == null) {
                        return "❌ Không tìm thấy sản phẩm với ID: " + productId;
                    }

                    int newQuantity = product.getQuantityInStock() + quantity;
                    int newStock = product.getStockQuantity() + quantity;

                    product.setQuantityInStock(newQuantity);
                    product.setStockQuantity(newStock);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    product.setUpdatedAt(sdf.format(new Date()));

                    productDAO.updateProduct(product);

                    return "✅ Đã tăng " + quantity + " sản phẩm '" + product.getName() + "'\nTồn kho mới: " + newQuantity;

                } catch (Exception e) {
                    Log.e(TAG, "Error increasing stock", e);
                    return "❌ Lỗi: " + e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String result) {
                Toast.makeText(StaffActivity.this, result, Toast.LENGTH_LONG).show();
                if (result.startsWith("✅") && showingProducts) {
                    loadProducts();
                }
            }
        }.execute();
    }

    // Inner class for Product Adapter
    private class StaffProductAdapter extends RecyclerView.Adapter<StaffProductAdapter.ProductViewHolder> {
        private List<Product> products;
        private NumberFormat currencyFormat;

        public StaffProductAdapter(List<Product> products) {
            this.products = products;
            this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        }

        @Override
        public ProductViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, parent, false);
            view.setPadding(20, 15, 20, 15);
            view.setBackgroundColor(0xFFFFFFFF);

            android.view.ViewGroup.MarginLayoutParams params = new android.view.ViewGroup.MarginLayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(10, 5, 10, 5);
            view.setLayoutParams(params);
            view.setElevation(4f);

            return new ProductViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ProductViewHolder holder, int position) {
            Product product = products.get(position);

            String title = "📦 " + product.getName();
            int stock = product.getQuantityInStock();

            if (stock == 0) {
                title += " ⚠️ HẾT HÀNG";
                holder.title.setTextColor(0xFFE74C3C);
            } else if (stock <= 5) {
                title += " ⚠️ SẮP HẾT";
                holder.title.setTextColor(0xFFF39C12);
            } else {
                title += " ✅";
                holder.title.setTextColor(0xFF27AE60);
            }

            holder.title.setText(title);
            holder.title.setTextSize(16f);
            holder.title.setTypeface(null, android.graphics.Typeface.BOLD);

            String subtitle = "🏷️ " + (product.getProductCode() != null ? product.getProductCode() : "N/A") +
                    " | 💰 " + currencyFormat.format(product.getPrice()) +
                    "\n📊 Tồn kho: " + product.getQuantityInStock() +
                    " | 🏪 Kho: " + product.getStockQuantity();

            if (product.getCategory() != null) {
                subtitle += "\n📂 " + product.getCategory();
            }

            subtitle += " | " + (product.isActive() ? "🟢 Hoạt động" : "🔴 Tạm dừng");

            holder.subtitle.setText(subtitle);
            holder.subtitle.setTextSize(12f);
            holder.subtitle.setTextColor(0xFF34495E);
        }

        @Override
        public int getItemCount() {
            return products.size();
        }

        class ProductViewHolder extends RecyclerView.ViewHolder {
            TextView title, subtitle;

            ProductViewHolder(View itemView) {
                super(itemView);
                title = itemView.findViewById(android.R.id.text1);
                subtitle = itemView.findViewById(android.R.id.text2);
            }
        }
    }

    // Inner class for Category Adapter
    private class StaffCategoryAdapter extends RecyclerView.Adapter<StaffCategoryAdapter.CategoryViewHolder> {
        private List<CategoryResponse> categories;
        private List<CategoryResponse> categoriesFiltered;

        public StaffCategoryAdapter(List<CategoryResponse> categories) {
            this.categories = categories;
            this.categoriesFiltered = new ArrayList<>(categories);
        }

        @Override
        public CategoryViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, parent, false);
            view.setPadding(20, 15, 20, 15);
            view.setBackgroundColor(0xFFFFFFFF);

            android.view.ViewGroup.MarginLayoutParams params = new android.view.ViewGroup.MarginLayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(10, 5, 10, 5);
            view.setLayoutParams(params);
            view.setElevation(4f);

            return new CategoryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CategoryViewHolder holder, int position) {
            CategoryResponse category = categoriesFiltered.get(position);

            String title = "📂 " + category.getName();
            if (category.isIs_deleted()) {
                title += " ❌";
                holder.title.setTextColor(0xFFE74C3C);
            } else {
                title += " ✅";
                holder.title.setTextColor(0xFF27AE60);
            }

            holder.title.setText(title);
            holder.title.setTextSize(16f);
            holder.title.setTypeface(null, android.graphics.Typeface.BOLD);

            String subtitle = "🆔 ID: " + category.getCategory_id();
            if (category.getDescription() != null && !category.getDescription().trim().isEmpty()) {
                subtitle += "\n📝 " + category.getDescription();
            }
            subtitle += "\n" + (category.isIs_deleted() ? "🔴 Đã xóa" : "🟢 Hoạt động");

            holder.subtitle.setText(subtitle);
            holder.subtitle.setTextSize(12f);
            holder.subtitle.setTextColor(0xFF34495E);
        }

        @Override
        public int getItemCount() {
            return categoriesFiltered.size();
        }

        public void filter(String query) {
            categoriesFiltered.clear();
            if (query.isEmpty()) {
                categoriesFiltered.addAll(categories);
            } else {
                query = query.toLowerCase();
                for (CategoryResponse category : categories) {
                    if (category.getName().toLowerCase().contains(query) ||
                            (category.getDescription() != null && category.getDescription().toLowerCase().contains(query)) ||
                            String.valueOf(category.getCategory_id()).contains(query)) {
                        categoriesFiltered.add(category);
                    }
                }
            }
            notifyDataSetChanged();
        }

        class CategoryViewHolder extends RecyclerView.ViewHolder {
            TextView title, subtitle;

            CategoryViewHolder(View itemView) {
                super(itemView);
                title = itemView.findViewById(android.R.id.text1);
                subtitle = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}