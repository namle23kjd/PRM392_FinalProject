package com.example.prm392_finalproject.dao;

import com.example.prm392_finalproject.db.ConnectionClass;
import com.example.prm392_finalproject.models.OrderDiscount;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDiscountDAO {
    private ConnectionClass connectionClass;
    private DiscountDAO discountDAO;

    public OrderDiscountDAO() {
        this.connectionClass = new ConnectionClass();
        this.discountDAO = new DiscountDAO();
    }

    public boolean addOrderDiscount(OrderDiscount orderDiscount, String discountCode) {
        // Chỉ thêm nếu mã giảm giá hợp lệ
        if (!discountDAO.isDiscountValid(discountCode)) {
            return false;
        }
        String sql = "INSERT INTO Order_Discounts (discount_id, order_id) VALUES ((SELECT discount_id FROM Discounts WHERE code = ?), ?)";
        try (Connection conn = connectionClass.CONN();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderDiscount.getDiscount_id());
            ps.setInt(2, orderDiscount.getOrder_id());
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void deleteOrderDiscount(int orderId, int discountId) {
        String sql = "DELETE FROM Order_Discounts WHERE order_id = ? AND discount_id = ?";
        try (Connection conn = connectionClass.CONN();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, discountId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<OrderDiscount> getDiscountsByOrderId(int orderId) {
        List<OrderDiscount> list = new ArrayList<>();
        String sql = "SELECT * FROM Order_Discounts WHERE order_id = ?";
        try (Connection conn = connectionClass.CONN();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new OrderDiscount(rs.getInt("discount_id"), rs.getInt("order_id")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<OrderDiscount> getOrdersByDiscountId(int discountId) {
        List<OrderDiscount> list = new ArrayList<>();
        String sql = "SELECT * FROM Order_Discounts WHERE discount_id = ?";
        try (Connection conn = connectionClass.CONN();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, discountId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new OrderDiscount(rs.getInt("discount_id"), rs.getInt("order_id")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}