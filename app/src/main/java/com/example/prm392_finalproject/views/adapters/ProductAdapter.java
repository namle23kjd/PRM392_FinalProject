package com.example.prm392_finalproject.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.models.Product;

import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    
    private List<Product> productList;
    private OnProductClickListener listener;
    private OnAddToCartClickListener addToCartListener;
    private boolean isReadOnly;

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
        this.isReadOnly = false;
    }

    public ProductAdapter(List<Product> productList, boolean isReadOnly) {
        this.productList = productList;
        this.isReadOnly = isReadOnly;
        this.listener = null;
        this.addToCartListener = null;
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
            ImageView btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
            btnAddToCart.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (addToCartListener != null && pos != RecyclerView.NO_POSITION) {
                    addToCartListener.onAddToCartClick(productList.get(pos));
                }
            });
            
            // Ẩn nút Add to Cart trong chế độ read-only
            if (isReadOnly) {
                btnAddToCart.setVisibility(View.GONE);
            }
        }

        public void bind(Product product) {
            tvProductName.setText(product.getName());
            tvProductPrice.setText(String.format(Locale.getDefault(), "$%.2f", product.getPrice()));
            tvProductDescription.setText(product.getDescription());
            tvProductCategory.setText(product.getCategory());
            
            // Set default image for now (you can load actual images later)
            ivProductImage.setImageResource(R.drawable.ic_menu_business);
            
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });
        }
    }
}