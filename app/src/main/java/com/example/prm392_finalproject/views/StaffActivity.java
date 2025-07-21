package com.example.prm392_finalproject.views;

import android.app.AlertDialog;
import android.content.Intent;
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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.dao.ProductDAO;
import com.example.prm392_finalproject.dao.CategoryDAO;
import com.example.prm392_finalproject.models.Product;
import com.example.prm392_finalproject.models.CategoryResponse;
// Thêm vào đầu file StaffActivity.java
import com.example.prm392_finalproject.views.ShippingActivity;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import android.content.SharedPreferences;
import android.view.HapticFeedbackConstants;
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
// Shipping button - THÊM MỚI
        Button btnShipping = new Button(this);
        btnShipping.setText("🚚 VẬN CHUYỂN");
        btnShipping.setTextColor(0xFFFFFFFF);
        btnShipping.setBackgroundColor(0xFF9B59B6); // Màu tím
        btnShipping.setTextSize(13f);
        btnShipping.setTypeface(null, android.graphics.Typeface.BOLD);
        btnShipping.setAllCaps(false);
        LinearLayout.LayoutParams shippingParams = new LinearLayout.LayoutParams(0, 110, 1f);
        shippingParams.setMargins(8, 0, 8, 0);
        btnShipping.setLayoutParams(shippingParams);
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
        actionButtonContainer.addView(btnShipping);
        // Thêm click listener cho shipping button - THÊM MỚI
        btnShipping.setOnClickListener(v -> {
            Intent shippingIntent = new Intent(this, ShippingActivity.class);
            startActivity(shippingIntent);
            Log.d(TAG, "Opened ShippingActivity");
        });
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
                    .setPositiveButton("Có", (dialog, which) -> performLogout())
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

        // Create input field containers for ALL attributes
        LinearLayout nameContainer = createInputField("Tên sản phẩm *", "Nhập tên sản phẩm");
        LinearLayout codeContainer = createInputField("Mã sản phẩm *", "Nhập mã sản phẩm");
        LinearLayout descContainer = createInputField("Mô tả", "Nhập mô tả sản phẩm");
        LinearLayout specificationsContainer = createInputField("Thông số kỹ thuật", "Nhập thông số kỹ thuật");
        LinearLayout priceContainer = createNumberInputField("Giá bán *", "Nhập giá bán");
        LinearLayout costContainer = createNumberInputField("Giá nhập", "Nhập giá nhập");
        LinearLayout quantityContainer = createNumberInputField("Số lượng *", "Nhập số lượng ban đầu");
        LinearLayout stockContainer = createNumberInputField("Kho dự trữ", "Nhập số lượng kho dự trữ");
        LinearLayout colorContainer = createInputField("Màu sắc", "Nhập màu sắc");
        LinearLayout weightContainer = createNumberInputField("Trọng lượng (kg)", "Nhập trọng lượng");
        LinearLayout dimensionsContainer = createInputField("Kích thước", "Nhập kích thước (VxDxR)");
        LinearLayout warrantyContainer = createNumberInputField("Bảo hành (tháng)", "Nhập thời gian bảo hành");
        LinearLayout originContainer = createInputField("Xuất xứ", "Nhập xuất xứ");
        LinearLayout releaseDateContainer = createInputField("Ngày phát hành", "YYYY-MM-DD");
        LinearLayout qrCodeContainer = createInputField("Mã QR", "Nhập mã QR");
        LinearLayout categoryIdContainer = createNumberInputField("ID Danh mục", "Nhập ID danh mục");
        LinearLayout categoryContainer = createInputField("Tên danh mục", "Nhập tên danh mục");
        LinearLayout imageUrlContainer = createInputField("URL hình ảnh", "Nhập đường dẫn hình ảnh");

        // Add fields to layout
        dialogLayout.addView(title);
        dialogLayout.addView(nameContainer);
        dialogLayout.addView(codeContainer);
        dialogLayout.addView(descContainer);
        dialogLayout.addView(specificationsContainer);
        dialogLayout.addView(priceContainer);
        dialogLayout.addView(costContainer);
        dialogLayout.addView(quantityContainer);
        dialogLayout.addView(stockContainer);
        dialogLayout.addView(colorContainer);
        dialogLayout.addView(weightContainer);
        dialogLayout.addView(dimensionsContainer);
        dialogLayout.addView(warrantyContainer);
        dialogLayout.addView(originContainer);
        dialogLayout.addView(releaseDateContainer);
        dialogLayout.addView(qrCodeContainer);
        dialogLayout.addView(categoryIdContainer);
        dialogLayout.addView(categoryContainer);
        dialogLayout.addView(imageUrlContainer);

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
                    EditText etSpecifications = getEditTextFromContainer(specificationsContainer);
                    EditText etPrice = getEditTextFromContainer(priceContainer);
                    EditText etCost = getEditTextFromContainer(costContainer);
                    EditText etQuantity = getEditTextFromContainer(quantityContainer);
                    EditText etStock = getEditTextFromContainer(stockContainer);
                    EditText etColor = getEditTextFromContainer(colorContainer);
                    EditText etWeight = getEditTextFromContainer(weightContainer);
                    EditText etDimensions = getEditTextFromContainer(dimensionsContainer);
                    EditText etWarranty = getEditTextFromContainer(warrantyContainer);
                    EditText etOrigin = getEditTextFromContainer(originContainer);
                    EditText etReleaseDate = getEditTextFromContainer(releaseDateContainer);
                    EditText etQrCode = getEditTextFromContainer(qrCodeContainer);
                    EditText etCategoryId = getEditTextFromContainer(categoryIdContainer);
                    EditText etCategory = getEditTextFromContainer(categoryContainer);
                    EditText etImageUrl = getEditTextFromContainer(imageUrlContainer);

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
                        // Create new product with ALL attributes
                        Product newProduct = new Product();
                        newProduct.setName(name);
                        newProduct.setProductCode(code);
                        newProduct.setDescription(etDescription.getText().toString().trim());
                        newProduct.setSpecifications(etSpecifications.getText().toString().trim());
                        newProduct.setPrice(Double.parseDouble(priceStr));

                        String costStr = etCost.getText().toString().trim();
                        if (!costStr.isEmpty()) {
                            newProduct.setCost(Double.parseDouble(costStr));
                        }

                        int quantity = Integer.parseInt(quantityStr);
                        newProduct.setQuantityInStock(quantity);

                        String stockStr = etStock.getText().toString().trim();
                        if (!stockStr.isEmpty()) {
                            newProduct.setStockQuantity(Integer.parseInt(stockStr));
                        } else {
                            newProduct.setStockQuantity(quantity); // Default to same as quantity
                        }

                        newProduct.setColor(etColor.getText().toString().trim());

                        String weightStr = etWeight.getText().toString().trim();
                        if (!weightStr.isEmpty()) {
                            newProduct.setWeight(Double.parseDouble(weightStr));
                        }

                        newProduct.setDimensions(etDimensions.getText().toString().trim());

                        String warrantyStr = etWarranty.getText().toString().trim();
                        if (!warrantyStr.isEmpty()) {
                            newProduct.setWarrantyPeriod(Integer.parseInt(warrantyStr));
                        }

                        newProduct.setOriginCountry(etOrigin.getText().toString().trim());
                        newProduct.setReleaseDate(etReleaseDate.getText().toString().trim());
                        newProduct.setQrCode(etQrCode.getText().toString().trim());

                        String categoryIdStr = etCategoryId.getText().toString().trim();
                        if (!categoryIdStr.isEmpty()) {
                            newProduct.setCategoryId(Integer.parseInt(categoryIdStr));
                        }

                        newProduct.setCategory(etCategory.getText().toString().trim());
                        newProduct.setImageUrl(etImageUrl.getText().toString().trim());
                        newProduct.setActive(true);

                        // Set timestamps
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        String now = sdf.format(new Date());
                        newProduct.setCreatedAt(now);
                        newProduct.setUpdatedAt(now);

                        addNewProduct(newProduct);

                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "❌ Vui lòng nhập số hợp lệ cho các trường số", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("❌ HỦY", null)
                .create();

        dialog.show();
    }
    private void showUpdateProductDialog(Product product) {
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(30, 30, 30, 30);

        TextView title = new TextView(this);
        title.setText("✏️ CẬP NHẬT SẢN PHẨM");
        title.setTextSize(18f);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(0xFF2980B9);
        title.setBackgroundColor(0xFFD6EAF8);
        title.setPadding(20, 20, 20, 20);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(0, 0, 0, 20);
        title.setLayoutParams(titleParams);

        // Current product info
        TextView currentInfo = new TextView(this);
        currentInfo.setText("📦 Sản phẩm: " + product.getName() + " (ID: " + product.getProductId() + ")" +
                "\n🏷️ Mã: " + (product.getProductCode() != null ? product.getProductCode() : "N/A") +
                "\n💰 Giá: " + String.format(Locale.getDefault(), "%.0f VND", product.getPrice()) +
                "\n📊 Tồn kho: " + product.getQuantityInStock());
        currentInfo.setTextSize(14f);
        currentInfo.setTextColor(0xFF2C3E50);
        currentInfo.setBackgroundColor(0xFFECF0F1);
        currentInfo.setPadding(15, 15, 15, 15);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        infoParams.setMargins(0, 0, 0, 20);
        currentInfo.setLayoutParams(infoParams);

        // Input fields - pre-filled with current values using new methods
        LinearLayout nameContainer = createInputFieldWithValue("Tên sản phẩm", product.getName());
        LinearLayout codeContainer = createInputFieldWithValue("Mã sản phẩm", product.getProductCode());
        LinearLayout descContainer = createInputFieldWithValue("Mô tả", product.getDescription());
        LinearLayout specificationsContainer = createInputFieldWithValue("Thông số kỹ thuật", product.getSpecifications());
        LinearLayout priceContainer = createNumberInputFieldWithValue("Giá bán", String.valueOf(product.getPrice()));
        LinearLayout costContainer = createNumberInputFieldWithValue("Giá nhập",
                product.getCost() != null ? String.valueOf(product.getCost()) : "");
        LinearLayout quantityContainer = createNumberInputFieldWithValue("Số lượng", String.valueOf(product.getQuantityInStock()));
        LinearLayout stockContainer = createNumberInputFieldWithValue("Kho dự trữ", String.valueOf(product.getStockQuantity()));
        LinearLayout colorContainer = createInputFieldWithValue("Màu sắc", product.getColor());
        LinearLayout weightContainer = createNumberInputFieldWithValue("Trọng lượng (kg)",
                product.getWeight() != null ? String.valueOf(product.getWeight()) : "");
        LinearLayout dimensionsContainer = createInputFieldWithValue("Kích thước", product.getDimensions());
        LinearLayout warrantyContainer = createNumberInputFieldWithValue("Bảo hành (tháng)",
                product.getWarrantyPeriod() != null ? String.valueOf(product.getWarrantyPeriod()) : "");
        LinearLayout originContainer = createInputFieldWithValue("Xuất xứ", product.getOriginCountry());
        LinearLayout releaseDateContainer = createInputFieldWithValue("Ngày phát hành", product.getReleaseDate());
        LinearLayout qrCodeContainer = createInputFieldWithValue("Mã QR", product.getQrCode());
        LinearLayout categoryIdContainer = createNumberInputFieldWithValue("ID Danh mục",
                product.getCategoryId() != null ? String.valueOf(product.getCategoryId()) : "");
        LinearLayout categoryContainer = createInputFieldWithValue("Tên danh mục", product.getCategory());
        LinearLayout imageUrlContainer = createInputFieldWithValue("URL hình ảnh", product.getImageUrl());

        // Active status toggle
        LinearLayout activeContainer = new LinearLayout(this);
        activeContainer.setOrientation(LinearLayout.HORIZONTAL);
        activeContainer.setPadding(10, 10, 10, 10);

        TextView activeLabel = new TextView(this);
        activeLabel.setText("Trạng thái: ");
        activeLabel.setTextSize(14f);
        activeLabel.setTypeface(null, android.graphics.Typeface.BOLD);

        android.widget.Switch activeSwitch = new android.widget.Switch(this);
        activeSwitch.setChecked(product.isActive());
        activeSwitch.setText(product.isActive() ? "Hoạt động" : "Tạm dừng");
        activeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            buttonView.setText(isChecked ? "Hoạt động" : "Tạm dừng");
        });

        activeContainer.addView(activeLabel);
        activeContainer.addView(activeSwitch);

        // Add all fields to layout
        dialogLayout.addView(title);
        dialogLayout.addView(currentInfo);
        dialogLayout.addView(nameContainer);
        dialogLayout.addView(codeContainer);
        dialogLayout.addView(descContainer);
        dialogLayout.addView(specificationsContainer);
        dialogLayout.addView(priceContainer);
        dialogLayout.addView(costContainer);
        dialogLayout.addView(quantityContainer);
        dialogLayout.addView(stockContainer);
        dialogLayout.addView(colorContainer);
        dialogLayout.addView(weightContainer);
        dialogLayout.addView(dimensionsContainer);
        dialogLayout.addView(warrantyContainer);
        dialogLayout.addView(originContainer);
        dialogLayout.addView(releaseDateContainer);
        dialogLayout.addView(qrCodeContainer);
        dialogLayout.addView(categoryIdContainer);
        dialogLayout.addView(categoryContainer);
        dialogLayout.addView(imageUrlContainer);
        dialogLayout.addView(activeContainer);

        TextView note = new TextView(this);
        note.setText("💡 Tất cả field đã được điền sẵn giá trị hiện tại\n" +
                "🔄 Chỉnh sửa những gì bạn muốn thay đổi\n" +
                "⚠️ Chỉ có tên sản phẩm là bắt buộc không được để trống");
        note.setTextSize(12f);
        note.setTextColor(0xFF7F8C8D);
        note.setBackgroundColor(0xFFE8F6F3);
        note.setPadding(15, 15, 15, 15);
        LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        noteParams.setMargins(0, 10, 0, 0);
        note.setLayoutParams(noteParams);
        dialogLayout.addView(note);

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(dialogLayout);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(scrollView)
                .setPositiveButton("💾 CẬP NHẬT", (d, which) -> {
                    try {
                        // Get values from EditTexts
                        String newName = getEditTextFromContainer(nameContainer).getText().toString().trim();
                        String newCode = getEditTextFromContainer(codeContainer).getText().toString().trim();
                        String newDescription = getEditTextFromContainer(descContainer).getText().toString().trim();
                        String newSpecifications = getEditTextFromContainer(specificationsContainer).getText().toString().trim();
                        String newPriceStr = getEditTextFromContainer(priceContainer).getText().toString().trim();
                        String newCostStr = getEditTextFromContainer(costContainer).getText().toString().trim();
                        String newQuantityStr = getEditTextFromContainer(quantityContainer).getText().toString().trim();
                        String newStockStr = getEditTextFromContainer(stockContainer).getText().toString().trim();
                        String newColor = getEditTextFromContainer(colorContainer).getText().toString().trim();
                        String newWeightStr = getEditTextFromContainer(weightContainer).getText().toString().trim();
                        String newDimensions = getEditTextFromContainer(dimensionsContainer).getText().toString().trim();
                        String newWarrantyStr = getEditTextFromContainer(warrantyContainer).getText().toString().trim();
                        String newOrigin = getEditTextFromContainer(originContainer).getText().toString().trim();
                        String newReleaseDate = getEditTextFromContainer(releaseDateContainer).getText().toString().trim();
                        String newQrCode = getEditTextFromContainer(qrCodeContainer).getText().toString().trim();
                        String newCategoryIdStr = getEditTextFromContainer(categoryIdContainer).getText().toString().trim();
                        String newCategory = getEditTextFromContainer(categoryContainer).getText().toString().trim();
                        String newImageUrl = getEditTextFromContainer(imageUrlContainer).getText().toString().trim();

                        // Validate: only name is required
                        if (newName.isEmpty()) {
                            Toast.makeText(this, "❌ Tên sản phẩm không được để trống", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Update product object directly - no complex logic
                        product.setName(newName);
                        product.setProductCode(newCode.isEmpty() ? null : newCode);
                        product.setDescription(newDescription.isEmpty() ? null : newDescription);
                        product.setSpecifications(newSpecifications.isEmpty() ? null : newSpecifications);

                        if (!newPriceStr.isEmpty()) {
                            product.setPrice(Double.parseDouble(newPriceStr));
                        }

                        if (!newCostStr.isEmpty()) {
                            product.setCost(Double.parseDouble(newCostStr));
                        } else {
                            product.setCost(null);
                        }

                        if (!newQuantityStr.isEmpty()) {
                            product.setQuantityInStock(Integer.parseInt(newQuantityStr));
                        }

                        if (!newStockStr.isEmpty()) {
                            product.setStockQuantity(Integer.parseInt(newStockStr));
                        }

                        product.setColor(newColor.isEmpty() ? null : newColor);

                        if (!newWeightStr.isEmpty()) {
                            product.setWeight(Double.parseDouble(newWeightStr));
                        } else {
                            product.setWeight(null);
                        }

                        product.setDimensions(newDimensions.isEmpty() ? null : newDimensions);

                        if (!newWarrantyStr.isEmpty()) {
                            product.setWarrantyPeriod(Integer.parseInt(newWarrantyStr));
                        } else {
                            product.setWarrantyPeriod(null);
                        }

                        product.setOriginCountry(newOrigin.isEmpty() ? null : newOrigin);
                        product.setReleaseDate(newReleaseDate.isEmpty() ? null : newReleaseDate);
                        product.setQrCode(newQrCode.isEmpty() ? null : newQrCode);

                        if (!newCategoryIdStr.isEmpty()) {
                            product.setCategoryId(Integer.parseInt(newCategoryIdStr));
                        } else {
                            product.setCategoryId(null);
                        }

                        product.setCategory(newCategory.isEmpty() ? null : newCategory);
                        product.setImageUrl(newImageUrl.isEmpty() ? null : newImageUrl);
                        product.setActive(activeSwitch.isChecked());

                        // Update timestamp
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        product.setUpdatedAt(sdf.format(new Date()));

                        // Save to database
                        updateProduct(product);

                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "❌ Vui lòng nhập số hợp lệ cho các trường số", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "❌ Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error updating product", e);
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
        // Nếu hint không phải là hint thật mà là current value thì set text
        if (hint != null && !hint.startsWith("Nhập") && !hint.startsWith("YYYY-MM-DD")) {
            editText.setText(hint); // Set current value
            editText.setHint("Chỉnh sửa " + label.toLowerCase());
        } else {
            editText.setHint(hint); // Set real hint
        }
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
    private LinearLayout createInputFieldWithValue(String label, String currentValue) {
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
        // Set current value, nếu null thì set empty string
        editText.setText(currentValue != null ? currentValue : "");
        editText.setHint("Chỉnh sửa " + label.toLowerCase());
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

    // Method mới để tạo number input field với current value
    private LinearLayout createNumberInputFieldWithValue(String label, String currentValue) {
        LinearLayout container = createInputFieldWithValue(label, currentValue);
        EditText editText = (EditText) container.getChildAt(1);
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

    private void updateProduct(Product product) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    productDAO.updateProduct(product);
                    return "✅ Đã cập nhật sản phẩm '" + product.getName() + "' thành công!";
                } catch (Exception e) {
                    Log.e(TAG, "❌ Lỗi khi cập nhật sản phẩm", e);
                    return "❌ Lỗi cập nhật sản phẩm: " + e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String result) {
                Toast.makeText(StaffActivity.this, result, Toast.LENGTH_LONG).show();
                if (result.startsWith("✅") && showingProducts) {
                    loadProducts(); // Làm mới danh sách
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
// Inner class for Product Adapter - WITH LONG CLICK SUPPORT
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

            // Add more details for complete information
            if (product.getSpecifications() != null && !product.getSpecifications().trim().isEmpty()) {
                subtitle += "\n🔧 " + product.getSpecifications().substring(0, Math.min(50, product.getSpecifications().length()));
                if (product.getSpecifications().length() > 50) subtitle += "...";
            }

            if (product.getWarrantyPeriod() != null && product.getWarrantyPeriod() > 0) {
                subtitle += "\n🛡️ Bảo hành: " + product.getWarrantyPeriod() + " tháng";
            }

            holder.subtitle.setText(subtitle);
            holder.subtitle.setTextSize(12f);
            holder.subtitle.setTextColor(0xFF34495E);

            // Setup click listeners for this item
            holder.setupClickListeners(product);
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

            void setupClickListeners(Product product) {
                // Normal click to show detailed product info
                itemView.setOnClickListener(v -> {
                    showProductDetailsDialog(product);
                });

                // Long click to edit/update product
                itemView.setOnLongClickListener(v -> {
                    v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    showUpdateProductDialog(product);
                    return true;
                });
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
    private void performLogout() {
        try {
            // Show loading message
            Toast.makeText(this, "Đang đăng xuất...", Toast.LENGTH_SHORT).show();

            // 1. Logout khỏi Firebase Auth
            FirebaseAuth.getInstance().signOut();
            Log.d(TAG, "Firebase Auth signed out");

            // 2. Logout khỏi Google Sign-In (nếu có)
            try {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
                googleSignInClient.signOut().addOnCompleteListener(this, task -> {
                    Log.d(TAG, "Google Sign-In signed out: " + task.isSuccessful());
                });
            } catch (Exception e) {
                Log.w(TAG, "Google sign out error (can be ignored): " + e.getMessage());
            }

            // 3. Xóa session data trong SharedPreferences
            clearAllSessions();

            // 4. Chuyển về LoginActivity và clear back stack
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            // 5. Hiển thị thông báo logout thành công
            Toast.makeText(this, "✅ Đăng xuất thành công!", Toast.LENGTH_SHORT).show();

            // 6. Finish activity hiện tại
            finish();

            Log.d(TAG, "Logout completed successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error during logout", e);
            Toast.makeText(this, "❌ Lỗi đăng xuất: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Xóa tất cả session data trong SharedPreferences
     */
    private void clearAllSessions() {
        try {
            // Clear staff session
            SharedPreferences staffPrefs = getSharedPreferences("staff_session", MODE_PRIVATE);
            staffPrefs.edit().clear().apply();
            Log.d(TAG, "Staff session cleared");

            // Clear customer session (nếu có)
            SharedPreferences customerPrefs = getSharedPreferences("customer_session", MODE_PRIVATE);
            customerPrefs.edit().clear().apply();
            Log.d(TAG, "Customer session cleared");

            // Clear any other app preferences if needed
            SharedPreferences defaultPrefs = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
            defaultPrefs.edit().clear().apply();
            Log.d(TAG, "Default preferences cleared");

            // Clear any login-related preferences
            SharedPreferences loginPrefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
            loginPrefs.edit().clear().apply();

            Log.d(TAG, "All sessions cleared successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error clearing sessions", e);
        }
    }
    // Add this method to StaffActivity to show detailed product information
    private void showProductDetailsDialog(Product product) {
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(30, 30, 30, 30);

        // Title
        TextView title = new TextView(this);
        title.setText("📦 CHI TIẾT SẢN PHẨM");
        title.setTextSize(18f);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(0xFF2980B9);
        title.setBackgroundColor(0xFFD6EAF8);
        title.setPadding(20, 20, 20, 20);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(0, 0, 0, 20);
        title.setLayoutParams(titleParams);

        // Product details
        TextView details = new TextView(this);
        StringBuilder detailsText = new StringBuilder();

        detailsText.append("🆔 ID: ").append(product.getProductId()).append("\n");
        detailsText.append("📦 Tên: ").append(product.getName()).append("\n");
        detailsText.append("🏷️ Mã: ").append(product.getProductCode() != null ? product.getProductCode() : "N/A").append("\n");
        detailsText.append("💰 Giá bán: ").append(String.format(Locale.getDefault(), "%.2f VND", product.getPrice())).append("\n");

        if (product.getCost() != null) {
            detailsText.append("💵 Giá nhập: ").append(String.format(Locale.getDefault(), "%.2f VND", product.getCost())).append("\n");
        }

        detailsText.append("📊 Tồn kho: ").append(product.getQuantityInStock()).append("\n");
        detailsText.append("🏪 Kho dự trữ: ").append(product.getStockQuantity()).append("\n");

        if (product.getDescription() != null && !product.getDescription().trim().isEmpty()) {
            detailsText.append("📝 Mô tả: ").append(product.getDescription()).append("\n");
        }

        if (product.getSpecifications() != null && !product.getSpecifications().trim().isEmpty()) {
            detailsText.append("🔧 Thông số: ").append(product.getSpecifications()).append("\n");
        }

        if (product.getColor() != null && !product.getColor().trim().isEmpty()) {
            detailsText.append("🎨 Màu sắc: ").append(product.getColor()).append("\n");
        }

        if (product.getWeight() != null) {
            detailsText.append("⚖️ Trọng lượng: ").append(product.getWeight()).append(" kg\n");
        }

        if (product.getDimensions() != null && !product.getDimensions().trim().isEmpty()) {
            detailsText.append("📏 Kích thước: ").append(product.getDimensions()).append("\n");
        }

        if (product.getWarrantyPeriod() != null && product.getWarrantyPeriod() > 0) {
            detailsText.append("🛡️ Bảo hành: ").append(product.getWarrantyPeriod()).append(" tháng\n");
        }

        if (product.getOriginCountry() != null && !product.getOriginCountry().trim().isEmpty()) {
            detailsText.append("🌍 Xuất xứ: ").append(product.getOriginCountry()).append("\n");
        }

        if (product.getReleaseDate() != null && !product.getReleaseDate().trim().isEmpty()) {
            detailsText.append("📅 Ngày phát hành: ").append(product.getReleaseDate()).append("\n");
        }

        if (product.getQrCode() != null && !product.getQrCode().trim().isEmpty()) {
            detailsText.append("📱 Mã QR: ").append(product.getQrCode()).append("\n");
        }

        if (product.getCategoryId() != null) {
            detailsText.append("🆔 ID Danh mục: ").append(product.getCategoryId()).append("\n");
        }

        if (product.getCategory() != null && !product.getCategory().trim().isEmpty()) {
            detailsText.append("📂 Danh mục: ").append(product.getCategory()).append("\n");
        }

        detailsText.append("🔄 Trạng thái: ").append(product.isActive() ? "Hoạt động" : "Tạm dừng").append("\n");

        if (product.getCreatedAt() != null) {
            detailsText.append("📅 Tạo lúc: ").append(product.getCreatedAt()).append("\n");
        }

        if (product.getUpdatedAt() != null) {
            detailsText.append("🔄 Cập nhật: ").append(product.getUpdatedAt());
        }

        details.setText(detailsText.toString());
        details.setTextSize(12f);
        details.setTextColor(0xFF2C3E50);
        details.setBackgroundColor(0xFFECF0F1);
        details.setPadding(15, 15, 15, 15);

        dialogLayout.addView(title);
        dialogLayout.addView(details);

        // Action buttons
        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setPadding(0, 20, 0, 0);

        Button btnEdit = new Button(this);
        btnEdit.setText("✏️ CHỈNH SỬA");
        btnEdit.setTextColor(0xFFFFFFFF);
        btnEdit.setBackgroundColor(0xFF3498DB);
        LinearLayout.LayoutParams editParams = new LinearLayout.LayoutParams(0, 100, 1f);
        editParams.setMargins(0, 0, 10, 0);
        btnEdit.setLayoutParams(editParams);

        Button btnClose = new Button(this);
        btnClose.setText("❌ ĐÓNG");
        btnClose.setTextColor(0xFFFFFFFF);
        btnClose.setBackgroundColor(0xFF95A5A6);
        LinearLayout.LayoutParams closeParams = new LinearLayout.LayoutParams(0, 100, 1f);
        closeParams.setMargins(10, 0, 0, 0);
        btnClose.setLayoutParams(closeParams);

        buttonLayout.addView(btnEdit);
        buttonLayout.addView(btnClose);
        dialogLayout.addView(buttonLayout);

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(dialogLayout);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(scrollView)
                .create();

        btnEdit.setOnClickListener(v -> {
            dialog.dismiss();
            showUpdateProductDialog(product);
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
} // KẾT THÚC StaffActivity
