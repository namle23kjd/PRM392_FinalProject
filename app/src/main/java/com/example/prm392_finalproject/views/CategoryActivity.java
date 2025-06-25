package com.example.prm392_finalproject.views;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CategoryActivity extends AppCompatActivity {
    private RecyclerView rvCategoryList;
    private FloatingActionButton fabAddCategory;
    private CategoryController categoryController;
    private CategoryAdapter categoryAdapter;
    private List<CategoryResponse> categoryList = new ArrayList<>();

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
    }

    private void loadCategories() {
        new Thread(() -> {
            List<CategoryResponse> categories = categoryController.getAllCategories();
            categories.sort(Comparator.comparingInt(CategoryResponse::getCategory_id).reversed());
            runOnUiThread(() -> {
                categoryList.clear();
                categoryList.addAll(categories);
                categoryAdapter.notifyDataSetChanged();
            });
        }).start();
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
            holder.tvName.setText(category.getName());
            holder.tvDescription.setText(category.getDescription());
            holder.btnEdit.setOnClickListener(v -> listener.onEdit(category));
            holder.btnDelete.setOnClickListener(v -> listener.onDelete(category));
        }
        @Override
        public int getItemCount() {
            return categories.size();
        }
        static class CategoryViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvDescription;
            Button btnEdit, btnDelete;
            public CategoryViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvCategoryName);
                tvDescription = itemView.findViewById(R.id.tvCategoryDescription);
                btnEdit = itemView.findViewById(R.id.btnEditCategory);
                btnDelete = itemView.findViewById(R.id.btnDeleteCategory);
            }
        }
    }
}