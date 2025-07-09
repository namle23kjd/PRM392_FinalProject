package com.example.prm392_finalproject.models;

import com.example.prm392_finalproject.models.zalopay.Order;
import java.util.List;

public class OrderResponse {
    private boolean success;
    private String message;
    private Order data;
    private List<Order> dataList;

    public OrderResponse() {
    }

    public OrderResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public OrderResponse(boolean success, String message, Order data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public OrderResponse(boolean success, String message, List<Order> dataList) {
        this.success = success;
        this.message = message;
        this.dataList = dataList;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Order getData() {
        return data;
    }

    public void setData(Order data) {
        this.data = data;
    }

    public List<Order> getDataList() {
        return dataList;
    }

    public void setDataList(List<Order> dataList) {
        this.dataList = dataList;
    }
} 