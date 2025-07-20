package com.example.prm392_finalproject.views.adapters;

import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.models.Product;

import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;
    private OnProductClickListener listener;
    private OnAddToCartClickListener addToCartListener;


    private OnProductLongClickListener longClickListener;


    public interface OnProductClickListener {
        void onProductClick(Product product);
    }
    public interface OnAddToCartClickListener {
        void onAddToCartClick(Product product);
    }

    public ProductAdapter(List<Product> productList, OnProductClickListener listener, OnAddToCartClickListener addToCartListener) {
        this.productList = productList;
        this.listener = listener;
        this.addToCartListener = addToCartListener;

    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private ImageView ivProductImage;
        private TextView tvProductName;
        private TextView tvProductPrice;
        private TextView tvProductDescription;
        private TextView tvProductCategory;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardProduct);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductDescription = itemView.findViewById(R.id.tvProductDescription);
            tvProductCategory = itemView.findViewById(R.id.tvProductCategory);

            // Nút thêm vào giỏ hàng
            ImageView btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
            btnAddToCart.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (addToCartListener != null && pos != RecyclerView.NO_POSITION) {
                    addToCartListener.onAddToCartClick(productList.get(pos));
                }
            });

            // 👇 Nhấn giữ để cập nhật
            itemView.setOnLongClickListener(v -> {
                int pos = getAdapterPosition();
                if (longClickListener != null && pos != RecyclerView.NO_POSITION) {
                    v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    longClickListener.onProductLongClick(productList.get(pos));
                }
                return true;
            });
        }

        

        public void bind(Product product) {
            tvProductName.setText(product.getName());
            tvProductPrice.setText(String.format(Locale.getDefault(), "$%.2f", product.getPrice()));
            tvProductDescription.setText(product.getDescription());
            tvProductCategory.setText(product.getCategory());
            // In log để kiểm tra URL ảnh
            Log.d("ProductAdapter", "Image URL for product \"" + product.getName() + "\": " + product.getImageUrl());
            // Set default image for now (you can load actual images later)
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(product.getImageUrl())
                        .placeholder(R.drawable.ic_menu_business) // hoặc ảnh khác
                        .error(R.drawable.ic_menu_business)               // hoặc ảnh khác
                        .into(ivProductImage);
            } else {
                ivProductImage.setImageResource(R.drawable.ic_menu_business);
            }

            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });


            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });



        }

    }
    public interface OnProductLongClickListener {
        void onProductLongClick(Product product);
    }

}