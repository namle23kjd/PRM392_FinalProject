package com.example.prm392_finalproject.dao;

import android.util.Log;

import com.example.prm392_finalproject.db.ConnectionClass;
import com.example.prm392_finalproject.models.zalopay.Order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderDAO {
    private ConnectionClass connectionClass;

    public OrderDAO() {
        this.connectionClass = new ConnectionClass();
    }

    public List<Order> getOrdersByUserId(int userId) {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT order_id, customer_id, order_date, status, total_amount, note FROM Orders WHERE customer_id = ? ORDER BY order_id DESC";
        try{
            PreparedStatement preparedStatement = connectionClass.CONN().prepareStatement(query);
            preparedStatement.setInt(1, userId);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Order order = new Order();
                order.setOrderId(String.valueOf(rs.getInt("order_id")));
                order.setCustomerId(rs.getInt("customer_id"));
                order.setOrderDate(String.valueOf(rs.getTimestamp("order_date")));
                order.setStatus(rs.getString("status"));
                order.setTotalAmount(rs.getDouble("total_amount"));
                order.setNote(rs.getString("note"));
                orders.add(order);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orders;
    }

    public void updateOrderStatus(String orderId, String newStatus) {
        String sql = "UPDATE Orders SET status = ? WHERE order_id = ?";
        try (
            Connection conn = connectionClass.CONN();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, newStatus);
            ps.setInt(2, Integer.parseInt(orderId));
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT order_id, customer_id, order_date, status, total_amount, note FROM Orders ORDER BY order_id DESC";
        try {
            Connection conn = connectionClass.CONN();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Order order = new Order();
                order.setOrderId(String.valueOf(rs.getInt("order_id")));
                order.setCustomerId(rs.getInt("customer_id"));
                order.setOrderDate(String.valueOf(rs.getTimestamp("order_date")));
                order.setStatus(rs.getString("status"));
                order.setTotalAmount(rs.getDouble("total_amount"));
                order.setNote(rs.getString("note"));
                orders.add(order);
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orders;
    }

    public Map<Integer, Integer> getOrderCountByMonth(int year) {
        Map<Integer, Integer> monthCount = new HashMap<>();
        String sql = "SELECT MONTH(order_date) as month, COUNT(*) as count FROM Orders WHERE YEAR(order_date) = ? GROUP BY MONTH(order_date)";
        try (Connection conn = connectionClass.CONN();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                monthCount.put(rs.getInt("month"), rs.getInt("count"));
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return monthCount;
    }


    public boolean hasUserPurchasedProduct(int userId, int productId) {
        boolean hasPurchased = false;

        try (Connection conn = new ConnectionClass().CONN()) {
            String sql = "SELECT COUNT(*) FROM Orders WHERE userId = ? AND productId = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, productId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                hasPurchased = rs.getInt(1) > 0;
            }

            rs.close();
            stmt.close();
        } catch (Exception e) {
            Log.e("OrderDAO", e.getMessage());
        }

        return hasPurchased;
    }

} 