// ProductStaffAdapter.java - Đã loại bỏ Glide
package com.example.prm392_finalproject.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject.models.Product;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductStaffAdapter extends RecyclerView.Adapter<ProductStaffAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;
    private NumberFormat currencyFormat;

    public ProductStaffAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng layout simple_list_item_2 có sẵn trong Android
        View view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false);

        // Tùy chỉnh view để trông đẹp hơn
        view.setPadding(24, 20, 24, 20);
        view.setBackgroundColor(0xFFFFFFFF);

        // Tạo margin cho CardView effect
        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(16, 8, 16, 8);
        view.setLayoutParams(params);
        view.setElevation(6f); // Tạo shadow effect

        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        // Title: Tên sản phẩm + cảnh báo hết hàng
        String title = "📦 " + product.getName();
        int stockLevel = product.getQuantityInStock();

        if (stockLevel == 0) {
            title += " ⚠️ HẾT HÀNG";
            holder.title.setTextColor(0xFFE74C3C); // Màu đỏ
        } else if (stockLevel <= 5) {
            title += " ⚠️ SẮP HẾT";
            holder.title.setTextColor(0xFFF39C12); // Màu cam
        } else {
            title += " ✅";
            holder.title.setTextColor(0xFF27AE60); // Màu xanh
        }

        holder.title.setText(title);
        holder.title.setTextSize(16f);
        holder.title.setTypeface(null, android.graphics.Typeface.BOLD);

        // Subtitle: Thông tin chi tiết
        StringBuilder subtitle = new StringBuilder();

        // Dòng 1: Mã + Giá
        subtitle.append("🏷️ Mã: ").append(product.getProductCode() != null ? product.getProductCode() : "N/A");
        subtitle.append(" | 💰 ").append(currencyFormat.format(product.getPrice()));

        // Dòng 2: Số lượng tồn kho
        subtitle.append("\n📊 Tồn kho: ").append(product.getQuantityInStock())
                .append(" | 🏪 Kho: ").append(product.getStockQuantity());

        // Dòng 3: Thể loại
        if (product.getCategory() != null && !product.getCategory().trim().isEmpty()) {
            subtitle.append("\n📂 Loại: ").append(product.getCategory());
        } else {
            subtitle.append("\n📂 Loại: Chưa phân loại");
        }

        // Dòng 4: Trạng thái
        subtitle.append(" | ");
        if (product.isActive()) {
            subtitle.append("🟢 Hoạt động");
        } else {
            subtitle.append("🔴 Không hoạt động");
        }

        // Dòng 5: Mô tả (nếu có)
        if (product.getDescription() != null && !product.getDescription().trim().isEmpty()) {
            String desc = product.getDescription();
            if (desc.length() > 50) {
                desc = desc.substring(0, 50) + "...";
            }
            subtitle.append("\n📝 ").append(desc);
        }

        // Thông tin bổ sung (nếu có)
        if (product.getWeight() != null) {
            subtitle.append("\n⚖️ ").append(product.getWeight()).append("kg");
        }

        if (product.getOriginCountry() != null && !product.getOriginCountry().trim().isEmpty()) {
            subtitle.append(" | 🌍 ").append(product.getOriginCountry());
        }

        holder.subtitle.setText(subtitle.toString());
        holder.subtitle.setTextSize(12f);
        holder.subtitle.setTextColor(0xFF34495E);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView subtitle;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(android.R.id.text1);
            subtitle = itemView.findViewById(android.R.id.text2);
        }
    }
}