package com.example.prm392_finalproject.views;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.controllers.ShippingRepository;
import com.example.prm392_finalproject.models.Shipping;
import com.example.prm392_finalproject.views.ShippingAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ShippingActivity extends AppCompatActivity {
    private static final String TAG = "ShippingActivity";

    private ListView listViewShippings;
    private Button btnAddShipping, btnRefresh, btnTrackShipping;
    private EditText etSearchTracking;
    private Spinner spinnerStatusFilter;

    private ShippingRepository shippingRepository;
    private ShippingAdapter shippingAdapter;
    private List<Shipping> shippingList;

    private String[] statusOptions = {"Tất cả", "pending", "preparing", "shipping", "delivered", "cancelled"};
    private String[] shippingMethods = {"Giao hàng tiêu chuẩn", "Giao hàng nhanh", "Giao hàng siêu tốc"};

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
        listViewShippings = findViewById(R.id.listViewShippings);
        btnAddShipping = findViewById(R.id.btnAddShipping);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnTrackShipping = findViewById(R.id.btnTrackShipping);
        etSearchTracking = findViewById(R.id.etSearchTracking);
        spinnerStatusFilter = findViewById(R.id.spinnerStatusFilter);

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
    }

    private void setupListeners() {
        btnAddShipping.setOnClickListener(v -> showAddShippingDialog());
        btnRefresh.setOnClickListener(v -> loadShippings());
        btnTrackShipping.setOnClickListener(v -> trackShipping());

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

    private void loadShippings() {
        new LoadShippingsTask().execute();
    }

    private void filterShippingsByStatus(int statusPosition) {
        if (statusPosition == 0) {
            // Show all
            loadShippings();
        } else {
            String status = getStatusFromPosition(statusPosition);
            new FilterShippingsTask().execute(status);
        }
    }

    private String getStatusFromPosition(int position) {
        switch (position) {
            case 1: return "pending";
            case 2: return "preparing";
            case 3: return "shipping";
            case 4: return "delivered";
            case 5: return "cancelled";
            default: return "";
        }
    }

    private void trackShipping() {
        String trackingNumber = etSearchTracking.getText().toString().trim();
        if (trackingNumber.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mã tracking", Toast.LENGTH_SHORT).show();
            return;
        }

        new TrackShippingTask().execute(trackingNumber);
    }

    private void showAddShippingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_shipping, null);

        EditText etOrderId = dialogView.findViewById(R.id.etOrderId);
        EditText etShippingAddress = dialogView.findViewById(R.id.etShippingAddress);
        EditText etPersonName = dialogView.findViewById(R.id.etPersonName);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);
        EditText etExpectedDelivery = dialogView.findViewById(R.id.etExpectedDelivery);
        Spinner spinnerMethod = dialogView.findViewById(R.id.spinnerShippingMethod);

        // Setup shipping method spinner
        ArrayAdapter<String> methodAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, shippingMethods);
        methodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMethod.setAdapter(methodAdapter);

        // Setup date picker for expected delivery
        etExpectedDelivery.setOnClickListener(v -> showDatePicker(etExpectedDelivery));

        builder.setView(dialogView)
                .setTitle("Thêm chuyến giao hàng")
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String orderIdStr = etOrderId.getText().toString().trim();
                    String address = etShippingAddress.getText().toString().trim();
                    String personName = etPersonName.getText().toString().trim();
                    String description = etDescription.getText().toString().trim();
                    String expectedDeliveryStr = etExpectedDelivery.getText().toString().trim();
                    String method = shippingMethods[spinnerMethod.getSelectedItemPosition()];

                    if (validateShippingInput(orderIdStr, address, personName, expectedDeliveryStr)) {
                        try {
                            int orderId = Integer.parseInt(orderIdStr);
                            Date expectedDelivery = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                    .parse(expectedDeliveryStr);

                            Shipping shipping = new Shipping(orderId, address, method, personName,
                                    expectedDelivery, description);

                            new AddShippingTask().execute(shipping);

                        } catch (Exception e) {
                            Toast.makeText(this, "Lỗi định dạng dữ liệu", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
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

        String[] statusArray = {"pending", "preparing", "shipping", "delivered", "cancelled"};
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
                .show();
    }

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
            Integer.parseInt(orderId);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Mã đơn hàng phải là số", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    // AsyncTask classes
    private class LoadShippingsTask extends AsyncTask<Void, Void, List<Shipping>> {
        @Override
        protected List<Shipping> doInBackground(Void... voids) {
            return shippingRepository.getAllShippings();
        }

        @Override
        protected void onPostExecute(List<Shipping> shippings) {
            shippingList.clear();
            shippingList.addAll(shippings);
            shippingAdapter.notifyDataSetChanged();

            Toast.makeText(ShippingActivity.this,
                    "Đã tải " + shippings.size() + " chuyến giao hàng", Toast.LENGTH_SHORT).show();
        }
    }

    private class FilterShippingsTask extends AsyncTask<String, Void, List<Shipping>> {
        @Override
        protected List<Shipping> doInBackground(String... status) {
            return shippingRepository.getShippingsByStatus(status[0]);
        }

        @Override
        protected void onPostExecute(List<Shipping> shippings) {
            shippingList.clear();
            shippingList.addAll(shippings);
            shippingAdapter.notifyDataSetChanged();
        }
    }

    private class AddShippingTask extends AsyncTask<Shipping, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Shipping... shippings) {
            return shippingRepository.createShipping(shippings[0]);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(ShippingActivity.this, "Thêm chuyến giao hàng thành công", Toast.LENGTH_SHORT).show();
                loadShippings();
            } else {
                Toast.makeText(ShippingActivity.this, "Thêm chuyến giao hàng thất bại", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class UpdateShippingTask extends AsyncTask<Shipping, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Shipping... shippings) {
            return shippingRepository.updateShipping(shippings[0]);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(ShippingActivity.this, "Cập nhật thông tin giao hàng thành công", Toast.LENGTH_SHORT).show();
                loadShippings();
            } else {
                Toast.makeText(ShippingActivity.this, "Cập nhật thông tin giao hàng thất bại", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class TrackShippingTask extends AsyncTask<String, Void, Shipping> {
        @Override
        protected Shipping doInBackground(String... trackingNumbers) {
            return shippingRepository.getShippingByTrackingNumber(trackingNumbers[0]);
        }

        @Override
        protected void onPostExecute(Shipping shipping) {
            if (shipping != null) {
                showShippingDetailsDialog(shipping);
            } else {
                Toast.makeText(ShippingActivity.this, "Không tìm thấy thông tin với mã tracking này", Toast.LENGTH_SHORT).show();
            }
        }
    }
}