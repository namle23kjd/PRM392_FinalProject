package com.example.prm392_finalproject.models;

import java.util.Date;

public class Order implements java.io.Serializable {
    private int order_id;
    private int customer_id;
    private Date order_date;
    private String status;
    private double total_amount;
    private String note;

    // Default constructor
    public Order() {
        this.order_date = new Date();
        this.status = "pending";
    }

    // Constructor for creating new order
    public Order(int customer_id, double total_amount, String note) {
        this();
        this.customer_id = customer_id;
        this.total_amount = total_amount;
        this.note = note;
    }

    // Getters and Setters
    public int getOrderId() {
        return order_id;
    }

    public void setOrderId(int order_id) {
        this.order_id = order_id;
    }

    public int getCustomerId() {
        return customer_id;
    }

    public void setCustomerId(int customer_id) {
        this.customer_id = customer_id;
    }

    public Date getOrderDate() {
        return order_date;
    }

    public void setOrderDate(Date order_date) {
        this.order_date = order_date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotalAmount() {
        return total_amount;
    }

    public void setTotalAmount(double total_amount) {
        this.total_amount = total_amount;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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
                "order_id=" + order_id +
                ", customer_id=" + customer_id +
                ", order_date=" + order_date +
                ", status='" + status + '\'' +
                ", total_amount=" + total_amount +
                ", note='" + note + '\'' +
                '}';
    }
}