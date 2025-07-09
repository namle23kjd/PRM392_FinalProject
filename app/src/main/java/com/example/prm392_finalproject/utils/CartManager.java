package com.example.prm392_finalproject.utils;

import com.example.prm392_finalproject.models.Product;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartManager {
    private static CartManager instance;
    private Map<Integer, CartItem> cartItems; // productId -> CartItem

    private CartManager() {
        cartItems = new HashMap<>();
    }

    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addToCart(Product product) {
        addToCart(product, 1);
    }

    public void addToCart(Product product, int quantity) {
        int productId = product.getProductId();
        if (cartItems.containsKey(productId)) {
            // Tăng số lượng nếu sản phẩm đã có trong giỏ hàng
            CartItem existingItem = cartItems.get(productId);
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            // Thêm sản phẩm mới vào giỏ hàng
            cartItems.put(productId, new CartItem(product, quantity));
        }
    }

    public void removeFromCart(int productId) {
        cartItems.remove(productId);
    }

    public void updateQuantity(int productId, int quantity) {
        if (cartItems.containsKey(productId)) {
            if (quantity <= 0) {
                removeFromCart(productId);
            } else {
                cartItems.get(productId).setQuantity(quantity);
            }
        }
    }

    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems.values());
    }

    public int getItemCount() {
        int total = 0;
        for (CartItem item : cartItems.values()) {
            total += item.getQuantity();
        }
        return total;
    }

    public double getTotalAmount() {
        double total = 0;
        for (CartItem item : cartItems.values()) {
            total += item.getTotalPrice();
        }
        return total;
    }

    public void clearCart() {
        cartItems.clear();
    }

    public boolean isEmpty() {
        return cartItems.isEmpty();
    }

    // Inner class để lưu trữ thông tin sản phẩm và số lượng
    public static class CartItem {
        private Product product;
        private int quantity;

        public CartItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }

        public Product getProduct() {
            return product;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getTotalPrice() {
            return product.getPrice() * quantity;
        }
    }
} 