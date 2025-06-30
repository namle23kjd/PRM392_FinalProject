package com.example.prm392_finalproject.dao;

import com.example.prm392_finalproject.db.ConnectionClass;
import com.example.prm392_finalproject.models.zalopay.Order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

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
} 