package com.example.prm392_finalproject.models.zalopay;

import java.io.Serializable;

public class OrderItem implements Serializable {
    private String itemId;               // Mã mục sản phẩm
    private String itemName;             // Tên sản phẩm
    private int quantity;                // Số lượng
    private double unitPrice;            // Giá đơn vị
    private double totalPrice;           // Tổng giá trị của sản phẩm (quantity * unitPrice)
    
    // Thêm các field cho database
    private int id;                      // ID trong database
    private int orderId;                 // ID đơn hàng
    private int productId;               // ID sản phẩm

    // Constructor cho ZaloPay API
    public OrderItem(String itemId, String itemName, int quantity, double unitPrice, double totalPrice) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }

    // Constructor cho database results
    public OrderItem(int id, int orderId, int productId, int quantity, double unitPrice, double totalPrice) {
        this.id = id;
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.itemId = String.valueOf(id);
        this.itemName = "Product " + productId; // Tên mặc định
    }

    // Default constructor
    public OrderItem() {
    }

    // Getters and Setters
    public String getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
    public double getTotalPrice() { return totalPrice; }
    public int getId() { return id; }
    public int getOrderId() { return orderId; }
    public int getProductId() { return productId; }

    public void setItemId(String itemId) { this.itemId = itemId; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public void setId(int id) { this.id = id; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public void setProductId(int productId) { this.productId = productId; }
}
