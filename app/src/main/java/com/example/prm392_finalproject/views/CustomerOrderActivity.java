package com.example.prm392_finalproject.views;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.controllers.CustomerOrderRepository;
import com.example.prm392_finalproject.models.CustomerOrder;
import com.example.prm392_finalproject.utils.DistanceCalculator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CustomerOrderActivity extends AppCompatActivity {
    private static final String TAG = "CustomerOrderActivity";

    // UI Components
    private ListView listViewOrders;
    private Button btnRefresh, btnOrderHistory;
    private LinearLayout emptyStateLayout;
    private TextView tvWelcomeCustomer;

    // Statistics TextViews
    private TextView tvTotalOrderCount;
    private TextView tvPendingOrderCount;
    private TextView tvCompletedOrderCount;
    private TextView tvShippingOrderCount;

    // Distance Summary Card
    private LinearLayout distanceSummaryCard;
    private TextView tvDistanceValue;
    private TextView tvDurationValue;
    private TextView tvShippingFeeValue;
    private Button btnHideDistanceSummary;
    private TextView tvDistanceStatus;

    // Customer Info
    private TextView tvCustomerInfo;
    private TextView tvDeliveryAddress;

    // Data and Repository
    private CustomerOrderRepository orderRepository;
    private CustomerOrderAdapter orderAdapter;
    private List<CustomerOrder> orderList;

    // Distance Calculator
    private DistanceCalculator distanceCalculator;
    private static final int MAX_DELIVERY_DISTANCE_KM = 50;
    private AlertDialog loadingDialog;

    // Customer session data
    private int currentCustomerId;
    private String customerName;
    private String customerAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_order);

        try {
            Log.d(TAG, "CustomerOrderActivity onCreate started");
            
            loadCustomerSession();
            initViews();
            initData();
            setupListeners();
            loadCustomerOrders();

            // Auto-calculate distance for customer address if available
            if (customerAddress != null && !customerAddress.trim().isEmpty()) {
                autoCalculateDeliveryDistance();
            }
            
            Log.d(TAG, "CustomerOrderActivity onCreate completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khởi tạo: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void loadCustomerSession() {
        SharedPreferences prefs = getSharedPreferences("customer_session", MODE_PRIVATE);
        currentCustomerId = prefs.getInt("customer_id", -1);
        customerName = prefs.getString("customer_name", "Khách hàng");
        customerAddress = prefs.getString("customer_address", "");

        Log.d(TAG, "Loaded from SharedPreferences - customer_id: " + currentCustomerId + ", name: " + customerName);

        // Also check intent data
        Intent intent = getIntent();
        if (intent != null) {
            int intentCustomerId = intent.getIntExtra("customer_id", -1);
            if (intentCustomerId != -1) {
                currentCustomerId = intentCustomerId;
                customerName = intent.getStringExtra("customer_name");
                customerAddress = intent.getStringExtra("customer_address");
                
                Log.d(TAG, "Loaded from Intent - customer_id: " + currentCustomerId + ", name: " + customerName);
            }
        }

        if (currentCustomerId == -1) {
            Log.e(TAG, "No valid customer_id found, finishing activity");
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Log.d(TAG, "Customer session loaded successfully - ID: " + currentCustomerId + ", Name: " + customerName);
        }
    }

    private void initViews() {
        Log.d(TAG, "Initializing views");
        
        // Main UI components
        listViewOrders = findViewById(R.id.listViewCustomerOrders);
        if (listViewOrders == null) {
            Log.e(TAG, "listViewCustomerOrders is null!");
        } else {
            Log.d(TAG, "listViewCustomerOrders found successfully");
        }
        
        btnRefresh = findViewById(R.id.btnRefreshOrders);
        btnOrderHistory = findViewById(R.id.btnOrderHistory);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        tvWelcomeCustomer = findViewById(R.id.tvWelcomeCustomer);

        // Customer info
        tvCustomerInfo = findViewById(R.id.tvCustomerInfo);
        tvDeliveryAddress = findViewById(R.id.tvDeliveryAddress);

        // Statistics TextViews
        tvTotalOrderCount = findViewById(R.id.tvTotalOrderCount);
        tvPendingOrderCount = findViewById(R.id.tvPendingOrderCount);
        tvCompletedOrderCount = findViewById(R.id.tvCompletedOrderCount);
        tvShippingOrderCount = findViewById(R.id.tvShippingOrderCount);

        // Distance Summary Card
        distanceSummaryCard = findViewById(R.id.distanceSummaryCard);
        tvDistanceValue = findViewById(R.id.tvDistanceValue);
        tvDurationValue = findViewById(R.id.tvDurationValue);
        tvShippingFeeValue = findViewById(R.id.tvShippingFeeValue);
        btnHideDistanceSummary = findViewById(R.id.btnHideDistanceSummary);
        tvDistanceStatus = findViewById(R.id.tvDistanceStatus);

        // Set welcome message
        tvWelcomeCustomer.setText("Xin chào, " + customerName + " 👋");

        // Set customer info
        tvCustomerInfo.setText("📱 ID: " + currentCustomerId + " • " + customerName);
        if (customerAddress != null && !customerAddress.trim().isEmpty()) {
            tvDeliveryAddress.setText("📍 " + customerAddress);
            tvDeliveryAddress.setVisibility(View.VISIBLE);
        } else {
            tvDeliveryAddress.setVisibility(View.GONE);
        }
        
        Log.d(TAG, "Views initialized successfully");
    }

    private void initData() {
        Log.d(TAG, "Initializing data components");
        try {
            orderRepository = new CustomerOrderRepository();
            orderList = new ArrayList<>();
            orderAdapter = new CustomerOrderAdapter(this, orderList);
            
            if (listViewOrders != null) {
                listViewOrders.setAdapter(orderAdapter);
                Log.d(TAG, "Adapter set successfully");
            } else {
                Log.e(TAG, "listViewOrders is null, cannot set adapter");
            }

            // Initialize distance calculator
            distanceCalculator = new DistanceCalculator(this);
            Log.d(TAG, "Data components initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing data components: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupListeners() {
        btnRefresh.setOnClickListener(v -> loadCustomerOrders());
        btnOrderHistory.setOnClickListener(v -> showOrderHistoryOptions());
        btnHideDistanceSummary.setOnClickListener(v -> hideDistanceSummary());

        // ListView item click listener
        listViewOrders.setOnItemClickListener((parent, view, position, id) -> {
            CustomerOrder order = orderList.get(position);
            showOrderDetailsDialog(order);
        });

        // ListView item long click for distance check
        listViewOrders.setOnItemLongClickListener((parent, view, position, id) -> {
            CustomerOrder order = orderList.get(position);
            if (order.getShippingAddress() != null && !order.getShippingAddress().trim().isEmpty()) {
                checkOrderDeliveryDistance(order);
            } else {
                Toast.makeText(this, "Đơn hàng này chưa có địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
    }

    // ===== DISTANCE CALCULATION METHODS =====

    private void autoCalculateDeliveryDistance() {
        if (customerAddress == null || customerAddress.trim().isEmpty()) {
            return;
        }

        updateDistanceStatus("🔍 Đang tính khoảng cách...", "#FF9800");

        distanceCalculator.calculateDistanceToCustomer(customerAddress, new DistanceCalculator.DistanceCallback() {
            @Override
            public void onSuccess(DistanceCalculator.DistanceResult result) {
                boolean isWithinRange = result.isValidForDelivery(MAX_DELIVERY_DISTANCE_KM);
                showDistanceSummary(result, isWithinRange);
                updateDistanceStatus("✅ Sẵn sàng giao hàng", "#4CAF50");

                // Show notification if outside delivery range
                if (!isWithinRange) {
                    showDeliveryRangeWarning(result);
                }
            }

            @Override
            public void onError(String error) {
                updateDistanceStatus("❌ Không thể tính khoảng cách", "#F44336");
                Log.e(TAG, "Distance calculation error: " + error);
            }
        });
    }

    private void checkOrderDeliveryDistance(CustomerOrder order) {
        String address = order.getShippingAddress();
        if (address == null || address.trim().isEmpty()) {
            Toast.makeText(this, "Đơn hàng này chưa có địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        updateDistanceStatus("🔍 Đang kiểm tra đơn #" + order.getOrderId(), "#2196F3");
        showLoadingDialog("Đang tính khoảng cách giao hàng...");

        distanceCalculator.calculateDistanceToCustomer(address, new DistanceCalculator.DistanceCallback() {
            @Override
            public void onSuccess(DistanceCalculator.DistanceResult result) {
                hideLoadingDialog();
                boolean isWithinRange = result.isValidForDelivery(MAX_DELIVERY_DISTANCE_KM);
                double shippingFee = distanceCalculator.calculateShippingFee(result.distance, "Standard");

                String message = "📍 Địa chỉ: " + address + "\n\n" +
                        result.getFormattedInfo() + "\n" +
                        "💰 Phí giao hàng dự kiến: " + String.format("%,.0f VNĐ", shippingFee) + "\n\n" +
                        (isWithinRange ? "✅ Trong phạm vi giao hàng" : "❌ Ngoài phạm vi giao hàng (" + MAX_DELIVERY_DISTANCE_KM + "km)");

                showOrderDistanceResultDialog(order, isWithinRange, message, result);
                showDistanceSummary(result, isWithinRange);
                updateDistanceStatus("✅ Hoàn thành", "#4CAF50");
            }

            @Override
            public void onError(String error) {
                hideLoadingDialog();
                updateDistanceStatus("❌ Lỗi", "#F44336");
                Toast.makeText(CustomerOrderActivity.this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showDistanceSummary(DistanceCalculator.DistanceResult result, boolean isValid) {
        tvDistanceValue.setText(result.distance);
        tvDurationValue.setText(result.duration);

        double fee = distanceCalculator.calculateShippingFee(result.distance, "Standard");
        tvShippingFeeValue.setText(String.format("%,.0f VNĐ", fee));

        // Set colors based on validity
        if (isValid) {
            tvDistanceValue.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvDistanceValue.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }

        // Show the card with animation
        distanceSummaryCard.setVisibility(View.VISIBLE);
        distanceSummaryCard.setAlpha(0f);
        distanceSummaryCard.animate()
                .alpha(1f)
                .setDuration(300)
                .start();
    }

    private void hideDistanceSummary() {
        distanceSummaryCard.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> distanceSummaryCard.setVisibility(View.GONE))
                .start();
    }

    private void updateDistanceStatus(String status, String colorHex) {
        tvDistanceStatus.setText(status);
        try {
            tvDistanceStatus.setTextColor(android.graphics.Color.parseColor(colorHex));
        } catch (Exception e) {
            // Use default color if parsing fails
        }
    }

    private void showDeliveryRangeWarning(DistanceCalculator.DistanceResult result) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("⚠️ Cảnh báo phạm vi giao hàng")
                .setMessage("Địa chỉ của bạn nằm ngoài phạm vi giao hàng tiêu chuẩn (" + MAX_DELIVERY_DISTANCE_KM + "km).\n\n" +
                        "📏 Khoảng cách: " + result.distance + "\n" +
                        "⏱️ Thời gian: " + result.duration + "\n\n" +
                        "Vui lòng liên hệ cửa hàng để được hỗ trợ về phí giao hàng đặc biệt.")
                .setPositiveButton("Hiểu rồi", null)
                .setNeutralButton("📞 Liên hệ cửa hàng", (dialog, which) -> {
                    showStoreContactDialog();
                })
                .show();
    }

    private void showStoreContactDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("📞 Liên hệ cửa hàng")
                .setMessage("🏪 Technology Device Store\n\n" +
                        "📱 Hotline: 1900-1234\n" +
                        "📧 Email: support@techstore.com\n" +
                        "🕒 Giờ làm việc: 8:00 - 22:00\n" +
                        "📍 Địa chỉ kho: " + DistanceCalculator.WAREHOUSE_ADDRESS)
                .setPositiveButton("OK", null)
                .show();
    }

    // ===== ORDER MANAGEMENT METHODS =====

    private void loadCustomerOrders() {
        new LoadCustomerOrdersTask().execute(currentCustomerId);
    }

    private void showOrderDetailsDialog(CustomerOrder order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String details = "🆔 Mã đơn hàng: " + order.getOrderId() + "\n" +
                "📅 Ngày đặt: " + formatDate(order.getOrderDate()) + "\n" +
                "💰 Tổng tiền: " + String.format("%,.0f VNĐ", order.getTotalAmount()) + "\n" +
                "📊 Trạng thái: " + order.getStatusDisplay() + "\n";

        if (order.getShippingAddress() != null && !order.getShippingAddress().isEmpty()) {
            details += "📍 Địa chỉ giao hàng: " + order.getShippingAddress() + "\n";
        }

        if (order.getShippingMethod() != null) {
            details += "🚚 Phương thức giao hàng: " + order.getShippingMethod() + "\n";
        }

        if (order.getTrackingNumber() != null) {
            details += "📦 Mã vận đơn: " + order.getTrackingNumber() + "\n";
        }

        if (order.getExpectedDelivery() != null) {
            details += "📅 Dự kiến giao hàng: " + formatDate(order.getExpectedDelivery()) + "\n";
        }

        if (order.getNote() != null && !order.getNote().isEmpty()) {
            details += "📝 Ghi chú: " + order.getNote() + "\n";
        }

        builder.setTitle("Chi tiết đơn hàng")
                .setMessage(details)
                .setPositiveButton("Đóng", null);

        // Add distance check button if shipping address exists
        if (order.getShippingAddress() != null && !order.getShippingAddress().trim().isEmpty()) {
            builder.setNeutralButton("📏 Kiểm tra khoảng cách", (dialog, which) -> {
                checkOrderDeliveryDistance(order);
            });
        }

        // Add order tracking button if tracking number exists
        if (order.getTrackingNumber() != null && !order.getTrackingNumber().trim().isEmpty()) {
            builder.setNegativeButton("📦 Theo dõi đơn hàng", (dialog, which) -> {
                showTrackingInfo(order);
            });
        }

        builder.show();
    }

    private void showTrackingInfo(CustomerOrder order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String trackingInfo = "📦 Thông tin vận chuyển\n\n" +
                "🆔 Mã vận đơn: " + order.getTrackingNumber() + "\n" +
                "📊 Trạng thái: " + order.getStatusDisplay() + "\n";

        if (order.getExpectedDelivery() != null) {
            trackingInfo += "📅 Dự kiến giao: " + formatDate(order.getExpectedDelivery()) + "\n";
        }

        trackingInfo += "\n🚚 Bạn có thể theo dõi đơn hàng qua:\n" +
                "• Ứng dụng của chúng tôi\n" +
                "• Website: techstore.com/tracking\n" +
                "• Hotline: 1900-1234";

        builder.setTitle("Theo dõi đơn hàng")
                .setMessage(trackingInfo)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showOrderHistoryOptions() {
        String[] options = {"Tất cả đơn hàng", "Đơn hàng đang xử lý", "Đơn hàng đã hoàn thành", "Đơn hàng đã hủy"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("📋 Lọc đơn hàng")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: loadCustomerOrders(); break;
                        case 1: new FilterOrdersTask().execute(currentCustomerId, "Pending,Processing,Shipped"); break;
                        case 2: new FilterOrdersTask().execute(currentCustomerId, "Completed,Delivered"); break;
                        case 3: new FilterOrdersTask().execute(currentCustomerId, "Cancelled"); break;
                    }
                })
                .show();
    }

    private void showOrderDistanceResultDialog(CustomerOrder order, boolean isValid, String message, DistanceCalculator.DistanceResult result) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String title = isValid ? "✅ Trong phạm vi giao hàng" : "⚠️ Ngoài phạm vi giao hàng";

        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null);

        if (!isValid) {
            builder.setNeutralButton("📞 Liên hệ hỗ trợ", (dialog, which) -> {
                showStoreContactDialog();
            });
        }

        builder.show();
    }

    // ===== UTILITY METHODS =====

    private void showLoadingDialog(String message) {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setCancelable(false);
        loadingDialog = builder.create();
        loadingDialog.show();
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private String formatDate(java.util.Date date) {
        if (date == null) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

    // ===== STATISTICS =====

    private void updateOrderStats() {
        if (orderList == null || orderList.isEmpty()) {
            updateStatCount(tvTotalOrderCount, 0, " đơn");
            updateStatCount(tvPendingOrderCount, 0, "");
            updateStatCount(tvCompletedOrderCount, 0, "");
            updateStatCount(tvShippingOrderCount, 0, "");

            if (emptyStateLayout != null) {
                emptyStateLayout.setVisibility(View.VISIBLE);
                listViewOrders.setVisibility(View.GONE);
            }
            return;
        }

        if (emptyStateLayout != null) {
            emptyStateLayout.setVisibility(View.GONE);
            listViewOrders.setVisibility(View.VISIBLE);
        }

        int pendingCount = 0, completedCount = 0, shippingCount = 0;

        for (CustomerOrder order : orderList) {
            String status = order.getStatus();
            switch (status) {
                case "Pending":
                case "Processing":
                    pendingCount++;
                    break;
                case "Completed":
                case "Delivered":
                    completedCount++;
                    break;
                case "Shipped":
                    shippingCount++;
                    break;
            }
        }

        updateStatCount(tvTotalOrderCount, orderList.size(), " đơn");
        updateStatCount(tvPendingOrderCount, pendingCount, "");
        updateStatCount(tvCompletedOrderCount, completedCount, "");
        updateStatCount(tvShippingOrderCount, shippingCount, "");
    }

    private void updateStatCount(TextView textView, int count, String suffix) {
        if (textView != null) {
            textView.setText(String.valueOf(count) + suffix);
        }
    }

    // ===== ASYNC TASKS =====

    private class LoadCustomerOrdersTask extends AsyncTask<Integer, Void, List<CustomerOrder>> {
        @Override
        protected void onPreExecute() {
            updateDistanceStatus("🔄 Đang tải đơn hàng...", "#2196F3");
            Log.d(TAG, "Starting to load orders for customer_id: " + currentCustomerId);
        }

        @Override
        protected List<CustomerOrder> doInBackground(Integer... customerIds) {
            try {
                Log.d(TAG, "Loading orders for customer_id: " + customerIds[0]);
                List<CustomerOrder> orders = orderRepository.getOrdersByCustomerId(customerIds[0]);
                Log.d(TAG, "Loaded " + orders.size() + " orders from database");
                return orders;
            } catch (Exception e) {
                Log.e(TAG, "Error loading customer orders: " + e.getMessage());
                e.printStackTrace();
                return new ArrayList<>();
            }
        }

        @Override
        protected void onPostExecute(List<CustomerOrder> orders) {
            Log.d(TAG, "onPostExecute: Received " + orders.size() + " orders");
            orderList.clear();
            orderList.addAll(orders);
            orderAdapter.notifyDataSetChanged();
            updateOrderStats();

            updateDistanceStatus("📦 Đã tải " + orders.size() + " đơn hàng", "#4CAF50");
            Toast.makeText(CustomerOrderActivity.this, "Đã tải " + orders.size() + " đơn hàng", Toast.LENGTH_SHORT).show();
        }
    }

    private class FilterOrdersTask extends AsyncTask<Object, Void, List<CustomerOrder>> {
        @Override
        protected List<CustomerOrder> doInBackground(Object... params) {
            try {
                int customerId = (Integer) params[0];
                String statusFilter = (String) params[1];
                return orderRepository.getOrdersByCustomerIdAndStatus(customerId, statusFilter);
            } catch (Exception e) {
                Log.e(TAG, "Error filtering orders: " + e.getMessage());
                return new ArrayList<>();
            }
        }

        @Override
        protected void onPostExecute(List<CustomerOrder> orders) {
            orderList.clear();
            orderList.addAll(orders);
            orderAdapter.notifyDataSetChanged();
            updateOrderStats();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideLoadingDialog();
        hideDistanceSummary();
    }
}