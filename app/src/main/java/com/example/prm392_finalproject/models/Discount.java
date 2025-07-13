package com.example.prm392_finalproject.models;

import java.time.LocalDateTime;

public class Discount {
    private int discount_id;
    private String code;
    private String discount_type;
    private double discount_value;
    private LocalDateTime start_date;
    private LocalDateTime end_date;

    public Discount() {
    }

    public Discount(int discount_id, String code, String discount_type, double discount_value, LocalDateTime start_date, LocalDateTime end_date) {
        this.discount_id = discount_id;
        this.code = code;
        this.discount_type = discount_type;
        this.discount_value = discount_value;
        this.start_date = start_date;
        this.end_date = end_date;
    }

    public int getDiscount_id() {
        return discount_id;
    }

    public void setDiscount_id(int discount_id) {
        this.discount_id = discount_id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDiscount_type() {
        return discount_type;
    }

    public void setDiscount_type(String discount_type) {
        this.discount_type = discount_type;
    }

    public double getDiscount_value() {
        return discount_value;
    }

    public void setDiscount_value(double discount_value) {
        this.discount_value = discount_value;
    }

    public LocalDateTime getStart_date() {
        return start_date;
    }

    public void setStart_date(LocalDateTime start_date) {
        this.start_date = start_date;
    }

    public LocalDateTime getEnd_date() {
        return end_date;
    }

    public void setEnd_date(LocalDateTime end_date) {
        this.end_date = end_date;
    }
}
