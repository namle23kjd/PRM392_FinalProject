package com.example.prm392_finalproject.dao;

import android.util.Log;

import com.example.prm392_finalproject.db.ConnectionClass;
import com.example.prm392_finalproject.models.Review;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ReviewDAO {

    public void addReview(Review review) {
        String sql = "INSERT INTO Reviews (product_id, customer_id, rating, review_text, created_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (Connection conn = new ConnectionClass().CONN();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (conn == null) {
                Log.e("ReviewDAO", "❌ Cannot add review: connection is null.");
                return;
            }

            stmt.setInt(1, review.getProductId());
            stmt.setInt(2, review.getCustomerId());
            stmt.setFloat(3, review.getRating());
            stmt.setString(4, review.getReviewText());

            int rowsInserted = stmt.executeUpdate();
            Log.d("ReviewDAO", "✅ Review added successfully. Rows inserted: " + rowsInserted);

        } catch (Exception e) {
            Log.e("ReviewDAO", "❌ SQL Error adding review: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Review> getReviewsByProductId(int productId) {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM Reviews WHERE product_id = ? ORDER BY created_at DESC";

        try (Connection conn = new ConnectionClass().CONN();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (conn == null) {
                Log.e("ReviewDAO", "❌ Cannot load reviews: connection is null.");
                return reviews;
            }

            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Review review = new Review();
                review.setReviewId(rs.getInt("review_id"));
                review.setProductId(rs.getInt("product_id"));
                review.setCustomerId(rs.getInt("customer_id"));
                review.setRating(rs.getFloat("rating"));
                review.setReviewText(rs.getString("review_text"));
                review.setCreatedAt(rs.getTimestamp("created_at"));

                reviews.add(review);
            }

            rs.close();
            Log.d("ReviewDAO", "✅ Loaded " + reviews.size() + " reviews for product_id = " + productId);

        } catch (Exception e) {
            Log.e("ReviewDAO", "❌ SQL Error loading reviews: " + e.getMessage());
            e.printStackTrace();
        }

        return reviews;
    }

    public float getAverageRating(int productId) {
        float avgRating = 0;
        String sql = "SELECT AVG(rating) AS avg_rating FROM Reviews WHERE product_id = ?";

        try (Connection conn = new ConnectionClass().CONN();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (conn == null) {
                Log.e("ReviewDAO", "❌ Cannot calculate average rating: connection is null.");
                return 0;
            }

            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                avgRating = rs.getFloat("avg_rating");
            }

            rs.close();
            Log.d("ReviewDAO", "✅ Average rating for product_id = " + productId + " is " + avgRating);

        } catch (Exception e) {
            Log.e("ReviewDAO", "❌ SQL Error calculating average rating: " + e.getMessage());
            e.printStackTrace();
        }

        return avgRating;
    }

}
