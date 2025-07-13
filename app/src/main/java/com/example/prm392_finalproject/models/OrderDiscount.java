package com.example.prm392_finalproject.models;

public class OrderDiscount {
    private int discount_id;
    private int order_id;

    public OrderDiscount () {

    }

    public OrderDiscount(int discount_id, int order_id) {
        this.discount_id = discount_id;
        this.order_id = order_id;
    }

    public int getDiscount_id() {
        return discount_id;
    }

    public void setDiscount_id(int discount_id) {
        this.discount_id = discount_id;
    }

    public int getOrder_id() {
        return order_id;
    }

    public void setOrder_id(int order_id) {
        this.order_id = order_id;
    }
}
