package com.example.prm392_finalproject.controllers;

import com.example.prm392_finalproject.dao.OrderDAO;
import com.example.prm392_finalproject.dao.ReviewDAO;
import com.example.prm392_finalproject.models.Review;

import java.util.List;

public class ReviewController {
    private final OrderDAO orderDAO;
    private final ReviewDAO reviewDAO;

    public ReviewController(OrderDAO orderDAO, ReviewDAO reviewDAO) {
        this.orderDAO = orderDAO;
        this.reviewDAO = reviewDAO;
    }

    public boolean canUserReviewProduct(int userId, int productId) {
        // Giả sử luôn cho phép đánh giá (hoặc kiểm tra đơn hàng nếu cần)
        return true;
        // return orderDAO.hasUserPurchasedProduct(userId, productId);
    }

    public void addReview(Review review) {
        reviewDAO.addReview(review);
    }

    public List<Review> getProductReviews(int productId) {
        return reviewDAO.getReviewsByProductId(productId);
    }

    public float getAverageRating(int productId) {
        return reviewDAO.getAverageRating(productId);
    }
}
