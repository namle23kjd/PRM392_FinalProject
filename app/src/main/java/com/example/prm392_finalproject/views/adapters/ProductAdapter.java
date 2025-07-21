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

    // Interfaces
    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public interface OnAddToCartClickListener {
        void onAddToCartClick(Product product);
    }

    public interface OnProductLongClickListener {
        void onProductLongClick(Product product);
    }

    // Constructor without long click listener (for normal customers)
    public ProductAdapter(List<Product> productList,
                          OnProductClickListener listener,
                          OnAddToCartClickListener addToCartListener) {
        this.productList = productList;
        this.listener = listener;
        this.addToCartListener = addToCartListener;
        this.longClickListener = null;
    }

    // Constructor with long click listener (for staff functionality)
    public ProductAdapter(List<Product> productList,
                          OnProductClickListener listener,
                          OnAddToCartClickListener addToCartListener,
                          OnProductLongClickListener longClickListener) {
        this.productList = productList;
        this.listener = listener;
        this.addToCartListener = addToCartListener;
        this.longClickListener = longClickListener;
    }

    // Setter for long click listener (can be set later)
    public void setOnProductLongClickListener(OnProductLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
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
        return productList != null ? productList.size() : 0;
    }

    // Helper methods for list management
    public void updateProduct(Product updatedProduct) {
        if (productList == null) return;

        for (int i = 0; i < productList.size(); i++) {
            if (productList.get(i).getProductId() == updatedProduct.getProductId()) {
                productList.set(i, updatedProduct);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void removeProduct(int productId) {
        if (productList == null) return;

        for (int i = 0; i < productList.size(); i++) {
            if (productList.get(i).getProductId() == productId) {
                productList.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    public void addProduct(Product product) {
        if (productList == null) return;

        productList.add(product);
        notifyItemInserted(productList.size() - 1);
    }

    public void updateProductList(List<Product> newProductList) {
        this.productList = newProductList;
        notifyDataSetChanged();
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

            // Initialize views
            cardView = itemView.findViewById(R.id.cardProduct);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductDescription = itemView.findViewById(R.id.tvProductDescription);
            tvProductCategory = itemView.findViewById(R.id.tvProductCategory);

            setupClickListeners();
        }

        private void setupClickListeners() {
            // Add to cart button click listener
            ImageView btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
            if (btnAddToCart != null) {
                btnAddToCart.setOnClickListener(v -> {
                    int pos = getAdapterPosition();
                    if (addToCartListener != null && pos != RecyclerView.NO_POSITION && productList != null) {
                        Product product = productList.get(pos);
                        // Check if product is available before adding to cart
                        if (product.isActive() && product.getQuantityInStock() > 0) {
                            addToCartListener.onAddToCartClick(product);
                        } else {
                            // Could show a toast here indicating product is not available
                            Log.w("ProductAdapter", "Product not available: " + product.getName());
                        }
                    }
                });
            }

            // Normal click listener for product details
            cardView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (listener != null && pos != RecyclerView.NO_POSITION && productList != null) {
                    listener.onProductClick(productList.get(pos));
                }
            });

            // Long click listener for staff functions (edit/update)
            itemView.setOnLongClickListener(v -> {
                int pos = getAdapterPosition();
                if (longClickListener != null && pos != RecyclerView.NO_POSITION && productList != null) {
                    v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    longClickListener.onProductLongClick(productList.get(pos));
                    return true;
                }
                return false;
            });
        }

        public void bind(Product product) {
            if (product == null) return;

            // Set product information
            setProductTexts(product);
            setProductImage(product);
            setProductStatus(product);

            Log.d("ProductAdapter", "Bound product: " + product.getName() +
                    ", Stock: " + product.getQuantityInStock() +
                    ", Active: " + product.isActive());
        }

        private void setProductTexts(Product product) {
            // Product name with stock indicator
            String productName = product.getName();
            if (product.getQuantityInStock() <= 0) {
                productName += " (Hết hàng)";
            } else if (product.getQuantityInStock() <= 5) {
                productName += " (Sắp hết)";
            }
            tvProductName.setText(productName);

            // Price formatting
            tvProductPrice.setText(String.format(Locale.getDefault(), "$%.2f", product.getPrice()));

            // Description
            String description = product.getDescription();
            if (description != null && description.length() > 100) {
                description = description.substring(0, 97) + "...";
            }
            tvProductDescription.setText(description);

            // Category
            tvProductCategory.setText(product.getCategory() != null ? product.getCategory() : "Chưa phân loại");
        }

        private void setProductImage(Product product) {
            // Log image URL for debugging
            Log.d("ProductAdapter", "Image URL for product \"" + product.getName() + "\": " + product.getImageUrl());

            // Load image with Glide
            if (product.getImageUrl() != null && !product.getImageUrl().trim().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(product.getImageUrl())
                        .placeholder(R.drawable.ic_menu_business)
                        .error(R.drawable.ic_menu_business)
                        .into(ivProductImage);
            } else {
                ivProductImage.setImageResource(R.drawable.ic_menu_business);
            }
        }

        private void setProductStatus(Product product) {
            // Visual indicators based on stock and active status
            if (!product.isActive()) {
                // Inactive product - gray out
                cardView.setAlpha(0.5f);
                cardView.setCardBackgroundColor(0xFFECECEC);
                tvProductName.setTextColor(0xFF757575);
                tvProductPrice.setTextColor(0xFF757575);
            } else if (product.getQuantityInStock() <= 0) {
                // Out of stock - red tint
                cardView.setAlpha(1.0f);
                cardView.setCardBackgroundColor(0xFFFFEBEE);
                tvProductName.setTextColor(0xFFD32F2F);
                tvProductPrice.setTextColor(0xFF212121);
            } else if (product.getQuantityInStock() <= 5) {
                // Low stock - orange tint
                cardView.setAlpha(1.0f);
                cardView.setCardBackgroundColor(0xFFFFF3E0);
                tvProductName.setTextColor(0xFFF57C00);
                tvProductPrice.setTextColor(0xFF212121);
            } else {
                // Normal stock - default colors
                cardView.setAlpha(1.0f);
                cardView.setCardBackgroundColor(0xFFFFFFFF);
                tvProductName.setTextColor(0xFF212121);
                tvProductPrice.setTextColor(0xFF4CAF50); // Green for price when available
            }

            // Disable add to cart button if product is not available
            ImageView btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
            if (btnAddToCart != null) {
                if (!product.isActive() || product.getQuantityInStock() <= 0) {
                    btnAddToCart.setAlpha(0.3f);
                    btnAddToCart.setEnabled(false);
                } else {
                    btnAddToCart.setAlpha(1.0f);
                    btnAddToCart.setEnabled(true);
                }
            }
        }
    }

    // Utility method to get product at position
    public Product getProductAt(int position) {
        if (productList != null && position >= 0 && position < productList.size()) {
            return productList.get(position);
        }
        return null;
    }

    // Utility method to find product by ID
    public int findProductPosition(int productId) {
        if (productList == null) return -1;

        for (int i = 0; i < productList.size(); i++) {
            if (productList.get(i).getProductId() == productId) {
                return i;
            }
        }
        return -1;
    }
}