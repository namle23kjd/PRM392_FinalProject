package com.example.prm392_finalproject.models;

import java.util.List;

public class OrderRequest {
    private int customerId;
    private double totalAmount;
    private String note;
    private String shippingAddress;
    private String shippingMethod;
    private String shippingPersonName;
    private List<OrderItemRequest> items;

    public OrderRequest() {
    }

    public OrderRequest(int customerId, double totalAmount, String note, 
                       String shippingAddress, String shippingMethod, 
                       String shippingPersonName, List<OrderItemRequest> items) {
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.note = note;
        this.shippingAddress = shippingAddress;
        this.shippingMethod = shippingMethod;
        this.shippingPersonName = shippingPersonName;
        this.items = items;
    }

    // Getters and Setters
    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getShippingMethod() {
        return shippingMethod;
    }

    public void setShippingMethod(String shippingMethod) {
        this.shippingMethod = shippingMethod;
    }

    public String getShippingPersonName() {
        return shippingPersonName;
    }

    public void setShippingPersonName(String shippingPersonName) {
        this.shippingPersonName = shippingPersonName;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }

    // Inner class for order items
    public static class OrderItemRequest {
        private int productId;
        private int quantity;
        private double unitPrice;

        public OrderItemRequest() {
        }

        public OrderItemRequest(int productId, int quantity, double unitPrice) {
            this.productId = productId;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        // Getters and Setters
        public int getProductId() {
            return productId;
        }

        public void setProductId(int productId) {
            this.productId = productId;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(double unitPrice) {
            this.unitPrice = unitPrice;
        }
    }
} 