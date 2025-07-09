package com.example.prm392_finalproject.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

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
        fabCreateOrder.setOnClickListener(v -> {
            // Mở giỏ hàng (CartActivity)
            Intent intent = new Intent(this, CartActivity.class);
            startActivity(intent);
        });
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);
        rvProducts = findViewById(R.id.rvProducts);
        welcomeText = findViewById(R.id.tvWelcome);
        btnSignOut = findViewById(R.id.btnSignOut);
        btnSignOut.setOnClickListener(v -> signOut());
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Tech Store");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupNavigationDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(productList, product -> {
            // Xem chi tiết sản phẩm (nếu muốn)
        }, product -> {
            // Thêm vào giỏ hàng
            CartManager.getInstance().addToCart(product);
            Snackbar.make(rvProducts, "Đã thêm vào giỏ hàng: " + product.getName(), Snackbar.LENGTH_SHORT).show();
        });
        
        rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        rvProducts.setAdapter(productAdapter);
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
                    productList.clear();
                    productList.addAll(products);
                    productAdapter.notifyDataSetChanged();
                    
                    if (products.isEmpty()) {
                        Toast.makeText(UserDashboardActivity.this, 
                            "No products available", Toast.LENGTH_SHORT).show();
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
        if (user != null) {
            welcomeText.setText("Welcome, " + user.getFullName() + "!");
            
            // Update navigation header
            View headerView = navigationView.getHeaderView(0);
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
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.nav_my_orders) {
            Intent intent = new Intent(UserDashboardActivity.this, CustomerOrderActivity.class);
            if (currentUser != null) {
                intent.putExtra("currentUser", currentUser);
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