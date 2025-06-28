package com.example.prm392_finalproject.models;

import java.util.Date;

public class Shipping implements java.io.Serializable {
    private int shipping_id;
    private int order_id;
    private String shipping_address;
    private String shipping_method;
    private String shipping_person_name;
    private String tracking_number;
    private Date expected_delivery;
    private Date delivered_date;
    private String description;
    private String status;

    // Default constructor
    public Shipping() {
        this.status = "pending"; // Default status
    }

    // Constructor for creating new shipping
    public Shipping(int order_id, String shipping_address, String shipping_method,
                    String shipping_person_name, Date expected_delivery, String description) {
        this();
        this.order_id = order_id;
        this.shipping_address = shipping_address;
        this.shipping_method = shipping_method;
        this.shipping_person_name = shipping_person_name;
        this.expected_delivery = expected_delivery;
        this.description = description;
        this.tracking_number = generateTrackingNumber();
    }

    // Getters and Setters
    public int getShippingId() {
        return shipping_id;
    }

    public void setShippingId(int shipping_id) {
        this.shipping_id = shipping_id;
    }

    public int getOrderId() {
        return order_id;
    }

    public void setOrderId(int order_id) {
        this.order_id = order_id;
    }

    public String getShippingAddress() {
        return shipping_address;
    }

    public void setShippingAddress(String shipping_address) {
        this.shipping_address = shipping_address;
    }

    public String getShippingMethod() {
        return shipping_method;
    }

    public void setShippingMethod(String shipping_method) {
        this.shipping_method = shipping_method;
    }

    public String getShippingPersonName() {
        return shipping_person_name;
    }

    public void setShippingPersonName(String shipping_person_name) {
        this.shipping_person_name = shipping_person_name;
    }

    public String getTrackingNumber() {
        return tracking_number;
    }

    public void setTrackingNumber(String tracking_number) {
        this.tracking_number = tracking_number;
    }

    public Date getExpectedDelivery() {
        return expected_delivery;
    }

    public void setExpectedDelivery(Date expected_delivery) {
        this.expected_delivery = expected_delivery;
    }

    public Date getDeliveredDate() {
        return delivered_date;
    }

    public void setDeliveredDate(Date delivered_date) {
        this.delivered_date = delivered_date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Helper method to generate tracking number
    private String generateTrackingNumber() {
        return "TN" + System.currentTimeMillis();
    }

    // Helper method to check if delivery is overdue
    public boolean isOverdue() {
        if (expected_delivery != null && !"delivered".equals(status)) {
            return new Date().after(expected_delivery);
        }
        return false;
    }

    // Helper method to get status display text
    public String getStatusDisplay() {
        switch (status.toLowerCase()) {
            case "pending":
                return "Chờ xử lý";
            case "preparing":
                return "Đang chuẩn bị";
            case "shipping":
                return "Đang giao hàng";
            case "delivered":
                return "Đã giao hàng";
            case "cancelled":
                return "Đã hủy";
            default:
                return status;
        }
    }

    @Override
    public String toString() {
        return "Shipping{" +
                "shipping_id=" + shipping_id +
                ", order_id=" + order_id +
                ", shipping_address='" + shipping_address + '\'' +
                ", shipping_method='" + shipping_method + '\'' +
                ", shipping_person_name='" + shipping_person_name + '\'' +
                ", tracking_number='" + tracking_number + '\'' +
                ", expected_delivery=" + expected_delivery +
                ", delivered_date=" + delivered_date +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}