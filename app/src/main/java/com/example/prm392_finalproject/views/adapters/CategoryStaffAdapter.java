package com.example.prm392_finalproject.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.models.CategoryResponse;

import java.util.ArrayList;
import java.util.List;

public class CategoryStaffAdapter extends RecyclerView.Adapter<CategoryStaffAdapter.CategoryViewHolder> {

    private Context context;
    private List<CategoryResponse> categoryList;
    private List<CategoryResponse> categoryListFull; // For search functionality

    public CategoryStaffAdapter(Context context, List<CategoryResponse> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
        this.categoryListFull = new ArrayList<>(categoryList);
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category_staff, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryResponse category = categoryList.get(position);

        holder.tvCategoryId.setText("ID: " + category.getCategory_id());
        holder.tvCategoryName.setText(category.getName());

        // Description
        if (category.getDescription() != null && !category.getDescription().trim().isEmpty()) {
            holder.tvCategoryDescription.setText(category.getDescription());
            holder.tvCategoryDescription.setVisibility(View.VISIBLE);
        } else {
            holder.tvCategoryDescription.setText("Không có mô tả");
            holder.tvCategoryDescription.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
        }

        // Status
        if (category.isIs_deleted()) {
            holder.tvStatus.setText("Đã xóa");
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            holder.tvStatus.setBackgroundColor(context.getResources().getColor(android.R.color.holo_red_light));
        } else {
            holder.tvStatus.setText("Hoạt động");
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
            holder.tvStatus.setBackgroundColor(context.getResources().getColor(android.R.color.holo_green_light));
        }
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    /**
     * Filter categories based on search query
     */
    public void filter(String text) {
        categoryList.clear();
        if (text.isEmpty()) {
            categoryList.addAll(categoryListFull);
        } else {
            text = text.toLowerCase().trim();
            for (CategoryResponse category : categoryListFull) {
                if (category.getName().toLowerCase().contains(text) ||
                        (category.getDescription() != null &&
                                category.getDescription().toLowerCase().contains(text)) ||
                        String.valueOf(category.getCategory_id()).contains(text)) {
                    categoryList.add(category);
                }
            }
        }
        notifyDataSetChanged();
    }

    /**
     * Update adapter data
     */
    public void updateData(List<CategoryResponse> newCategoryList) {
        this.categoryList.clear();
        this.categoryList.addAll(newCategoryList);
        this.categoryListFull.clear();
        this.categoryListFull.addAll(newCategoryList);
        notifyDataSetChanged();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryId, tvCategoryName, tvCategoryDescription, tvStatus;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);

            tvCategoryId = itemView.findViewById(R.id.tvCategoryId);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvCategoryDescription = itemView.findViewById(R.id.tvCategoryDescription);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
