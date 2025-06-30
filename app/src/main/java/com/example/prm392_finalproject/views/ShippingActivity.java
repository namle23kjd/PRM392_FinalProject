package com.example.prm392_finalproject.views;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.controllers.ShippingRepository;
import com.example.prm392_finalproject.models.Shipping;
import com.example.prm392_finalproject.utils.DistanceCalculator;
import com.example.prm392_finalproject.views.ShippingAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ShippingActivity extends AppCompatActivity {
    private static final String TAG = "ShippingActivity";

    // UI Components
    private ListView listViewShippings;
    private Button btnAddShipping, btnRefresh, btnTrackShipping;
    private EditText etSearchTracking;
    private Spinner spinnerStatusFilter;
    private LinearLayout emptyStateLayout;

    // Statistics TextViews
    private TextView tvTotalShippingCount;
    private TextView tvPendingCount;
    private TextView tvShippingCount;
    private TextView tvDeliveredCount;
    private TextView tvOverdueCount;

    // New UI Components for Distance Features
    private EditText etQuickDistanceCheck;
    private Button btnQuickDistanceCheck;
    private Button btnDistanceSettings;
    private LinearLayout distanceSummaryCard;
    private TextView tvDistanceValue;
    private TextView tvDurationValue;
    private TextView tvShippingFeeValue;
    private Button btnHideDistanceSummary;
    private TextView tvDistanceStatus;

    // Data and Repository
    private ShippingRepository shippingRepository;
    private ShippingAdapter shippingAdapter;
    private List<Shipping> shippingList;

    // Distance Calculator
    private DistanceCalculator distanceCalculator;
    private static final int MAX_DELIVERY_DISTANCE_KM = 50;
    private AlertDialog loadingDialog;

    private String[] statusOptions = {"Tất cả", "Pending", "Shipped", "Delivered", "Cancelled"};
    private String[] shippingMethods = {"Standard", "Express"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipping);

        initViews();
        initData();
        setupListeners();
        loadShippings();
    }

    private void initViews() {
        // Main UI components
        listViewShippings = findViewById(R.id.listViewShippings);
        btnAddShipping = findViewById(R.id.btnAddShipping);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnTrackShipping = findViewById(R.id.btnTrackShipping);
        etSearchTracking = findViewById(R.id.etSearchTracking);
        spinnerStatusFilter = findViewById(R.id.spinnerStatusFilter);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);

        // Statistics TextViews
        tvTotalShippingCount = findViewById(R.id.tvTotalShippingCount);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvShippingCount = findViewById(R.id.tvShippingCount);
        tvDeliveredCount = findViewById(R.id.tvDeliveredCount);
        tvOverdueCount = findViewById(R.id.tvOverdueCount);

        // New Distance-related UI components
        etQuickDistanceCheck = findViewById(R.id.etQuickDistanceCheck);
        btnQuickDistanceCheck = findViewById(R.id.btnQuickDistanceCheck);
        btnDistanceSettings = findViewById(R.id.btnDistanceSettings);
        distanceSummaryCard = findViewById(R.id.distanceSummaryCard);
        tvDistanceValue = findViewById(R.id.tvDistanceValue);
        tvDurationValue = findViewById(R.id.tvDurationValue);
        tvShippingFeeValue = findViewById(R.id.tvShippingFeeValue);
        btnHideDistanceSummary = findViewById(R.id.btnHideDistanceSummary);
        tvDistanceStatus = findViewById(R.id.tvDistanceStatus);

        // Setup status filter spinner
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, statusOptions);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatusFilter.setAdapter(statusAdapter);
    }

    private void initData() {
        shippingRepository = new ShippingRepository();
        shippingList = new ArrayList<>();
        shippingAdapter = new ShippingAdapter(this, shippingList);
        listViewShippings.setAdapter(shippingAdapter);

        // Khởi tạo distance calculator
        distanceCalculator = new DistanceCalculator(this);
    }

    private void setupListeners() {
        btnAddShipping.setOnClickListener(v -> showAddShippingDialog());
        btnRefresh.setOnClickListener(v -> loadShippings());
        btnTrackShipping.setOnClickListener(v -> trackShippingWithDistance());

        // New Distance-related listeners
        btnQuickDistanceCheck.setOnClickListener(v -> performQuickDistanceCheck());
        btnDistanceSettings.setOnClickListener(v -> showDistanceSettingsDialog());
        btnHideDistanceSummary.setOnClickListener(v -> hideDistanceSummary());

        // Status filter listener
        spinnerStatusFilter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                filterShippingsByStatus(position);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // ListView item click listener
        listViewShippings.setOnItemClickListener((parent, view, position, id) -> {
            Shipping shipping = shippingList.get(position);
            showShippingDetailsDialog(shipping);
        });

        // ListView item long click listener for editing
        listViewShippings.setOnItemLongClickListener((parent, view, position, id) -> {
            Shipping shipping = shippingList.get(position);
            showEditShippingDialog(shipping);
            return true;
        });
    }

    // ===== NEW DISTANCE-RELATED METHODS =====

    private void performQuickDistanceCheck() {
        String address = etQuickDistanceCheck.getText().toString().trim();
        if (address.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập địa chỉ", Toast.LENGTH_SHORT).show();
            etQuickDistanceCheck.requestFocus();
            return;
        }

        updateDistanceStatus("🔍 Đang kiểm tra...", "#FF9800");

        checkDeliveryDistance(address, new DistanceCheckCallback() {
            @Override
            public void onResult(boolean isValid, String message, DistanceCalculator.DistanceResult result) {
                if (result != null) {
                    showDistanceSummary(result, isValid);
                    updateDistanceStatus("✅ Hoàn thành", "#4CAF50");
                } else {
                    updateDistanceStatus("❌ Lỗi", "#F44336");
                    Toast.makeText(ShippingActivity.this, message, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void showDistanceSummary(DistanceCalculator.DistanceResult result, boolean isValid) {
        // Update distance summary card
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

    private void showDistanceSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("📏 Cài đặt khoảng cách")
                .setMessage("Phạm vi giao hàng hiện tại: " + MAX_DELIVERY_DISTANCE_KM + " km\n" +
                        "Vị trí kho: " + DistanceCalculator.WAREHOUSE_ADDRESS + "\n\n" +
                        "Phí giao hàng:\n" +
                        "• Standard: 20k + 2k/km\n" +
                        "• Express: 30k + 3k/km")
                .setPositiveButton("OK", null)
                .setNeutralButton("📍 Đổi vị trí kho", (dialog, which) -> {
                    Toast.makeText(this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void trackShippingWithDistance() {
        String trackingNumber = etSearchTracking.getText().toString().trim();
        if (trackingNumber.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mã tracking", Toast.LENGTH_SHORT).show();
            return;
        }

        updateDistanceStatus("🔍 Đang tìm...", "#2196F3");
        new TrackShippingTask().execute(trackingNumber);
    }

    // ===== DISTANCE CALCULATOR METHODS =====

    private void checkDeliveryDistance(String address, DistanceCheckCallback callback) {
        if (address == null || address.trim().isEmpty()) {
            callback.onResult(false, "Địa chỉ không hợp lệ", null);
            return;
        }

        // Hiển thị loading
        showLoadingDialog("Đang kiểm tra khoảng cách...");

        distanceCalculator.calculateDistanceToCustomer(address, new DistanceCalculator.DistanceCallback() {
            @Override
            public void onSuccess(DistanceCalculator.DistanceResult result) {
                hideLoadingDialog();

                boolean isWithinRange = result.isValidForDelivery(MAX_DELIVERY_DISTANCE_KM);
                double shippingFee = distanceCalculator.calculateShippingFee(result.distance, "Standard");

                String message = result.getFormattedInfo() + "\n" +
                        "💰 Phí giao hàng dự kiến: " + String.format("%,.0f VNĐ", shippingFee) + "\n" +
                        (isWithinRange ? "✅ Trong phạm vi giao hàng" : "❌ Ngoài phạm vi giao hàng (" + MAX_DELIVERY_DISTANCE_KM + "km)");

                callback.onResult(isWithinRange, message, result);
            }

            @Override
            public void onError(String error) {
                hideLoadingDialog();
                callback.onResult(false, "Lỗi: " + error, null);
            }
        });
    }

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

    private void showDistanceResultDialog(boolean isValid, String message, DistanceCalculator.DistanceResult result) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isValid ? "✅ Kết quả kiểm tra" : "⚠️ Cảnh báo")
                .setMessage(message)
                .setPositiveButton("OK", null);

        if (result != null) {
            builder.setNeutralButton("📍 Xem bản đồ", (dialog, which) -> {
                showRouteOnMap(result);
            });
        }

        builder.show();
    }

    private void showDistanceWarningDialog(String message, Runnable onContinue) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("⚠️ Ngoài phạm vi giao hàng")
                .setMessage(message + "\n\nBạn có muốn tiếp tục tạo đơn giao hàng không?")
                .setPositiveButton("Tiếp tục", (dialog, which) -> onContinue.run())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showRouteOnMap(DistanceCalculator.DistanceResult result) {
        Toast.makeText(this, "Tính năng xem bản đồ đang phát triển", Toast.LENGTH_SHORT).show();
    }

    private void showEnhancedDistanceResultDialog(boolean isValid, String message, DistanceCalculator.DistanceResult result, EditText addressField) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String title = isValid ? "✅ Trong phạm vi giao hàng" : "⚠️ Ngoài phạm vi giao hàng";

        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null);

        if (result != null) {
            builder.setNeutralButton("📍 Xem bản đồ", (dialog, which) -> {
                showRouteOnMap(result);
            });

            // Nếu trong phạm vi, offer to use this address
            if (isValid) {
                builder.setNegativeButton("📝 Sử dụng địa chỉ này", (dialog, which) -> {
                    // Copy address to quick check field
                    etQuickDistanceCheck.setText(result.customerAddress);
                });
            }
        }

        builder.show();
    }

    // ===== MAIN SHIPPING METHODS =====

    private void loadShippings() {
        new LoadShippingsTask().execute();
    }

    private void filterShippingsByStatus(int statusPosition) {
        if (statusPosition == 0) {
            loadShippings();
        } else {
            String status = getStatusFromPosition(statusPosition);
            new FilterShippingsTask().execute(status);
        }
    }

    private String getStatusFromPosition(int position) {
        switch (position) {
            case 1: return "Pending";
            case 2: return "Shipped";
            case 3: return "Delivered";
            case 4: return "Cancelled";
            default: return "";
        }
    }

    private void showAddShippingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_shipping, null);

        EditText etOrderId = dialogView.findViewById(R.id.etOrderId);
        EditText etShippingAddress = dialogView.findViewById(R.id.etShippingAddress);
        EditText etPersonName = dialogView.findViewById(R.id.etPersonName);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);
        EditText etExpectedDelivery = dialogView.findViewById(R.id.etExpectedDelivery);
        Spinner spinnerMethod = dialogView.findViewById(R.id.spinnerShippingMethod);

        // Thêm enhanced button kiểm tra khoảng cách
        Button btnCheckDistance = new Button(this);
        btnCheckDistance.setText("📏 Kiểm tra khoảng cách & phí giao hàng");
        btnCheckDistance.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
        btnCheckDistance.setTextColor(getResources().getColor(android.R.color.white));
        btnCheckDistance.setPadding(16, 12, 16, 12);
        btnCheckDistance.setTextSize(12);

        // Thêm button vào layout
        LinearLayout layout = dialogView.findViewById(R.id.layoutContainer); // ✅ Đúng

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 16, 0, 16);
        btnCheckDistance.setLayoutParams(params);
        layout.addView(btnCheckDistance, layout.getChildCount() - 1);

        // Pre-fill địa chỉ từ quick distance check nếu có
        String quickAddress = etQuickDistanceCheck.getText().toString().trim();
        if (!quickAddress.isEmpty()) {
            etShippingAddress.setText(quickAddress);
        }

        btnCheckDistance.setOnClickListener(v -> {
            String address = etShippingAddress.getText().toString().trim();
            if (address.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập địa chỉ trước", Toast.LENGTH_SHORT).show();
                etShippingAddress.requestFocus();
                return;
            }

            // Update text của button khi đang check
            btnCheckDistance.setText("🔍 Đang kiểm tra...");
            btnCheckDistance.setEnabled(false);

            checkDeliveryDistance(address, new DistanceCheckCallback() {
                @Override
                public void onResult(boolean isValid, String message, DistanceCalculator.DistanceResult result) {
                    // Restore button
                    btnCheckDistance.setText("📏 Kiểm tra khoảng cách & phí giao hàng");
                    btnCheckDistance.setEnabled(true);

                    if (result != null) {
                        // Update quick summary trong main screen
                        showDistanceSummary(result, isValid);

                        // Show detailed result
                        showEnhancedDistanceResultDialog(isValid, message, result, etShippingAddress);
                    } else {
                        showDistanceResultDialog(false, message, null);
                    }
                }
            });
        });

        // Setup shipping method spinner
        ArrayAdapter<String> methodAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, shippingMethods);
        methodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMethod.setAdapter(methodAdapter);

        // Setup date picker for expected delivery
        etExpectedDelivery.setOnClickListener(v -> showDatePicker(etExpectedDelivery));

        builder.setView(dialogView)
                .setTitle("Thêm chuyến giao hàng")
                .setPositiveButton("Thêm", null)
                .setNegativeButton("Hủy", null);

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String orderIdStr = etOrderId.getText().toString().trim();
                String address = etShippingAddress.getText().toString().trim();
                String personName = etPersonName.getText().toString().trim();
                String description = etDescription.getText().toString().trim();
                String expectedDeliveryStr = etExpectedDelivery.getText().toString().trim();
                String method = shippingMethods[spinnerMethod.getSelectedItemPosition()];

                if (validateShippingInput(orderIdStr, address, personName, expectedDeliveryStr)) {
                    // Auto-check distance before creating
                    checkDeliveryDistance(address, new DistanceCheckCallback() {
                        @Override
                        public void onResult(boolean isValid, String message, DistanceCalculator.DistanceResult result) {
                            if (!isValid) {
                                showDistanceWarningDialog(message, () -> {
                                    createShippingRecord(orderIdStr, address, personName, description, expectedDeliveryStr, method, dialog);
                                });
                            } else {
                                createShippingRecord(orderIdStr, address, personName, description, expectedDeliveryStr, method, dialog);
                            }
                        }
                    });
                }
            });
        });

        dialog.show();
    }

    // Method helper để tạo shipping record
    private void createShippingRecord(String orderIdStr, String address, String personName,
                                      String description, String expectedDeliveryStr, String method, AlertDialog dialog) {
        try {
            int orderId = Integer.parseInt(orderIdStr);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date expectedDelivery = sdf.parse(expectedDeliveryStr);

            Shipping shipping = new Shipping(orderId, address, method, personName, expectedDelivery, description);
            new AddShippingTask().execute(shipping);
            dialog.dismiss();

        } catch (Exception e) {
            Log.e(TAG, "Error creating shipping: " + e.getMessage());
            Toast.makeText(this, "Lỗi định dạng dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showEditShippingDialog(Shipping shipping) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_shipping, null);

        EditText etShippingAddress = dialogView.findViewById(R.id.etShippingAddress);
        EditText etPersonName = dialogView.findViewById(R.id.etPersonName);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);
        EditText etExpectedDelivery = dialogView.findViewById(R.id.etExpectedDelivery);
        Spinner spinnerMethod = dialogView.findViewById(R.id.spinnerShippingMethod);
        Spinner spinnerStatus = dialogView.findViewById(R.id.spinnerStatus);

        // Pre-fill data
        etShippingAddress.setText(shipping.getShippingAddress());
        etPersonName.setText(shipping.getShippingPersonName());
        etDescription.setText(shipping.getDescription());

        if (shipping.getExpectedDelivery() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            etExpectedDelivery.setText(sdf.format(shipping.getExpectedDelivery()));
        }

        // Setup spinners
        ArrayAdapter<String> methodAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, shippingMethods);
        methodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMethod.setAdapter(methodAdapter);

        String[] statusArray = {"Pending", "Shipped", "Delivered", "Cancelled"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, statusArray);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        // Set current selections
        setSpinnerSelection(spinnerMethod, shipping.getShippingMethod(), shippingMethods);
        setSpinnerSelection(spinnerStatus, shipping.getStatus(), statusArray);

        etExpectedDelivery.setOnClickListener(v -> showDatePicker(etExpectedDelivery));

        builder.setView(dialogView)
                .setTitle("Sửa thông tin giao hàng")
                .setPositiveButton("Cập nhật", (dialog, which) -> {
                    String address = etShippingAddress.getText().toString().trim();
                    String personName = etPersonName.getText().toString().trim();
                    String description = etDescription.getText().toString().trim();
                    String expectedDeliveryStr = etExpectedDelivery.getText().toString().trim();
                    String method = shippingMethods[spinnerMethod.getSelectedItemPosition()];
                    String status = statusArray[spinnerStatus.getSelectedItemPosition()];

                    if (validateShippingInput("1", address, personName, expectedDeliveryStr)) {
                        try {
                            Date expectedDelivery = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                    .parse(expectedDeliveryStr);

                            shipping.setShippingAddress(address);
                            shipping.setShippingPersonName(personName);
                            shipping.setDescription(description);
                            shipping.setExpectedDelivery(expectedDelivery);
                            shipping.setShippingMethod(method);
                            shipping.setStatus(status);

                            new UpdateShippingTask().execute(shipping);

                        } catch (Exception e) {
                            Toast.makeText(this, "Lỗi định dạng ngày", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showShippingDetailsDialog(Shipping shipping) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String details = "Mã giao hàng: " + shipping.getShippingId() + "\n" +
                "Mã đơn hàng: " + shipping.getOrderId() + "\n" +
                "Địa chỉ: " + shipping.getShippingAddress() + "\n" +
                "Người nhận: " + shipping.getShippingPersonName() + "\n" +
                "Phương thức: " + shipping.getShippingMethod() + "\n" +
                "Mã tracking: " + shipping.getTrackingNumber() + "\n" +
                "Trạng thái: " + shipping.getStatusDisplay() + "\n";

        if (shipping.getExpectedDelivery() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            details += "Dự kiến giao: " + sdf.format(shipping.getExpectedDelivery()) + "\n";
        }

        if (shipping.getDeliveredDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            details += "Đã giao: " + sdf.format(shipping.getDeliveredDate()) + "\n";
        }

        if (shipping.getDescription() != null && !shipping.getDescription().isEmpty()) {
            details += "Ghi chú: " + shipping.getDescription() + "\n";
        }

        if (shipping.isOverdue()) {
            details += "\n⚠️ TRỄ HẠN GIAO HÀNG";
        }

        builder.setTitle("Chi tiết giao hàng")
                .setMessage(details)
                .setPositiveButton("Đóng", null)
                .setNeutralButton("Sửa", (dialog, which) -> showEditShippingDialog(shipping))
                .setNegativeButton("📏 Kiểm tra khoảng cách", (dialog, which) -> {
                    updateDistanceStatus("🔍 Đang kiểm tra...", "#2196F3");
                    checkDeliveryDistance(shipping.getShippingAddress(), new DistanceCheckCallback() {
                        @Override
                        public void onResult(boolean isValid, String message, DistanceCalculator.DistanceResult result) {
                            if (result != null) {
                                showDistanceSummary(result, isValid);
                                updateDistanceStatus("✅ Hoàn thành", "#4CAF50");

                                // Also show detailed dialog
                                showDistanceResultDialog(isValid, message, result);
                            } else {
                                updateDistanceStatus("❌ Lỗi", "#F44336");
                                showDistanceResultDialog(false, message, null);
                            }
                        }
                    });
                })
                .show();
    }

    // ===== UTILITY METHODS =====

    private void showDatePicker(EditText editText) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    editText.setText(sdf.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void setSpinnerSelection(Spinner spinner, String value, String[] array) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private boolean validateShippingInput(String orderId, String address, String personName, String expectedDelivery) {
        if (orderId.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mã đơn hàng", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (address.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (personName.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên người nhận", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (expectedDelivery.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ngày dự kiến giao hàng", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            int orderIdInt = Integer.parseInt(orderId);
            if (orderIdInt <= 0) {
                Toast.makeText(this, "Mã đơn hàng phải là số dương", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Mã đơn hàng phải là số", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    // ===== STATISTICS =====

    private void updateShippingStats() {
        if (shippingList == null || shippingList.isEmpty()) {
            updateStatCount(tvTotalShippingCount, 0, " chuyến");
            updateStatCount(tvPendingCount, 0, "");
            updateStatCount(tvShippingCount, 0, "");
            updateStatCount(tvDeliveredCount, 0, "");
            updateStatCount(tvOverdueCount, 0, "");

            if (emptyStateLayout != null) {
                emptyStateLayout.setVisibility(View.VISIBLE);
                listViewShippings.setVisibility(View.GONE);
            }
            return;
        }

        if (emptyStateLayout != null) {
            emptyStateLayout.setVisibility(View.GONE);
            listViewShippings.setVisibility(View.VISIBLE);
        }

        int pendingCount = 0, shippingCount = 0, deliveredCount = 0, overdueCount = 0;

        for (Shipping shipping : shippingList) {
            String status = shipping.getStatus();

            if (shipping.isOverdue() && !"Delivered".equals(status) && !"Cancelled".equals(status)) {
                overdueCount++;
            } else {
                switch (status) {
                    case "Pending": pendingCount++; break;
                    case "Shipped": shippingCount++; break;
                    case "Delivered": deliveredCount++; break;
                    default: Log.w(TAG, "Unknown status: " + status); break;
                }
            }
        }

        updateStatCount(tvTotalShippingCount, shippingList.size(), " chuyến");
        updateStatCount(tvPendingCount, pendingCount, "");
        updateStatCount(tvShippingCount, shippingCount, "");
        updateStatCount(tvDeliveredCount, deliveredCount, "");
        updateStatCount(tvOverdueCount, overdueCount, "");

        // Animate stats update
        tvTotalShippingCount.animate().scaleX(1.1f).scaleY(1.1f).setDuration(150)
                .withEndAction(() -> tvTotalShippingCount.animate().scaleX(1f).scaleY(1f).setDuration(150));
    }

    private void updateStatCount(TextView textView, int count, String suffix) {
        if (textView != null) {
            textView.setText(String.valueOf(count) + suffix);
        }
    }

    // ===== CALLBACK INTERFACE =====

    private interface DistanceCheckCallback {
        void onResult(boolean isValid, String message, DistanceCalculator.DistanceResult result);
    }

    // ===== ENHANCED ASYNC TASKS =====

    private class LoadShippingsTask extends AsyncTask<Void, Void, List<Shipping>> {
        @Override
        protected void onPreExecute() {
            updateDistanceStatus("🔄 Đang tải...", "#2196F3");
        }

        @Override
        protected List<Shipping> doInBackground(Void... voids) {
            try {
                return shippingRepository.getAllShippings();
            } catch (Exception e) {
                Log.e(TAG, "Error loading shippings: " + e.getMessage());
                return new ArrayList<>();
            }
        }

        @Override
        protected void onPostExecute(List<Shipping> shippings) {
            shippingList.clear();
            shippingList.addAll(shippings);
            shippingAdapter.notifyDataSetChanged();
            updateShippingStats();

            updateDistanceStatus("📏 Ready", "#4CAF50");
            Toast.makeText(ShippingActivity.this, "Đã tải " + shippings.size() + " chuyến giao hàng", Toast.LENGTH_SHORT).show();
        }
    }

    private class FilterShippingsTask extends AsyncTask<String, Void, List<Shipping>> {
        @Override
        protected List<Shipping> doInBackground(String... status) {
            try {
                return shippingRepository.getShippingsByStatus(status[0]);
            } catch (Exception e) {
                Log.e(TAG, "Error filtering shippings: " + e.getMessage());
                return new ArrayList<>();
            }
        }

        @Override
        protected void onPostExecute(List<Shipping> shippings) {
            shippingList.clear();
            shippingList.addAll(shippings);
            shippingAdapter.notifyDataSetChanged();
            updateShippingStats();
        }
    }

    private class AddShippingTask extends AsyncTask<Shipping, Void, Boolean> {
        private String errorMessage = "";

        @Override
        protected Boolean doInBackground(Shipping... shippings) {
            try {
                return shippingRepository.createShipping(shippings[0]);
            } catch (Exception e) {
                errorMessage = e.getMessage();
                Log.e(TAG, "Error in AddShippingTask: " + e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(ShippingActivity.this, "Thêm chuyến giao hàng thành công", Toast.LENGTH_SHORT).show();
                loadShippings();
            } else {
                String message = "Thêm chuyến giao hàng thất bại";
                if (!errorMessage.isEmpty()) {
                    message += ": " + errorMessage;
                }
                Toast.makeText(ShippingActivity.this, message, Toast.LENGTH_LONG).show();
            }
        }
    }

    private class UpdateShippingTask extends AsyncTask<Shipping, Void, Boolean> {
        private String errorMessage = "";

        @Override
        protected Boolean doInBackground(Shipping... shippings) {
            try {
                return shippingRepository.updateShipping(shippings[0]);
            } catch (Exception e) {
                errorMessage = e.getMessage();
                Log.e(TAG, "Error in UpdateShippingTask: " + e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(ShippingActivity.this, "Cập nhật thông tin giao hàng thành công", Toast.LENGTH_SHORT).show();
                loadShippings();
            } else {
                String message = "Cập nhật thông tin giao hàng thất bại";
                if (!errorMessage.isEmpty()) {
                    message += ": " + errorMessage;
                }
                Toast.makeText(ShippingActivity.this, message, Toast.LENGTH_LONG).show();
            }
        }
    }

    private class TrackShippingTask extends AsyncTask<String, Void, Shipping> {
        @Override
        protected Shipping doInBackground(String... trackingNumbers) {
            try {
                return shippingRepository.getShippingByTrackingNumber(trackingNumbers[0]);
            } catch (Exception e) {
                Log.e(TAG, "Error tracking shipping: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(Shipping shipping) {
            if (shipping != null) {
                updateDistanceStatus("✅ Đã tìm thấy", "#4CAF50");
                showShippingDetailsDialog(shipping);

                // Auto-populate quick distance check với địa chỉ của shipping này
                etQuickDistanceCheck.setText(shipping.getShippingAddress());
            } else {
                updateDistanceStatus("❌ Không tìm thấy", "#F44336");
                Toast.makeText(ShippingActivity.this, "Không tìm thấy thông tin với mã tracking này", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideLoadingDialog();
        hideDistanceSummary();
    }
}