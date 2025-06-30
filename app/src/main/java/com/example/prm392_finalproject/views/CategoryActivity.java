package com.example.prm392_finalproject.views;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.controllers.CategoryController;
import com.example.prm392_finalproject.models.CategoryRequest;
import com.example.prm392_finalproject.models.CategoryResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.text.Normalizer;

public class CategoryActivity extends AppCompatActivity {
    private RecyclerView rvCategoryList;
    private FloatingActionButton fabAddCategory;
    private Button btnBackToHome;
    private CategoryController categoryController;
    private CategoryAdapter categoryAdapter;
    private List<CategoryResponse> categoryList = new ArrayList<>();
    private TextInputEditText etSearch;
    private List<CategoryResponse> fullCategoryList = new ArrayList<>();
    private static String searchKeyword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_category);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rvCategoryList = findViewById(R.id.rvCategoryList);
        fabAddCategory = findViewById(R.id.fabAddCategory);
        btnBackToHome = findViewById(R.id.btnBackToHome);
        etSearch = findViewById(R.id.etSearch);
        btnBackToHome.setOnClickListener(v -> {
            Intent intent = new Intent(CategoryActivity.this, com.example.prm392_finalproject.MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        categoryController = new CategoryController();
        categoryAdapter = new CategoryAdapter(categoryList, new CategoryAdapter.OnCategoryActionListener() {
            @Override
            public void onEdit(CategoryResponse category) {
                showCategoryDialog(category);
            }
            @Override
            public void onDelete(CategoryResponse category) {
                confirmDeleteCategory(category);
            }
        });
        rvCategoryList.setLayoutManager(new LinearLayoutManager(this));
        rvCategoryList.setAdapter(categoryAdapter);

        fabAddCategory.setOnClickListener(v -> showCategoryDialog(null));
        loadCategories();

        // Xử lý search realtime
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCategories(s.toString());
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void loadCategories() {
        new Thread(() -> {
            List<CategoryResponse> categories = categoryController.getAllCategories();
            if (categories == null) {
                categories = new ArrayList<>();
            }
            categories.sort(Comparator.comparingInt(CategoryResponse::getCategory_id).reversed());
            List<CategoryResponse> finalCategories = categories;
            runOnUiThread(() -> {
                fullCategoryList.clear();
                fullCategoryList.addAll(finalCategories);
                categoryList.clear();
                categoryList.addAll(finalCategories);
                categoryAdapter.notifyDataSetChanged();
            });
        }).start();
    }


    private void filterCategories(String keyword) {
        searchKeyword = keyword;
        categoryList.clear();
        if (keyword == null || keyword.trim().isEmpty()) {
            categoryList.addAll(fullCategoryList);
        } else {
            String lower = keyword.toLowerCase();
            String lowerNoAccent = removeAccent(lower);
            for (CategoryResponse c : fullCategoryList) {
                String name = c.getName() != null ? c.getName() : "";
                String desc = c.getDescription() != null ? c.getDescription() : "";
                String nameNoAccent = removeAccent(name.toLowerCase());
                String descNoAccent = removeAccent(desc.toLowerCase());
                if (nameNoAccent.contains(lowerNoAccent) || descNoAccent.contains(lowerNoAccent)) {
                    categoryList.add(c);
                }
            }
        }
        categoryAdapter.notifyDataSetChanged();
    }

    // Loại bỏ dấu tiếng Việt
    private static String removeAccent(String s) {
        if (s == null) return "";
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        return temp.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    private void showCategoryDialog(CategoryResponse category) {
        boolean isEdit = category != null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isEdit ? "Sửa danh mục" : "Thêm danh mục");
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_category, null);
        EditText etName = view.findViewById(R.id.etCategoryName);
        EditText etDescription = view.findViewById(R.id.etCategoryDescription);
        if (isEdit) {
            etName.setText(category.getName());
            etDescription.setText(category.getDescription());
        }
        builder.setView(view);
        builder.setPositiveButton(isEdit ? "Cập nhật" : "Thêm", null);
        builder.setNegativeButton("Hủy", null);
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            Button btn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btn.setOnClickListener(v -> {
                String name = etName.getText().toString().trim();
                String desc = etDescription.getText().toString().trim();
                boolean hasError = false;
                if (TextUtils.isEmpty(name)) {
                    etName.setError("Không được để trống tên");
                    hasError = true;
                }
                if (TextUtils.isEmpty(desc)) {
                    etDescription.setError("Không được để trống mô tả");
                    hasError = true;
                }
                if (!isEdit) {
                    for (CategoryResponse c : categoryList) {
                        if (c.getName().equalsIgnoreCase(name)) {
                            etName.setError("Tên danh mục đã tồn tại");
                            hasError = true;
                            break;
                        }
                    }
                } else {
                    for (CategoryResponse c : categoryList) {
                        if (c.getName().equalsIgnoreCase(name) && c.getCategory_id() != category.getCategory_id()) {
                            etName.setError("Tên danh mục đã tồn tại");
                            hasError = true;
                            break;
                        }
                    }
                }
                if (hasError) return;
                new Thread(() -> {
                    if (isEdit) {
                        categoryController.updateCategory(new CategoryRequest(name, desc), category.getCategory_id());
                    } else {
                        categoryController.addCategory(new CategoryRequest(name, desc));
                    }
                    runOnUiThread(() -> {
                        loadCategories();
                        dialog.dismiss();
                    });
                }).start();
            });
        });
        dialog.show();
    }

    private void confirmDeleteCategory(CategoryResponse category) {
        new AlertDialog.Builder(this)
            .setTitle("Xóa danh mục")
            .setMessage("Bạn có chắc muốn xóa danh mục này?")
            .setPositiveButton("Xóa", (dialog, which) -> {
                new Thread(() -> {
                    categoryController.deleteCategory(category.getCategory_id());
                    runOnUiThread(this::loadCategories);
                }).start();
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    // Adapter cho RecyclerView
    public static class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
        public interface OnCategoryActionListener {
            void onEdit(CategoryResponse category);
            void onDelete(CategoryResponse category);
        }
        private List<CategoryResponse> categories;
        private OnCategoryActionListener listener;
        public CategoryAdapter(List<CategoryResponse> categories, OnCategoryActionListener listener) {
            this.categories = categories;
            this.listener = listener;
        }
        @NonNull
        @Override
        public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
            return new CategoryViewHolder(view);
        }
        @Override
        public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
            CategoryResponse category = categories.get(position);
            // Highlight từ khóa trong tên và mô tả
            holder.tvName.setText(getHighlightedText(category.getName(), searchKeyword));
            holder.tvDescription.setText(getHighlightedText(category.getDescription(), searchKeyword));
            holder.btnEdit.setOnClickListener(v -> listener.onEdit(category));
            holder.btnDelete.setOnClickListener(v -> listener.onDelete(category));
        }
        @Override
        public int getItemCount() {
            return categories.size();
        }
        static class CategoryViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvDescription;
            ImageButton btnEdit, btnDelete;
            public CategoryViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvCategoryName);
                tvDescription = itemView.findViewById(R.id.tvCategoryDescription);
                btnEdit = itemView.findViewById(R.id.btnEditCategory);
                btnDelete = itemView.findViewById(R.id.btnDeleteCategory);
            }
        }
        // Highlight từ khóa search trong text
        private Spannable getHighlightedText(String text, String keyword) {
            if (text == null || keyword == null || keyword.trim().isEmpty()) return new SpannableString(text == null ? "" : text);
            String textNoAccent = removeAccent(text.toLowerCase());
            String keywordNoAccent = removeAccent(keyword.toLowerCase());
            int start = textNoAccent.indexOf(keywordNoAccent);
            SpannableString spannable = new SpannableString(text);
            if (start >= 0) {
                spannable.setSpan(new ForegroundColorSpan(0xFFFF9800), start, start + keyword.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            return spannable;
        }
    }
}