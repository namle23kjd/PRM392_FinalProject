package com.example.prm392_finalproject.models.zalopay;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class Order implements Serializable {
    private String orderId;              // Mã đơn hàng
    private String orderDate;            // Ngày tạo đơn hàng
    private String status;               // Trạng thái đơn hàng (e.g. 'Completed', 'Pending')
    private double totalAmount;          // Tổng giá trị đơn hàng
    private String note;                 // Ghi chú đơn hàng
    private List<OrderItem> items;       // Danh sách các mục trong đơn hàng
    private int customerId;              // ID khách hàng

    // Constructor cho ZaloPay API
    public Order(String orderId, String orderDate, String status, double totalAmount, String note, List<OrderItem> items) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.status = status;
        this.totalAmount = totalAmount;
        this.note = note;
        this.items = items != null ? items : new ArrayList<>();
    }

    // Constructor cho database results
    public Order(int orderId, int customerId, Date orderDate, String status, double totalAmount, String note, List<OrderItem> items) {
        this.orderId = String.valueOf(orderId);
        this.customerId = customerId;
        this.orderDate = orderDate != null ? orderDate.toString() : new Date().toString();
        this.status = status;
        this.totalAmount = totalAmount;
        this.note = note;
        this.items = items != null ? items : new ArrayList<>();
    }

    // Default constructor
    public Order() {
        this.orderDate = new Date().toString();
        this.status = "pending";
        this.items = new ArrayList<>();
    }

    // Constructor for creating new order
    public Order(int customerId, double totalAmount, String note) {
        this();
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.note = note;
    }

    // Getters and Setters
    public String getOrderId() { return orderId; }
    public String getOrderDate() { return orderDate; }
    public String getStatus() { return status; }
    public double getTotalAmount() { return totalAmount; }
    public String getNote() { return note; }
    public List<OrderItem> getItems() { return items; }
    public int getCustomerId() { return customerId; }

    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }
    public void setStatus(String status) { this.status = status; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public void setNote(String note) { this.note = note; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    // Method to check if order is paid
    public boolean isPaid() {
        return "completed".equalsIgnoreCase(status) || "paid".equalsIgnoreCase(status);
    }

    // Helper method to get status display text
    public String getStatusDisplay() {
        switch (status.toLowerCase()) {
            case "pending":
                return "Chờ xác nhận";
            case "confirmed":
                return "Đã xác nhận";
            case "processing":
                return "Đang xử lý";
            case "shipping":
                return "Đang giao hàng";
            case "completed":
                return "Hoàn thành";
            case "cancelled":
                return "Đã hủy";
            default:
                return status;
        }
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", customerId=" + customerId +
                ", orderDate='" + orderDate + '\'' +
                ", status='" + status + '\'' +
                ", totalAmount=" + totalAmount +
                ", note='" + note + '\'' +
                ", items=" + items +
                '}';
    }
}
