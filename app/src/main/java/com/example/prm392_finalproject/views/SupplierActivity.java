package com.example.prm392_finalproject.views;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.controllers.SupplierController;
import com.example.prm392_finalproject.models.SupplierRequest;
import com.example.prm392_finalproject.models.SupplierResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.chip.Chip;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SupplierActivity extends AppCompatActivity {
    private RecyclerView rvSupplierList;
    private FloatingActionButton fabAddSupplier;
    private TextInputEditText etSearchSupplier;
    private SupplierController supplierController;
    private SupplierAdapter supplierAdapter;
    private List<SupplierResponse> supplierList = new ArrayList<>();
    private List<SupplierResponse> fullSupplierList = new ArrayList<>();
    private static String searchKeyword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supplier);

        rvSupplierList = findViewById(R.id.rvSupplierList);
        fabAddSupplier = findViewById(R.id.fabAddSupplier);
        etSearchSupplier = findViewById(R.id.etSearchSupplier);
        supplierController = new SupplierController();

        supplierAdapter = new SupplierAdapter(supplierList, supplier -> showSupplierDialog(supplier));
        rvSupplierList.setLayoutManager(new LinearLayoutManager(this));
        rvSupplierList.setAdapter(supplierAdapter);

        fabAddSupplier.setOnClickListener(v -> showSupplierDialog(null));

        etSearchSupplier.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSuppliers(s.toString());
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        loadSuppliers();
    }

    private void loadSuppliers() {
        new Thread(() -> {
            List<SupplierResponse> suppliers = supplierController.getAllSuppliers();
            suppliers.sort(Comparator.comparingInt(SupplierResponse::getSupplier_id).reversed());
            runOnUiThread(() -> {
                fullSupplierList.clear();
                fullSupplierList.addAll(suppliers);
                supplierList.clear();
                supplierList.addAll(suppliers);
                supplierAdapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void filterSuppliers(String keyword) {
        searchKeyword = keyword;
        supplierList.clear();
        if (keyword == null || keyword.trim().isEmpty()) {
            supplierList.addAll(fullSupplierList);
        } else {
            String lower = keyword.toLowerCase();
            String lowerNoAccent = removeAccent(lower);
            for (SupplierResponse s : fullSupplierList) {
                String name = s.getName() != null ? s.getName() : "";
                String phone = s.getPhone() != null ? s.getPhone() : "";
                String email = s.getEmail() != null ? s.getEmail() : "";
                String nameNoAccent = removeAccent(name.toLowerCase());
                String phoneNoAccent = removeAccent(phone.toLowerCase());
                String emailNoAccent = removeAccent(email.toLowerCase());
                if (nameNoAccent.contains(lowerNoAccent) || phoneNoAccent.contains(lowerNoAccent) || emailNoAccent.contains(lowerNoAccent)) {
                    supplierList.add(s);
                }
            }
        }
        supplierAdapter.notifyDataSetChanged();
    }

    private static String removeAccent(String s) {
        if (s == null) return "";
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        return temp.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    private boolean isValidEmail(String email) {
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPhone(String phone) {
        return phone != null && phone.matches("[0-9]{9,11}");
    }

    private boolean isValidName(String name) {
        return name != null && name.matches("[a-zA-ZÀ-ỹ0-9\\s]+") && !name.trim().isEmpty();
    }

    private boolean isDuplicate(String name, String email, String phone, Integer exceptId) {
        String nameNoAccent = removeAccent(name).toLowerCase();
        String emailLower = email.toLowerCase();
        for (SupplierResponse s : fullSupplierList) {
            if (exceptId != null && s.getSupplier_id() == exceptId) continue;
            if (removeAccent(s.getName()).toLowerCase().equals(nameNoAccent)
                    || s.getEmail().equalsIgnoreCase(emailLower)
                    || s.getPhone().equals(phone)) {
                return true;
            }
        }
        return false;
    }

    private void showSupplierDialog(SupplierResponse supplier) {
        boolean isEdit = supplier != null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isEdit ? "Sửa nhà cung cấp" : "Thêm nhà cung cấp");
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_supplier, null);
        TextInputEditText etName = view.findViewById(R.id.etSupplierName);
        TextInputEditText etAddress = view.findViewById(R.id.etSupplierAddress);
        TextInputEditText etContact = view.findViewById(R.id.etSupplierContact);
        TextInputEditText etPhone = view.findViewById(R.id.etSupplierPhone);
        TextInputEditText etEmail = view.findViewById(R.id.etSupplierEmail);
        SwitchMaterial switchActive = view.findViewById(R.id.switchSupplierActive);

        if (isEdit) {
            etName.setText(supplier.getName());
            etAddress.setText(supplier.getAddress());
            etContact.setText(supplier.getContact_info());
            etPhone.setText(supplier.getPhone());
            etEmail.setText(supplier.getEmail());
            switchActive.setChecked(supplier.isIs_active());
        }

        builder.setView(view);
        builder.setPositiveButton(isEdit ? "Cập nhật" : "Thêm", null);
        builder.setNegativeButton("Hủy", null);
        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(d -> {
            android.widget.Button btn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btn.setOnClickListener(v -> {
                String name = etName.getText().toString().trim();
                String address = etAddress.getText().toString().trim();
                String contact = etContact.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                boolean isActive = switchActive.isChecked();

                boolean hasError = false;
                if (!isValidName(name)) {
                    etName.setError("Tên không hợp lệ hoặc để trống");
                    hasError = true;
                }
                if (!isValidPhone(phone)) {
                    etPhone.setError("SĐT phải là số, 9-11 ký tự");
                    hasError = true;
                }
                if (!isValidEmail(email)) {
                    etEmail.setError("Email không hợp lệ");
                    hasError = true;
                }
                if (isDuplicate(name, email, phone, isEdit ? supplier.getSupplier_id() : null)) {
                    etName.setError("Tên, email hoặc SĐT đã tồn tại");
                    etEmail.setError("Tên, email hoặc SĐT đã tồn tại");
                    etPhone.setError("Tên, email hoặc SĐT đã tồn tại");
                    hasError = true;
                }
                if (hasError) return;

                new Thread(() -> {
                    try {
                        if (isEdit) {
                            supplierController.updateSupplier(new SupplierRequest(name, address, contact, phone, email, isActive), supplier.getSupplier_id());
                        } else {
                            supplierController.addSupplier(new SupplierRequest(name, address, contact, phone, email, isActive));
                        }
                        runOnUiThread(() -> {
                            loadSuppliers();
                            dialog.dismiss();
                            Toast.makeText(this, isEdit ? "Cập nhật thành công" : "Thêm thành công", Toast.LENGTH_SHORT).show();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(this, "Lỗi thao tác dữ liệu!", Toast.LENGTH_LONG).show());
                    }
                }).start();
            });
        });

        dialog.show();
    }

    // --- RecyclerView Adapter ---
    public static class SupplierAdapter extends RecyclerView.Adapter<SupplierAdapter.SupplierViewHolder> {
        public interface OnSupplierActionListener {
            void onEdit(SupplierResponse supplier);
        }

        private final List<SupplierResponse> suppliers;
        private final OnSupplierActionListener listener;

        public SupplierAdapter(List<SupplierResponse> suppliers, OnSupplierActionListener listener) {
            this.suppliers = suppliers;
            this.listener = listener;
        }

        @NonNull
        @Override
        public SupplierViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_supplier, parent, false);
            return new SupplierViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SupplierViewHolder holder, int position) {
            SupplierResponse supplier = suppliers.get(position);
            holder.tvName.setText(getHighlightedText(supplier.getName(), searchKeyword));
            holder.tvPhone.setText("SĐT: " + supplier.getPhone());
            holder.tvEmail.setText("Email: " + supplier.getEmail());

            if (supplier.isIs_active()) {
                holder.chipStatus.setText("Đang hoạt động");
                holder.chipStatus.setChipBackgroundColorResource(R.color.accent);
            } else {
                holder.chipStatus.setText("Ngừng hoạt động");
                holder.chipStatus.setChipBackgroundColorResource(R.color.accent);
            }

            holder.btnEdit.setOnClickListener(v -> listener.onEdit(supplier));
        }

        @Override
        public int getItemCount() {
            return suppliers.size();
        }

        static class SupplierViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvPhone, tvEmail;
            Chip chipStatus;
            ImageButton btnEdit;

            public SupplierViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvSupplierName);
                tvPhone = itemView.findViewById(R.id.tvSupplierPhone);
                tvEmail = itemView.findViewById(R.id.tvSupplierEmail);
                chipStatus = itemView.findViewById(R.id.chipSupplierStatus);
                btnEdit = itemView.findViewById(R.id.btnEditSupplier);
            }
        }

        private CharSequence getHighlightedText(String text, String keyword) {
            if (text == null || keyword == null || keyword.trim().isEmpty()) return text == null ? "" : text;
            String textNoAccent = removeAccent(text.toLowerCase());
            String keywordNoAccent = removeAccent(keyword.toLowerCase());
            int start = textNoAccent.indexOf(keywordNoAccent);
            android.text.SpannableString spannable = new android.text.SpannableString(text);
            if (start >= 0) {
                spannable.setSpan(new android.text.style.ForegroundColorSpan(0xFFFF9800),
                        start, start + keyword.length(),
                        android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            return spannable;
        }
    }
}
