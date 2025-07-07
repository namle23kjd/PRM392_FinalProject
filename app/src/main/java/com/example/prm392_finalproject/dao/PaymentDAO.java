package com.example.prm392_finalproject.dao;

import com.example.prm392_finalproject.db.ConnectionClass;
import com.example.prm392_finalproject.models.Payment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class PaymentDAO {
    private ConnectionClass connectionClass;
    public PaymentDAO() {
        this.connectionClass = new ConnectionClass();
    }
    public void insertPayment(int orderId, double amount, String paymentMethod, String paymentStatus) {
        String sql = "INSERT INTO Payments (order_id, payment_date, amount, payment_method, payment_status) VALUES (?, NOW(), ?, ?, ?)";
        try (
            Connection conn = connectionClass.CONN();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, orderId);
            ps.setDouble(2, amount);
            ps.setString(3, paymentMethod);
            ps.setString(4, paymentStatus);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public List<Payment> getPaymentsByUserId(int userId) {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT p.payment_id, p.order_id, p.payment_date, p.amount, p.payment_method, p.payment_status " +
                "FROM Payments p JOIN Orders o ON p.order_id = o.order_id WHERE o.customer_id = ? ORDER BY p.payment_date DESC";
        try (
            Connection conn = connectionClass.CONN();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Payment payment = new Payment();
                payment.setId(rs.getInt("payment_id"));
                payment.setOrderId(rs.getInt("order_id"));
                payment.setPaymentDate(String.valueOf(rs.getTimestamp("payment_date")));
                payment.setAmount(rs.getDouble("amount"));
                payment.setPaymentMethod(rs.getString("payment_method"));
                payment.setPaymentStatus(rs.getString("payment_status"));
                payments.add(payment);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return payments;
    }
    public boolean hasCompletedPayment(int orderId) {
        String sql = "SELECT COUNT(*) FROM Payments WHERE order_id = ? AND payment_status = 'Completed'";
        try (
            Connection conn = connectionClass.CONN();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public List<Payment> getAllPayments() {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT payment_id, order_id, payment_date, amount, payment_method, payment_status FROM Payments ORDER BY payment_date DESC";
        try (
            Connection conn = connectionClass.CONN();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Payment payment = new Payment();
                payment.setId(rs.getInt("payment_id"));
                payment.setOrderId(rs.getInt("order_id"));
                payment.setPaymentDate(String.valueOf(rs.getTimestamp("payment_date")));
                payment.setAmount(rs.getDouble("amount"));
                payment.setPaymentMethod(rs.getString("payment_method"));
                payment.setPaymentStatus(rs.getString("payment_status"));
                payments.add(payment);
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return payments;
    }
    public Map<Integer, Integer> getPaymentCountByMonth(int year) {
        Map<Integer, Integer> monthCount = new HashMap<>();
        String sql = "SELECT MONTH(payment_date) as month, COUNT(*) as count FROM Payments WHERE YEAR(payment_date) = ? GROUP BY MONTH(payment_date)";
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
} 