package com.example.prm392_finalproject.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.controllers.UserRepository;
import com.example.prm392_finalproject.dao.ProductDAO;
import com.example.prm392_finalproject.dao.SupplierDAO;
import com.example.prm392_finalproject.dao.OrderDAO;
import com.example.prm392_finalproject.dao.PaymentDAO;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

// MPAndroidChart imports
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

public class AdminActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private BarChart barChartMonth;
    private PieChart pieChartUserRole, pieChartProductCategory;
    private TextView tvTotalUsers, tvTotalProducts, tvSupplierCount;
    private UserRepository userRepository;
    private ProductDAO productDAO;
    private SupplierDAO supplierDAO;
    private OrderDAO orderDAO;
    private PaymentDAO paymentDAO;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Initialize dependencies
        userRepository = new UserRepository();
        productDAO = new ProductDAO();
        supplierDAO = new SupplierDAO();
        orderDAO = new OrderDAO();
        paymentDAO = new PaymentDAO();
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
        setupBarChartMonth();
        setupPieChartUserRole();
        setupPieChartProductCategory();
        loadSupplierCount();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);
        barChartMonth = findViewById(R.id.barChartMonth);
        pieChartUserRole = findViewById(R.id.pieChartUserRole);
        pieChartProductCategory = findViewById(R.id.pieChartProductCategory);
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvTotalProducts = findViewById(R.id.tvTotalProducts);
        tvSupplierCount = findViewById(R.id.tvSupplierCount);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Admin Dashboard");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupNavigationDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setupBarChartMonth() {
        barChartMonth.getDescription().setEnabled(false);
        barChartMonth.setDrawGridBackground(false);
        barChartMonth.setDrawBarShadow(false);
        barChartMonth.setHighlightFullBarEnabled(false);
        XAxis xAxis = barChartMonth.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        barChartMonth.getAxisLeft().setDrawGridLines(true);
        barChartMonth.getAxisRight().setEnabled(false);
        barChartMonth.getLegend().setEnabled(true);
        barChartMonth.getLegend().setTextSize(12f);
        barChartMonth.animateY(1000);
        loadBarChartMonthData();
    }

    private void loadBarChartMonthData() {
        new Thread(() -> {
            int year = Calendar.getInstance().get(Calendar.YEAR);
            Map<Integer, Integer> orderMap = orderDAO.getOrderCountByMonth(year);
            Map<Integer, Integer> paymentMap = paymentDAO.getPaymentCountByMonth(year);
            ArrayList<BarEntry> orderEntries = new ArrayList<>();
            ArrayList<BarEntry> paymentEntries = new ArrayList<>();
            String[] months = new String[12];
            for (int i = 0; i < 12; i++) {
                int month = i + 1;
                months[i] = String.valueOf(month);
                orderEntries.add(new BarEntry(i, orderMap.getOrDefault(month, 0)));
                paymentEntries.add(new BarEntry(i, paymentMap.getOrDefault(month, 0)));
            }
            runOnUiThread(() -> {
                BarDataSet orderSet = new BarDataSet(orderEntries, "Orders");
                orderSet.setColor(ColorTemplate.MATERIAL_COLORS[0]);
                BarDataSet paymentSet = new BarDataSet(paymentEntries, "Payments");
                paymentSet.setColor(ColorTemplate.MATERIAL_COLORS[1]);
                BarData barData = new BarData(orderSet, paymentSet);
                barData.setBarWidth(0.4f);
                barChartMonth.setData(barData);
                barChartMonth.getXAxis().setValueFormatter(new IndexAxisValueFormatter(months));
                if (!orderEntries.isEmpty() || !paymentEntries.isEmpty()) {
                    barChartMonth.groupBars(0, 0.2f, 0.05f);
                }
                barChartMonth.invalidate();
            });
        }).start();
    }

    private void setupPieChartUserRole() {
        pieChartUserRole.getDescription().setEnabled(false);
        pieChartUserRole.setUsePercentValues(false);
        pieChartUserRole.setDrawHoleEnabled(true);
        pieChartUserRole.setHoleColor(android.graphics.Color.WHITE);
        pieChartUserRole.setTransparentCircleAlpha(110);
        pieChartUserRole.setEntryLabelColor(android.graphics.Color.BLACK);
        pieChartUserRole.setEntryLabelTextSize(12f);
        pieChartUserRole.getLegend().setEnabled(true);
        pieChartUserRole.getLegend().setTextSize(12f);
        pieChartUserRole.animateY(1000);
        loadPieChartUserRoleData();
    }

    private void loadPieChartUserRoleData() {
        new Thread(() -> {
            Map<String, Integer> roleMap = userRepository.getUserCountByRole();
            int total = 0;
            ArrayList<PieEntry> entries = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : roleMap.entrySet()) {
                entries.add(new PieEntry(entry.getValue(), entry.getKey()));
                total += entry.getValue();
            }
            final int finalTotal = total;
            runOnUiThread(() -> {
                PieDataSet dataSet = new PieDataSet(entries, "User Roles");
                dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                dataSet.setValueTextSize(12f);
                dataSet.setValueTextColor(android.graphics.Color.BLACK);
                PieData pieData = new PieData(dataSet);
                pieChartUserRole.setData(pieData);
                pieChartUserRole.invalidate();
                tvTotalUsers.setText("Total Users: " + finalTotal);
            });
        }).start();
    }

    private void setupPieChartProductCategory() {
        pieChartProductCategory.getDescription().setEnabled(false);
        pieChartProductCategory.setUsePercentValues(false);
        pieChartProductCategory.setDrawHoleEnabled(true);
        pieChartProductCategory.setHoleColor(android.graphics.Color.WHITE);
        pieChartProductCategory.setTransparentCircleAlpha(110);
        pieChartProductCategory.setEntryLabelColor(android.graphics.Color.BLACK);
        pieChartProductCategory.setEntryLabelTextSize(12f);
        pieChartProductCategory.getLegend().setEnabled(true);
        pieChartProductCategory.getLegend().setTextSize(12f);
        pieChartProductCategory.animateY(1000);
        loadPieChartProductCategoryData();
    }

    private void loadPieChartProductCategoryData() {
        new Thread(() -> {
            Map<String, Integer> catMap = productDAO.getProductCountByCategory();
            int total = 0;
            ArrayList<PieEntry> entries = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : catMap.entrySet()) {
                entries.add(new PieEntry(entry.getValue(), entry.getKey()));
                total += entry.getValue();
            }
            final int finalTotal = total;
            runOnUiThread(() -> {
                PieDataSet dataSet = new PieDataSet(entries, "Categories");
                dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                dataSet.setValueTextSize(12f);
                dataSet.setValueTextColor(android.graphics.Color.BLACK);
                PieData pieData = new PieData(dataSet);
                pieChartProductCategory.setData(pieData);
                pieChartProductCategory.invalidate();
                tvTotalProducts.setText("Total Products: " + finalTotal);
            });
        }).start();
    }

    private void loadSupplierCount() {
        new Thread(() -> {
            int count = supplierDAO.getSupplierCount();
            runOnUiThread(() -> tvSupplierCount.setText("Suppliers: " + count));
        }).start();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_dashboard) {
            // Đã ở dashboard
        } else if (id == R.id.nav_users) {
            openUserManagement();
        } else if (id == R.id.nav_products) {
            openProductManagement();
        } else if (id == R.id.nav_orders) {
            openOrderManagement();
        } else if (id == R.id.nav_suppliers) {
            openSupplierManagement();
        } else if (id == R.id.nav_categories) {
            openCategoryManagement();
        } else if (id == R.id.nav_shipping) {
            openShippingManagement();
        } else if (id == R.id.nav_payment_history) {
            openPaymentHistory();
        } else if (id == R.id.nav_sign_out) {
            signOut();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void openUserManagement() {
        Intent intent = new Intent(this, UserListActivity.class);
        startActivity(intent);
    }
    private void openProductManagement() {
        Intent intent = new Intent(this, ProductListActivity.class);
        startActivity(intent);
    }
    private void openOrderManagement() {
        Intent intent = new Intent(this, OrderManagementActivity.class);
        startActivity(intent);
    }
    private void openSupplierManagement() {
        Intent intent = new Intent(this, SupplierActivity.class);
        startActivity(intent);
    }
    private void openCategoryManagement() {
        Intent intent = new Intent(this, CategoryActivity.class);
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
    private void signOut() {
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Toast.makeText(AdminActivity.this, "You have been signed out.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
} 