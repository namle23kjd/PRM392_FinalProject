package com.example.prm392_finalproject.models;

import java.util.List;

public class ProductResponse {
    private boolean success;
    private String message;
    private Product data;
    private List<Product> dataList;

    public ProductResponse() {
    }

    public ProductResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public ProductResponse(boolean success, String message, Product data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public ProductResponse(boolean success, String message, List<Product> dataList) {
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

    public Product getData() {
        return data;
    }

    public void setData(Product data) {
        this.data = data;
    }

    public List<Product> getDataList() {
        return dataList;
    }

    public void setDataList(List<Product> dataList) {
        this.dataList = dataList;
    }
}