package com.example.prm392_finalproject.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.controllers.UserRepository;
import com.example.prm392_finalproject.dao.OrderDAO;
import com.example.prm392_finalproject.dao.ProductDAO;
import com.example.prm392_finalproject.models.zalopay.Order;
import com.example.prm392_finalproject.models.User;
import com.example.prm392_finalproject.models.Product;
import com.example.prm392_finalproject.views.adapters.StaffOrderAdapter;
import com.example.prm392_finalproject.views.adapters.ProductAdapter;
import com.example.prm392_finalproject.views.OrderDetailActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StaffActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, StaffOrderAdapter.OnOrderClickListener {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TextView tvWelcomeStaff;
    private RecyclerView rvRecentOrders, rvRecentProducts;
    private StaffOrderAdapter orderAdapter;
    private ProductAdapter productAdapter;
    
    private UserRepository userRepository;
    private ProductDAO productDAO;
    private OrderDAO orderDAO;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff);

        // Initialize dependencies
        userRepository = new UserRepository();
        productDAO = new ProductDAO();
        orderDAO = new OrderDAO();
        mAuth = FirebaseAuth.getInstance();
        
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Init views
        initViews();
        setupToolbar();
        setupNavigationDrawer();
        loadCurrentUser();
        loadRecentOrders();
        loadRecentProducts();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);
        tvWelcomeStaff = findViewById(R.id.tvWelcomeStaff);
        rvRecentOrders = findViewById(R.id.rvRecentOrders);
        rvRecentOrders.setLayoutManager(new LinearLayoutManager(this));
        
        rvRecentProducts = findViewById(R.id.rvRecentProducts);
        rvRecentProducts.setLayoutManager(new LinearLayoutManager(this));
        
        // Setup click listeners for dashboard cards
        setupDashboardCards();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Staff Dashboard");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupNavigationDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        
        // Enable toolbar navigation
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_business);
        }
    }

    private void loadCurrentUser() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            new Thread(() -> {
                try {
                    User user = userRepository.getUserByEmail(firebaseUser.getEmail());
                    runOnUiThread(() -> {
                        if (user != null) {
                            currentUser = user;
                            tvWelcomeStaff.setText("Welcome, " + user.getFullName() + " (Staff)");
                        } else {
                            Toast.makeText(StaffActivity.this, "User not found in database", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(StaffActivity.this, "Error loading user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        }
    }

    private void setupDashboardCards() {
        // Orders card click listener
        View ordersCard = findViewById(R.id.ordersCard);
        if (ordersCard != null) {
            ordersCard.setOnClickListener(v -> openOrderManagement());
        }
        
        // Shipping card click listener
        View shippingCard = findViewById(R.id.shippingCard);
        if (shippingCard != null) {
            shippingCard.setOnClickListener(v -> openShippingManagement());
        }
        
        // Products card click listener
        View productsCard = findViewById(R.id.productsCard);
        if (productsCard != null) {
            productsCard.setOnClickListener(v -> openProductList());
        }
        
        // View All orders click listener
        View viewAllOrders = findViewById(R.id.viewAllOrders);
        if (viewAllOrders != null) {
            viewAllOrders.setOnClickListener(v -> openOrderManagement());
        }
        
        // View All products click listener
        View viewAllProducts = findViewById(R.id.viewAllProducts);
        if (viewAllProducts != null) {
            viewAllProducts.setOnClickListener(v -> openProductList());
        }
    }

    private void loadRecentOrders() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                List<Order> orders = orderDAO.getAllOrders();
                runOnUiThread(() -> {
                    if (orders != null && !orders.isEmpty()) {
                        // Chỉ hiển thị 5 đơn hàng gần nhất
                        List<Order> recentOrders = orders.size() > 5 ? orders.subList(0, 5) : orders;
                        orderAdapter = new StaffOrderAdapter(recentOrders, StaffActivity.this);
                        rvRecentOrders.setAdapter(orderAdapter);
                    } else {
                        // Hiển thị thông báo không có đơn hàng
                        Toast.makeText(StaffActivity.this, "No orders found", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(StaffActivity.this, "Error loading orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.nav_dashboard) {
            // Đã ở dashboard
            Toast.makeText(this, "You are already on Dashboard", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_orders) {
            openOrderManagement();
        } else if (id == R.id.nav_products) {
            openProductList();
        } else if (id == R.id.nav_shipping) {
            openShippingManagement();
        } else if (id == R.id.nav_payment_history) {
            openPaymentHistory();
        } else if (id == R.id.nav_profile) {
            openUserProfile();
        } else if (id == R.id.nav_sign_out) {
            signOut();
        }
        
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void openOrderManagement() {
        Intent intent = new Intent(this, OrderManagementActivity.class);
        startActivity(intent);
    }

    private void openProductList() {
        Intent intent = new Intent(this, StaffProductListActivity.class);
        startActivity(intent);
    }

    private void openShippingManagement() {
        Intent intent = new Intent(this, ShippingActivity.class);
        startActivity(intent);
    }

    private void openPaymentHistory() {
        Intent intent = new Intent(this, PaymentHistoryActivity.class);
        startActivity(intent);
    }

    private void openUserProfile() {
        Intent intent = new Intent(this, UserProfileActivity.class);
        if (currentUser != null) {
            intent.putExtra("currentUser", currentUser);
        }
        startActivity(intent);
    }

    private void signOut() {
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Toast.makeText(StaffActivity.this, "You have been signed out.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(StaffActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onOrderClick(Order order) {
        // Open order detail activity
        Intent intent = new Intent(this, OrderDetailActivity.class);
        intent.putExtra("order_id", order.getOrderId());
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void loadRecentProducts() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                List<Product> products = productDAO.getAllProducts();
                runOnUiThread(() -> {
                    if (products != null && !products.isEmpty()) {
                        // Chỉ hiển thị 3 sản phẩm gần nhất
                        List<Product> recentProducts = products.size() > 3 ? products.subList(0, 3) : products;
                        productAdapter = new ProductAdapter(recentProducts, false); // false = read-only mode
                        rvRecentProducts.setAdapter(productAdapter);
                    } else {
                        // Hiển thị thông báo không có sản phẩm
                        TextView tvNoProducts = findViewById(R.id.tvNoProducts);
                        if (tvNoProducts != null) {
                            tvNoProducts.setVisibility(View.VISIBLE);
                        }
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(StaffActivity.this, "Error loading products: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
} 