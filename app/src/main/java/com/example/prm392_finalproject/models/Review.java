package com.example.prm392_finalproject.models;

import java.sql.Timestamp;

public class Review {
    private int reviewId;
    private int productId;
    private int customerId;
    private float rating;
    private String reviewText;
    private Timestamp createdAt;

    public Review() {
    }

    public Review(int reviewId, int customerId, int productId, float rating, String reviewText) {
        this.reviewId = reviewId;
        this.customerId = customerId;
        this.productId = productId;
        this.rating = rating;
        this.reviewText = reviewText;
    }


    public int getReviewId() {
        return reviewId;
    }

    public void setReviewId(int reviewId) {
        this.reviewId = reviewId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
