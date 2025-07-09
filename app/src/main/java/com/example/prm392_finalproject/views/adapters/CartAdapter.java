package com.example.prm392_finalproject.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.utils.CartManager;

import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    
    private List<CartManager.CartItem> cartItems;
    private OnCartItemListener listener;

    public interface OnCartItemListener {
        void onQuantityChanged(int productId, int newQuantity);
        void onRemoveItem(int productId);
    }

    public CartAdapter(List<CartManager.CartItem> cartItems, OnCartItemListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartManager.CartItem cartItem = cartItems.get(position);
        holder.bind(cartItem);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public void updateData(List<CartManager.CartItem> newCartItems) {
        this.cartItems = newCartItems;
        notifyDataSetChanged();
    }

    public class CartViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProductImage;
        private TextView tvProductName;
        private TextView tvProductPrice;
        private TextView tvQuantity;
        private TextView tvTotalPrice;
        private ImageView btnIncrease;
        private ImageView btnDecrease;
        private ImageView btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }

        public void bind(CartManager.CartItem cartItem) {
            tvProductName.setText(cartItem.getProduct().getName());
            tvProductPrice.setText(String.format(Locale.getDefault(), "$%.2f", cartItem.getProduct().getPrice()));
            tvQuantity.setText(String.valueOf(cartItem.getQuantity()));
            tvTotalPrice.setText(String.format(Locale.getDefault(), "$%.2f", cartItem.getTotalPrice()));
            
            // Set default image for now
            ivProductImage.setImageResource(R.drawable.ic_menu_business);
            
            // Setup click listeners
            btnIncrease.setOnClickListener(v -> {
                int newQuantity = cartItem.getQuantity() + 1;
                if (listener != null) {
                    listener.onQuantityChanged(cartItem.getProduct().getProductId(), newQuantity);
                }
            });
            
            btnDecrease.setOnClickListener(v -> {
                int newQuantity = cartItem.getQuantity() - 1;
                if (listener != null) {
                    listener.onQuantityChanged(cartItem.getProduct().getProductId(), newQuantity);
                }
            });
            
            btnRemove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveItem(cartItem.getProduct().getProductId());
                }
            });
        }
    }
} 