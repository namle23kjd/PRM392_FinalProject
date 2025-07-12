package com.example.prm392_finalproject.views;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.app.Activity;

import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.controllers.UserRepository;
import com.example.prm392_finalproject.dao.ProductDAO;
import com.example.prm392_finalproject.models.Product;
import com.example.prm392_finalproject.models.User;
import com.example.prm392_finalproject.utils.CartManager;
import com.example.prm392_finalproject.views.adapters.ProductAdapter;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import com.example.prm392_finalproject.views.CustomerProductDetailActivity;
import java.util.ArrayList;
import java.util.List;

public class UserDashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "UserDashboardActivity";
    
    private UserRepository userRepository;
    private ProductDAO productDAO;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    
    // UI Components
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private RecyclerView rvProducts;
    private ProductAdapter productAdapter;
    private TextView welcomeText;
    private Button btnSignOut;
    
    private User currentUser;
    private List<Product> productList = new ArrayList<>();
    private ActivityResultLauncher<Intent> profileLauncher;
    // Thêm những biến này
    private SearchView searchViewProducts;
    private List<Product> allProducts = new ArrayList<>();
    private List<Product> filteredProducts = new ArrayList<>();

   // Filter components
   private Spinner spinnerCategory, spinnerPriceRange, spinnerColor, spinnerOrigin, spinnerWarranty;
    private CheckBox cbInStock;
    private Button btnMoreFilters, btnClearFilters, btnApplyFilters;
    private LinearLayout layoutAdvancedFilters;

    // Filter data
    private List<String> categories = new ArrayList<>();
    private List<String> colors = new ArrayList<>();
    private List<String> origins = new ArrayList<>();

    // Current filter state
    private String selectedCategory = "All";
    private String selectedPriceRange = "All";
    private String selectedColor = "All";
    private String selectedOrigin = "All";
    private String selectedWarranty = "All";
    private boolean filterInStockOnly = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        try {
            Log.d(TAG, "UserDashboardActivity onCreate started");

            // Initialize dependencies
            userRepository = new UserRepository();
            productDAO = new ProductDAO();
            mAuth = FirebaseAuth.getInstance();

            // Configure Google Sign-In
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

            // Initialize views
            initViews();
            
            // Setup toolbar and navigation drawer
            setupToolbar();
            setupNavigationDrawer();
            
            // Setup RecyclerView
            setupRecyclerView();
            // Setup filters
            setupFilters();
            // Search
            setupSearchView();
            // Load current user data
            loadCurrentUser();
            
            // Load products
            loadProducts();


            profileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        User updatedUser = (User) result.getData().getSerializableExtra("updatedUser");
                        if (updatedUser != null) {
                            currentUser = updatedUser;
                            updateUI(currentUser);
                            Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            );

            // Thêm sự kiện cho nút tạo đơn hàng mới
            FloatingActionButton fabCreateOrder = findViewById(R.id.fabCreateOrder);
            if (fabCreateOrder != null) {
                fabCreateOrder.setOnClickListener(v -> {
                    // Mở giỏ hàng (CartActivity)
                    Intent intent = new Intent(this, CartActivity.class);
                    startActivity(intent);
                });
            } else {
                Log.w(TAG, "fabCreateOrder is null, skipping click listener");
            }

            Log.d(TAG, "UserDashboardActivity onCreate completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khởi tạo: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        try {
            Log.d(TAG, "Initializing views...");
            
            drawerLayout = findViewById(R.id.drawerLayout);
            if (drawerLayout == null) {
                Log.e(TAG, "drawerLayout is null!");
                throw new RuntimeException("drawerLayout not found");
            }
            
            navigationView = findViewById(R.id.navigationView);
            if (navigationView == null) {
                Log.e(TAG, "navigationView is null!");
                throw new RuntimeException("navigationView not found");
            }
            
            toolbar = findViewById(R.id.toolbar);
            if (toolbar == null) {
                Log.e(TAG, "toolbar is null!");
                throw new RuntimeException("toolbar not found");
            }
            
            rvProducts = findViewById(R.id.rvProducts);
            if (rvProducts == null) {
                Log.e(TAG, "rvProducts is null!");
                throw new RuntimeException("rvProducts not found");
            }
            
            welcomeText = findViewById(R.id.tvWelcome);
            if (welcomeText == null) {
                Log.e(TAG, "tvWelcome is null!");
                throw new RuntimeException("tvWelcome not found");
            }
            
            btnSignOut = findViewById(R.id.btnSignOut);
            if (btnSignOut == null) {
                Log.e(TAG, "btnSignOut is null!");
                throw new RuntimeException("btnSignOut not found");
            }
            
            btnSignOut.setOnClickListener(v -> signOut());
            
            Log.d(TAG, "All views initialized successfully");
            // Thêm đoạn này vào cuối method initViews()
            searchViewProducts = findViewById(R.id.searchViewProducts);
            if (searchViewProducts == null) {
                Log.e(TAG, "searchViewProducts is null!");
                throw new RuntimeException("searchViewProducts not found");
            }
            Log.d(TAG, "SearchView initialized successfully");
            // Initialize filter components
            spinnerCategory = findViewById(R.id.spinnerCategory);
            spinnerPriceRange = findViewById(R.id.spinnerPriceRange);
            spinnerColor = findViewById(R.id.spinnerColor);
            spinnerOrigin = findViewById(R.id.spinnerOrigin);
            spinnerWarranty = findViewById(R.id.spinnerWarranty);
            cbInStock = findViewById(R.id.cbInStock);
            btnMoreFilters = findViewById(R.id.btnMoreFilters);
            btnClearFilters = findViewById(R.id.btnClearFilters);
            btnApplyFilters = findViewById(R.id.btnApplyFilters);
            layoutAdvancedFilters = findViewById(R.id.layoutAdvancedFilters);

            Log.d(TAG, "Filter components initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khởi tạo giao diện: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupToolbar() {
        try {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Tech Store");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            Log.d(TAG, "Toolbar setup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up toolbar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupNavigationDrawer() {
        try {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();

            navigationView.setNavigationItemSelectedListener(this);
            Log.d(TAG, "Navigation drawer setup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up navigation drawer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupRecyclerView() {
        try {
            productAdapter = new ProductAdapter(productList, product -> {
                // Xem chi tiết sản phẩm
                Intent intent = new Intent(UserDashboardActivity.this, CustomerProductDetailActivity.class);
                intent.putExtra("product", product);
                startActivity(intent);

            }, product -> {
                // Thêm vào giỏ hàng
                CartManager.getInstance().addToCart(product);
                Snackbar.make(rvProducts, "Đã thêm vào giỏ hàng: " + product.getName(), Snackbar.LENGTH_SHORT).show();
            });
            
            rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
            rvProducts.setAdapter(productAdapter);
            Log.d(TAG, "RecyclerView setup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerView: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void setupSearchView() {
        try {
            searchViewProducts.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    Log.d(TAG, "Search submitted: " + query);
                    performSearch(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    Log.d(TAG, "Search text changed: " + newText);
                    performSearch(newText);
                    return true;
                }
            });

            searchViewProducts.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    Log.d(TAG, "Search view closed");
                    updateProductAdapter(allProducts);
                    return false;
                }
            });

            Log.d(TAG, "SearchView setup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error in setupSearchView: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void performSearch(String query) {
        try {
            if (query == null || query.trim().isEmpty()) {
                Log.d(TAG, "Empty query, showing all products");
                updateProductAdapter(allProducts);
                return;
            }

            Log.d(TAG, "Performing search for: " + query);

            new Thread(() -> {
                try {
                    List<Product> searchResults = productDAO.searchProducts(query.trim());
                    Log.d(TAG, "Search completed. Found: " +
                            (searchResults != null ? searchResults.size() : "null") + " products");

                    runOnUiThread(() -> {
                        if (searchResults != null) {
                            filteredProducts = searchResults;
                            updateProductAdapter(filteredProducts);

                            if (searchResults.isEmpty()) {
                                Toast.makeText(UserDashboardActivity.this,
                                        "No products found for '" + query + "'", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(UserDashboardActivity.this,
                                        "Found " + searchResults.size() + " products", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(UserDashboardActivity.this,
                                    "Search error occurred", Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (Exception e) {
                    Log.e(TAG, "Error in search: " + e.getMessage());
                    e.printStackTrace();

                    runOnUiThread(() -> {
                        Toast.makeText(UserDashboardActivity.this,
                                "Search error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }).start();

        } catch (Exception e) {
            Log.e(TAG, "ERROR in performSearch: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void updateProductAdapter(List<Product> products) {
        try {
            productList.clear();
            if (products != null) {
                productList.addAll(products);
            }
            productAdapter.notifyDataSetChanged();
            Log.d(TAG, "Product adapter updated with " + productList.size() + " products");
        } catch (Exception e) {
            Log.e(TAG, "Error updating product adapter: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void loadCurrentUser() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            // Load user data from MySQL database
            new Thread(() -> {
                User user = userRepository.getUserByEmail(firebaseUser.getEmail());
                runOnUiThread(() -> {
                    if (user != null) {
                        currentUser = user;
                        updateUI(user);
                    } else {
                        Log.e(TAG, "User not found in database");
                        Toast.makeText(UserDashboardActivity.this, 
                            "Error loading user data", Toast.LENGTH_SHORT).show();
                        signOut();
                    }
                });
            }).start();
        } else {
            // No user logged in, redirect to login
            redirectToLogin();
        }
    }

    private void loadProducts() {
        new Thread(() -> {
            try {
                List<Product> products = productDAO.getAllProducts();
                runOnUiThread(() -> {
                    // Lưu danh sách tất cả sản phẩm
                    allProducts.clear();
                    if (products != null) {
                        allProducts.addAll(products);
                        filteredProducts = new ArrayList<>(products);
                    }

                    updateProductAdapter(allProducts);
                    populateFilterOptions();
                    if (products == null || products.isEmpty()) {
                        Toast.makeText(UserDashboardActivity.this,
                                "No products available", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d(TAG, "Loaded " + products.size() + " products successfully");
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading products: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(UserDashboardActivity.this,
                            "Error loading products: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void updateUI(User user) {
        try {
            if (user != null) {
                welcomeText.setText("Welcome, " + user.getFullName() + "!");
                
                // Update navigation header
                View headerView = navigationView.getHeaderView(0);
                if (headerView != null) {
                    TextView navUserName = headerView.findViewById(R.id.navUserName);
                    TextView navUserEmail = headerView.findViewById(R.id.navUserEmail);
                    TextView navUserGender = headerView.findViewById(R.id.navUserGender);
                    
                    if (navUserName != null) {
                        navUserName.setText(user.getFullName());
                    }
                    if (navUserEmail != null) {
                        navUserEmail.setText(user.getEmail());
                    }
                    if (navUserGender != null) {
                        navUserGender.setText(user.getGender());
                    }
                } else {
                    Log.w(TAG, "Navigation header view is null");
                }
                
                Log.d(TAG, "UI updated successfully for user: " + user.getFullName());
            } else {
                Log.w(TAG, "User is null, cannot update UI");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating UI: " + e.getMessage());
            e.printStackTrace();
        }
    }
    //Filter method
    private void setupFilters() {
        try {
            // Setup price range spinner
            setupPriceRangeFilter();

            // Setup warranty filter
            setupWarrantyFilter();

            // Setup More Filters toggle
            btnMoreFilters.setOnClickListener(v -> toggleAdvancedFilters());

            // Setup filter buttons
            btnClearFilters.setOnClickListener(v -> clearAllFilters());
            btnApplyFilters.setOnClickListener(v -> applyFilters());

            // Setup checkbox
            cbInStock.setOnCheckedChangeListener((buttonView, isChecked) -> {
                filterInStockOnly = isChecked;
                applyFilters();
            });

            Log.d(TAG, "Filters setup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up filters: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupPriceRangeFilter() {
        String[] priceRanges = {"All", "Under $50", "$50 - $500", "$500 - $1000", "$1000 - $1500", "Over $1500"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, priceRanges);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriceRange.setAdapter(adapter);

        spinnerPriceRange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPriceRange = priceRanges[position];
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupWarrantyFilter() {
        String[] warranties = {"All", "6 months", "12 months", "24+ months"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, warranties);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWarranty.setAdapter(adapter);

        spinnerWarranty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedWarranty = warranties[position];
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    private void populateFilterOptions() {
        new Thread(() -> {
            try {
                // Get unique categories
                categories.clear();
                categories.add("All");
                for (Product product : allProducts) {
                    if (product.getCategory() != null && !categories.contains(product.getCategory())) {
                        categories.add(product.getCategory());
                    }
                }

                // Get unique colors
                colors.clear();
                colors.add("All");
                for (Product product : allProducts) {
                    if (product.getColor() != null && !colors.contains(product.getColor())) {
                        colors.add(product.getColor());
                    }
                }

                // Get unique origins
                origins.clear();
                origins.add("All");
                for (Product product : allProducts) {
                    if (product.getOriginCountry() != null && !origins.contains(product.getOriginCountry())) {
                        origins.add(product.getOriginCountry());
                    }
                }

                runOnUiThread(() -> {
                    setupDynamicFilters();
                });

            } catch (Exception e) {
                Log.e(TAG, "Error populating filter options: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private void setupDynamicFilters() {
        // Setup category filter
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = categories.get(position);
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Setup color filter
        ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, colors);
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerColor.setAdapter(colorAdapter);
        spinnerColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedColor = colors.get(position);
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Setup origin filter
        ArrayAdapter<String> originAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, origins);
        originAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOrigin.setAdapter(originAdapter);
        spinnerOrigin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedOrigin = origins.get(position);
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    private void toggleAdvancedFilters() {
        if (layoutAdvancedFilters.getVisibility() == View.GONE) {
            layoutAdvancedFilters.setVisibility(View.VISIBLE);
            btnMoreFilters.setText("Less");
        } else {
            layoutAdvancedFilters.setVisibility(View.GONE);
            btnMoreFilters.setText("More");
        }
    }
    private void clearAllFilters() {
        try {
            // Reset filter states
            selectedCategory = "All";
            selectedPriceRange = "All";
            selectedColor = "All";
            selectedOrigin = "All";
            selectedWarranty = "All";
            filterInStockOnly = false;

            // Reset UI components
            spinnerCategory.setSelection(0);
            spinnerPriceRange.setSelection(0);
            spinnerColor.setSelection(0);
            spinnerOrigin.setSelection(0);
            spinnerWarranty.setSelection(0);
            cbInStock.setChecked(false);

            // Apply cleared filters
            updateProductAdapter(allProducts);

            Toast.makeText(this, "All filters cleared", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "All filters cleared");

        } catch (Exception e) {
            Log.e(TAG, "Error clearing filters: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void applyFilters() {
        try {
            List<Product> filtered = new ArrayList<>();

            for (Product product : allProducts) {
                // Category filter
                if (!selectedCategory.equals("All") &&
                        (product.getCategory() == null || !product.getCategory().equals(selectedCategory))) {
                    continue;
                }

                // Price range filter
                if (!selectedPriceRange.equals("All")) {
                    double price = product.getPrice();
                    boolean priceMatch = false;

                    switch (selectedPriceRange) {
                        case "Under $50":
                            priceMatch = price < 50;
                            break;
                        case "$50 - $500":
                            priceMatch = price >= 50 && price <= 500;
                            break;
                        case "$500 - $1000":
                            priceMatch = price >= 500 && price <= 1000;
                            break;
                        case "$1000 - $1500":
                            priceMatch = price >= 1000 && price <= 1500;
                            break;
                        case "Over $1500":
                            priceMatch = price > 1500;
                            break;
                    }

                    if (!priceMatch) {
                        continue;
                    }
                }

                // In stock filter
                if (filterInStockOnly && product.getQuantityInStock() <= 0) {
                    continue;
                }

                // Color filter
                if (!selectedColor.equals("All") &&
                        (product.getColor() == null || !product.getColor().equals(selectedColor))) {
                    continue;
                }

                // Origin filter
                if (!selectedOrigin.equals("All") &&
                        (product.getOriginCountry() == null || !product.getOriginCountry().equals(selectedOrigin))) {
                    continue;
                }

                // Warranty filter
                if (!selectedWarranty.equals("All")) {
                    Integer warranty = product.getWarrantyPeriod();
                    boolean warrantyMatch = false;

                    switch (selectedWarranty) {
                        case "6 months":
                            warrantyMatch = (warranty != null && warranty == 6);
                            break;
                        case "12 months":
                            warrantyMatch = (warranty != null && warranty == 12);
                            break;
                        case "24+ months":
                            warrantyMatch = (warranty != null && warranty >= 24);
                            break;
                    }

                    if (!warrantyMatch) {
                        continue;
                    }
                }

                // If all filters pass, add to filtered list
                filtered.add(product);
            }

            filteredProducts = filtered;
            updateProductAdapter(filteredProducts);

            Log.d(TAG, "Filters applied. Showing " + filtered.size() + " products");

            if (filtered.isEmpty()) {
                Toast.makeText(this, "No products match the selected filters", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error applying filters: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error applying filters", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.nav_my_orders) {
            Intent intent = new Intent(UserDashboardActivity.this, CustomerOrderActivity.class);
            if (currentUser != null) {
                // Truyền thông tin customer theo đúng format mà CustomerOrderActivity mong đợi
                intent.putExtra("customer_id", currentUser.getCustomerId());
                intent.putExtra("customer_name", currentUser.getFullName());
                intent.putExtra("customer_address", currentUser.getAddress());
                
                // Lưu vào SharedPreferences để CustomerOrderActivity có thể đọc
                SharedPreferences prefs = getSharedPreferences("customer_session", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("customer_id", currentUser.getCustomerId());
                editor.putString("customer_name", currentUser.getFullName());
                editor.putString("customer_address", currentUser.getAddress());
                editor.apply();
                
                // Log để debug
                Log.d(TAG, "Starting CustomerOrderActivity with customer_id: " + currentUser.getCustomerId());
                Log.d(TAG, "Customer name: " + currentUser.getFullName());
                Log.d(TAG, "Customer address: " + currentUser.getAddress());
            } else {
                Log.e(TAG, "currentUser is null when trying to open orders");
                Toast.makeText(this, "Lỗi: Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                drawerLayout.closeDrawer(GravityCompat.START);
                return false;
            }
            startActivity(intent);
        } else if (id == R.id.nav_payment_history) {
            Intent intent = new Intent(UserDashboardActivity.this, PaymentHistoryActivity.class);
            if (currentUser != null) {
                intent.putExtra("currentUser", currentUser);
            }
            startActivity(intent);
        } else if (id == R.id.nav_shipping) {
            Intent intent = new Intent(UserDashboardActivity.this, ShippingActivity.class);
            if (currentUser != null) {
                intent.putExtra("currentUser", currentUser);
            }
            startActivity(intent);
        } else if (id == R.id.nav_profile) {
            Intent intent = new Intent(UserDashboardActivity.this, UserProfileActivity.class);
            if (currentUser != null) {
                intent.putExtra("currentUser", currentUser);
            }
            profileLauncher.launch(intent);
        } else if (id == R.id.nav_sign_out) {
            signOut();
        }
        
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void signOut() {
        // Sign out from Firebase
        mAuth.signOut();

        // Sign out from Google
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Toast.makeText(UserDashboardActivity.this, 
                "You have been signed out.", Toast.LENGTH_SHORT).show();
            redirectToLogin();
        });
    }

    private void redirectToLogin() {
        Intent intent = new Intent(UserDashboardActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check if user is still logged in
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            redirectToLogin();
        }
    }
} 