
package com.example.prm392_finalproject.views;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.dao.DiscountDAO;
import com.example.prm392_finalproject.models.Discount;
import com.example.prm392_finalproject.models.DiscountRequest;
import com.example.prm392_finalproject.views.adapters.DiscountAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DiscountManagementActivity extends AppCompatActivity implements DiscountAdapter.OnDiscountClickListener {

    private RecyclerView recyclerViewDiscounts;
    private TextView tvEmpty;
    private FloatingActionButton fabAddDiscount;
    private DiscountAdapter adapter;
    private DiscountDAO discountDAO;
    private List<Discount> discountList;
    private List<Discount> allDiscountsList;
    private ProgressBar progressBarLoading;
    
    // Pagination variables
    private static final int ITEMS_PER_PAGE = 5;
    private int currentPage = 0;
    private int totalPages = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discount_management);

        // Initialize views
        recyclerViewDiscounts = findViewById(R.id.recyclerViewDiscounts);
        tvEmpty = findViewById(R.id.tvEmpty);
        fabAddDiscount = findViewById(R.id.fabAddDiscount);
        progressBarLoading = findViewById(R.id.progressBarLoading);
        
        // Initialize pagination views
        LinearLayout paginationLayout = findViewById(R.id.paginationLayout);
        Button btnPrevious = findViewById(R.id.btnPrevious);
        Button btnNext = findViewById(R.id.btnNext);
        TextView tvPageInfo = findViewById(R.id.tvPageInfo);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Discount Management");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize DAO
        discountDAO = new DiscountDAO();

        // Debug: Check table existence
        new Thread(() -> {
            boolean tableExists = discountDAO.checkTableExists();
            int count = discountDAO.getDiscountCount();
            runOnUiThread(() -> {
                Log.d("DiscountManagement", "Table exists: " + tableExists + ", Count: " + count);
                if (!tableExists) {
                    Toast.makeText(this, "Discount table does not exist in database", Toast.LENGTH_LONG).show();
                } else if (count == 0) {
                    Toast.makeText(this, "No discounts found in database", Toast.LENGTH_LONG).show();
                }
            });
        }).start();

        // Setup RecyclerView
        discountList = new ArrayList<>();
        allDiscountsList = new ArrayList<>();
        adapter = new DiscountAdapter(discountList, this);
        recyclerViewDiscounts.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewDiscounts.setAdapter(adapter);
        
        // Setup pagination click listeners
        btnPrevious.setOnClickListener(v -> {
            if (currentPage > 0) {
                currentPage--;
                updatePagination();
            }
        });
        
        btnNext.setOnClickListener(v -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
                updatePagination();
            }
        });

        // Setup click listeners
        fabAddDiscount.setOnClickListener(v -> {
            Log.d("DiscountManagement", "FAB clicked");
            showAddEditDialog(null);
        });

        // Load discounts
        loadDiscounts();
    }

    private void loadDiscounts() {
        runOnUiThread(() -> {
            progressBarLoading.setVisibility(View.VISIBLE);
            recyclerViewDiscounts.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.GONE);
            findViewById(R.id.paginationLayout).setVisibility(View.GONE);
        });
        new Thread(() -> {
            try {
                List<Discount> discounts = discountDAO.getAllDiscounts();
                runOnUiThread(() -> {
                    allDiscountsList.clear();
                    allDiscountsList.addAll(discounts);
                    currentPage = 0;
                    totalPages = (int) Math.ceil((double) allDiscountsList.size() / ITEMS_PER_PAGE);
                    updatePagination();
                    updateEmptyView();
                    progressBarLoading.setVisibility(View.GONE);
                });
            } catch (Exception e) {
                Log.e("DiscountManagement", "Error loading discounts: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error loading discounts: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    updateEmptyView();
                    progressBarLoading.setVisibility(View.GONE);
                });
            }
        }).start();
    }

    private void updatePagination() {
        // Calculate start and end indices for current page
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, allDiscountsList.size());
        
        // Update discount list for current page
        discountList.clear();
        if (startIndex < allDiscountsList.size()) {
            discountList.addAll(allDiscountsList.subList(startIndex, endIndex));
        }
        adapter.notifyDataSetChanged();
        
        // Update pagination controls
        LinearLayout paginationLayout = findViewById(R.id.paginationLayout);
        Button btnPrevious = findViewById(R.id.btnPrevious);
        Button btnNext = findViewById(R.id.btnNext);
        TextView tvPageInfo = findViewById(R.id.tvPageInfo);
        
        if (totalPages > 1) {
            paginationLayout.setVisibility(View.VISIBLE);
            btnPrevious.setEnabled(currentPage > 0);
            btnNext.setEnabled(currentPage < totalPages - 1);
            tvPageInfo.setText(String.format("Page %d of %d", currentPage + 1, totalPages));
        } else {
            paginationLayout.setVisibility(View.GONE);
        }
        
        updateEmptyView();
    }

    private void updateEmptyView() {
        if (allDiscountsList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerViewDiscounts.setVisibility(View.GONE);
            findViewById(R.id.paginationLayout).setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerViewDiscounts.setVisibility(View.VISIBLE);
        }
    }

    private void showAddEditDialog(Discount discount) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_discount, null);
            builder.setView(dialogView);

            // Initialize dialog views
            Spinner spinnerDiscountType = dialogView.findViewById(R.id.spinnerDiscountType);
            TextInputEditText etDiscountValue = dialogView.findViewById(R.id.etDiscountValue);
            Button btnStartDate = dialogView.findViewById(R.id.btnStartDate);
            Button btnStartTime = dialogView.findViewById(R.id.btnStartTime);
            Button btnEndDate = dialogView.findViewById(R.id.btnEndDate);
            Button btnEndTime = dialogView.findViewById(R.id.btnEndTime);
            TextView tvStartDateTime = dialogView.findViewById(R.id.tvStartDateTime);
            TextView tvEndDateTime = dialogView.findViewById(R.id.tvEndDateTime);
            Button btnCancel = dialogView.findViewById(R.id.btnCancel);
            Button btnSave = dialogView.findViewById(R.id.btnSave);

            // Date and time variables
            LocalDate[] selectedStartDate = {LocalDate.now()};
            LocalTime[] selectedStartTime = {LocalTime.now()};
            LocalDate[] selectedEndDate = {LocalDate.now().plusDays(30)};
            LocalTime[] selectedEndTime = {LocalTime.now()};

            // Setup discount type spinner
            String[] discountTypes = {"Percentage", "Amount"};
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, discountTypes);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerDiscountType.setAdapter(spinnerAdapter);

            // Update display
            updateDateTimeDisplay(tvStartDateTime, selectedStartDate[0], selectedStartTime[0]);
            updateDateTimeDisplay(tvEndDateTime, selectedEndDate[0], selectedEndTime[0]);

            // Date pickers
            btnStartDate.setOnClickListener(v -> {
                Calendar calendar = Calendar.getInstance();
                new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                    selectedStartDate[0] = LocalDate.of(year, month + 1, dayOfMonth);
                    updateDateTimeDisplay(tvStartDateTime, selectedStartDate[0], selectedStartTime[0]);
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            });

            btnStartTime.setOnClickListener(v -> {
                Calendar calendar = Calendar.getInstance();
                new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                    selectedStartTime[0] = LocalTime.of(hourOfDay, minute);
                    updateDateTimeDisplay(tvStartDateTime, selectedStartDate[0], selectedStartTime[0]);
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
            });

            btnEndDate.setOnClickListener(v -> {
                Calendar calendar = Calendar.getInstance();
                new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                    selectedEndDate[0] = LocalDate.of(year, month + 1, dayOfMonth);
                    updateDateTimeDisplay(tvEndDateTime, selectedEndDate[0], selectedEndTime[0]);
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            });

            btnEndTime.setOnClickListener(v -> {
                Calendar calendar = Calendar.getInstance();
                new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                    selectedEndTime[0] = LocalTime.of(hourOfDay, minute);
                    updateDateTimeDisplay(tvEndDateTime, selectedEndDate[0], selectedEndTime[0]);
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
            });

            // Pre-fill data if editing
            if (discount != null) {
                // Set selected discount type in spinner
                String discountType = discount.getDiscount_type();
                for (int i = 0; i < discountTypes.length; i++) {
                    if (discountTypes[i].equals(discountType)) {
                        spinnerDiscountType.setSelection(i);
                        break;
                    }
                }
                etDiscountValue.setText(String.valueOf(discount.getDiscount_value()));
                if (discount.getStart_date() != null) {
                    selectedStartDate[0] = discount.getStart_date().toLocalDate();
                    selectedStartTime[0] = discount.getStart_date().toLocalTime();
                    updateDateTimeDisplay(tvStartDateTime, selectedStartDate[0], selectedStartTime[0]);
                }
                if (discount.getEnd_date() != null) {
                    selectedEndDate[0] = discount.getEnd_date().toLocalDate();
                    selectedEndTime[0] = discount.getEnd_date().toLocalTime();
                    updateDateTimeDisplay(tvEndDateTime, selectedEndDate[0], selectedEndTime[0]);
                }
            }

            AlertDialog dialog = builder.create();

            btnCancel.setOnClickListener(v -> dialog.dismiss());

            btnSave.setOnClickListener(v -> {
                // Validate form
                String type = spinnerDiscountType.getSelectedItem().toString();
                String valueStr = etDiscountValue.getText().toString().trim();

                // Clear previous errors
                etDiscountValue.setError(null);

                // Validation
                boolean isValid = true;

                // No need to validate discount type since it's selected from predefined list

                // Validate discount value
                if (valueStr.isEmpty()) {
                    etDiscountValue.setError("Discount value is required");
                    isValid = false;
                } else {
                    try {
                        double value = Double.parseDouble(valueStr);
                        if (value <= 0) {
                            etDiscountValue.setError("Discount value must be greater than 0");
                            isValid = false;
                        } else if (value > 1000000) {
                            etDiscountValue.setError("Discount value is too high");
                            isValid = false;
                        } else {
                            // Additional validation for percentage discounts
                            if (type.equals("Percentage")) {
                                if (value > 100) {
                                    etDiscountValue.setError("Percentage discount cannot exceed 100%");
                                    isValid = false;
                                }
                            }
                        }
                    } catch (NumberFormatException e) {
                        etDiscountValue.setError("Invalid discount value format");
                        isValid = false;
                    }
                }

                // Validate dates
                LocalDateTime startDateTime = LocalDateTime.of(selectedStartDate[0], selectedStartTime[0]);
                LocalDateTime endDateTime = LocalDateTime.of(selectedEndDate[0], selectedEndTime[0]);

                if (endDateTime.isBefore(startDateTime)) {
                    Toast.makeText(this, "End date must be after start date", Toast.LENGTH_SHORT).show();
                    isValid = false;
                }

                if (!isValid) {
                    return;
                }

                // Show loading state
                btnSave.setEnabled(false);
                btnSave.setText("Saving...");
                btnCancel.setEnabled(false);
                
                // Disable input fields during loading
                spinnerDiscountType.setEnabled(false);
                etDiscountValue.setEnabled(false);
                btnStartDate.setEnabled(false);
                btnStartTime.setEnabled(false);
                btnEndDate.setEnabled(false);
                btnEndTime.setEnabled(false);

                try {
                    double value = Double.parseDouble(valueStr);

                    if (discount == null) {
                        // Add new discount
                        DiscountRequest request = new DiscountRequest(type, value, startDateTime, endDateTime);
                        new Thread(() -> {
                            try {
                                discountDAO.addDiscount(request);
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "Discount added successfully", Toast.LENGTH_SHORT).show();
                                    loadDiscounts();
                                    dialog.dismiss();
                                });
                            } catch (Exception e) {
                                Log.e("DiscountManagement", "Error adding discount: " + e.getMessage(), e);
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "Error adding discount: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    // Reset button state on error
                                    btnSave.setEnabled(true);
                                    btnSave.setText("Save");
                                    btnCancel.setEnabled(true);
                                    
                                    // Re-enable input fields
                                    spinnerDiscountType.setEnabled(true);
                                    etDiscountValue.setEnabled(true);
                                    btnStartDate.setEnabled(true);
                                    btnStartTime.setEnabled(true);
                                    btnEndDate.setEnabled(true);
                                    btnEndTime.setEnabled(true);
                                });
                            }
                        }).start();
                    } else {
                        // Update existing discount
                        discount.setDiscount_type(type);
                        discount.setDiscount_value(value);
                        discount.setStart_date(startDateTime);
                        discount.setEnd_date(endDateTime);
                        new Thread(() -> {
                            try {
                                discountDAO.updateDiscount(discount);
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "Discount updated successfully", Toast.LENGTH_SHORT).show();
                                    loadDiscounts();
                                    dialog.dismiss();
                                });
                            } catch (Exception e) {
                                Log.e("DiscountManagement", "Error updating discount: " + e.getMessage(), e);
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "Error updating discount: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    // Reset button state on error
                                    btnSave.setEnabled(true);
                                    btnSave.setText("Save");
                                    btnCancel.setEnabled(true);
                                    
                                    // Re-enable input fields
                                    spinnerDiscountType.setEnabled(true);
                                    etDiscountValue.setEnabled(true);
                                    btnStartDate.setEnabled(true);
                                    btnStartTime.setEnabled(true);
                                    btnEndDate.setEnabled(true);
                                    btnEndTime.setEnabled(true);
                                });
                            }
                        }).start();
                    }
                } catch (NumberFormatException e) {
                    etDiscountValue.setError("Invalid discount value format");
                    // Reset button state on error
                    btnSave.setEnabled(true);
                    btnSave.setText("Save");
                    btnCancel.setEnabled(true);
                    
                    // Re-enable input fields
                    spinnerDiscountType.setEnabled(true);
                    etDiscountValue.setEnabled(true);
                    btnStartDate.setEnabled(true);
                    btnStartTime.setEnabled(true);
                    btnEndDate.setEnabled(true);
                    btnEndTime.setEnabled(true);
                }
            });

            dialog.show();
        } catch (Exception e) {
            Log.e("DiscountManagement", "Error showing dialog: " + e.getMessage(), e);
            Toast.makeText(this, "Error opening dialog: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void updateDateTimeDisplay(TextView textView, LocalDate date, LocalTime time) {
        textView.setText(String.format("%04d-%02d-%02d %02d:%02d", 
            date.getYear(), date.getMonthValue(), date.getDayOfMonth(),
            time.getHour(), time.getMinute()));
    }

    @Override
    public void onEditClick(Discount discount) {
        showAddEditDialog(discount);
    }

    @Override
    public void onDeleteClick(Discount discount) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_delete_discount, null);
        builder.setView(dialogView);

        TextView tvMessage = dialogView.findViewById(R.id.tvMessage);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnDelete = dialogView.findViewById(R.id.btnDelete);
        ProgressBar progressBar = dialogView.findViewById(R.id.progressBar);

        tvMessage.setText("Are you sure you want to delete this discount?");

        AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnDelete.setOnClickListener(v -> {
            // Show loading state
            btnDelete.setEnabled(false);
            btnDelete.setText("Deleting...");
            btnCancel.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);

            new Thread(() -> {
                boolean deleted = discountDAO.deleteDiscount(discount.getDiscount_id());
                runOnUiThread(() -> {
                    if (deleted) {
                        Toast.makeText(this, "Discount deleted successfully", Toast.LENGTH_SHORT).show();
                        loadDiscounts();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, "Cannot delete discount - it is currently being used in orders", Toast.LENGTH_LONG).show();
                        // Reset button state on error
                        btnDelete.setEnabled(true);
                        btnDelete.setText("Delete");
                        btnCancel.setEnabled(true);
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }).start();
        });

        dialog.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 