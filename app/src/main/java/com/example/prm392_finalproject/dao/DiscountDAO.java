package com.example.prm392_finalproject.dao;

import com.example.prm392_finalproject.db.ConnectionClass;
import com.example.prm392_finalproject.models.Discount;
import com.example.prm392_finalproject.models.DiscountRequest;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DiscountDAO {
    private ConnectionClass connectionClass;

    public DiscountDAO() {
        this.connectionClass = new ConnectionClass();
    }

    public void addDiscount(DiscountRequest request) {
        String code = generateRandomCode();
        Discount discount = new Discount();
        discount.setCode(code);
        discount.setDiscount_type(request.getDiscount_type());
        discount.setDiscount_value(request.getDiscount_value());
        discount.setStart_date(request.getStart_date());
        discount.setEnd_date(request.getEnd_date());
        String sql = "INSERT INTO Discounts (code, discount_type, discount_value, start_date, end_date) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = connectionClass.CONN();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, discount.getCode());
            ps.setString(2, discount.getDiscount_type());
            ps.setDouble(3, discount.getDiscount_value());
            // Xử lý ngày tháng: chuyển LocalDateTime thành String trước khi tạo Timestamp
            if (discount.getStart_date() != null) {
                String startDateStr = discount.getStart_date().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                ps.setTimestamp(4, Timestamp.valueOf(startDateStr));
            } else {
                ps.setTimestamp(4, null);
            }
            if (discount.getEnd_date() != null) {
                String endDateStr = discount.getEnd_date().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                ps.setTimestamp(5, Timestamp.valueOf(endDateStr));
            } else {
                ps.setTimestamp(5, null);
            }
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String generateRandomCode() {
        Random random = new Random();
        int min = 100000; // 6 chữ số
        int max = 999999;
        return Integer.toString(random.nextInt(max - min + 1) + min);
    }

    public void updateDiscount(Discount discount) {
        String sql = "UPDATE Discounts SET code=?, discount_type=?, discount_value=?, start_date=?, end_date=? WHERE discount_id=?";
        try (Connection conn = connectionClass.CONN();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, discount.getCode());
            ps.setString(2, discount.getDiscount_type());
            ps.setDouble(3, discount.getDiscount_value());
            if (discount.getStart_date() != null) {
                String startDateStr = discount.getStart_date().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                ps.setTimestamp(4, Timestamp.valueOf(startDateStr));
            } else {
                ps.setTimestamp(4, null);
            }
            if (discount.getEnd_date() != null) {
                String endDateStr = discount.getEnd_date().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                ps.setTimestamp(5, Timestamp.valueOf(endDateStr));
            } else {
                ps.setTimestamp(5, null);
            }
            ps.setInt(6, discount.getDiscount_id());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isDiscountInUse(int discountId) {
        String sql = "SELECT COUNT(*) FROM Order_Discounts WHERE discount_id = ?";
        try (Connection conn = connectionClass.CONN();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, discountId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("Discount " + discountId + " is used in " + count + " orders");
                return count > 0;
            }
        } catch (Exception e) {
            System.err.println("Error checking if discount is in use: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteDiscount(int discountId) {
        // Kiểm tra xem discount có đang được sử dụng không
        if (isDiscountInUse(discountId)) {
            System.out.println("Cannot delete discount " + discountId + " - it is currently in use");
            return false;
        }
        
        String sql = "DELETE FROM Discounts WHERE discount_id=?";
        try (Connection conn = connectionClass.CONN();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, discountId);
            int affectedRows = ps.executeUpdate();
            System.out.println("Deleted discount " + discountId + " - affected rows: " + affectedRows);
            return affectedRows > 0;
        } catch (Exception e) {
            System.err.println("Error deleting discount: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Discount> getAllDiscounts() {
        List<Discount> discounts = new ArrayList<>();
        String sql = "SELECT * FROM Discounts ORDER BY discount_id DESC";
        System.out.println("Executing SQL: " + sql);
        
        try (Connection conn = connectionClass.CONN()) {
            if (conn == null) {
                System.err.println("Connection is null!");
                return discounts;
            }
            
            System.out.println("Database connection successful");
            
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                
                System.out.println("Query executed successfully");
                int count = 0;
                
                while (rs.next()) {
                    count++;
                    System.out.println("Processing row " + count);
                    
                    Timestamp startTs = rs.getTimestamp("start_date");
                    Timestamp endTs = rs.getTimestamp("end_date");

                    Discount d = new Discount(
                        rs.getInt("discount_id"),
                        rs.getString("code"),
                        rs.getString("discount_type"),
                        rs.getDouble("discount_value"),
                        startTs != null ? LocalDateTime.ofInstant(startTs.toInstant(), ZoneId.systemDefault()) : null,
                        endTs != null ? LocalDateTime.ofInstant(endTs.toInstant(), ZoneId.systemDefault()) : null
                    );
                    discounts.add(d);
                }
                
                System.out.println("Total discounts loaded: " + count);
            }
        } catch (Exception e) {
            System.err.println("Error in getAllDiscounts: " + e.getMessage());
            e.printStackTrace();
        }
        
        return discounts;
    }

    public Discount getDiscountByCode(String code) {
        String sql = "SELECT * FROM Discounts WHERE code = ?";
        try (Connection conn = connectionClass.CONN();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Timestamp startTs = rs.getTimestamp("start_date");
                Timestamp endTs = rs.getTimestamp("end_date");
                return new Discount(
                    rs.getInt("discount_id"),
                    rs.getString("code"),
                    rs.getString("discount_type"),
                    rs.getDouble("discount_value"),
                    startTs != null ? LocalDateTime.ofInstant(startTs.toInstant(), ZoneId.systemDefault()) : null,
                    endTs != null ? LocalDateTime.ofInstant(endTs.toInstant(), ZoneId.systemDefault()) : null
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isDiscountValid(String code) {
        String sql = "SELECT * FROM Discounts WHERE code = ? AND start_date <= ? AND end_date >= ?";
        try (Connection conn = connectionClass.CONN();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            LocalDateTime now = LocalDateTime.now();
            String nowStr = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            ps.setString(1, code);
            ps.setTimestamp(2, Timestamp.valueOf(nowStr));
            ps.setTimestamp(3, Timestamp.valueOf(nowStr));
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean checkTableExists() {
        String sql = "SHOW TABLES LIKE 'Discounts'";
        try (Connection conn = connectionClass.CONN();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            boolean exists = rs.next();
            System.out.println("Discount table exists: " + exists);
            return exists;
        } catch (Exception e) {
            System.err.println("Error checking table existence: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public int getDiscountCount() {
        String sql = "SELECT COUNT(*) FROM Discounts";
        try (Connection conn = connectionClass.CONN();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("Total discounts in database: " + count);
                return count;
            }
        } catch (Exception e) {
            System.err.println("Error getting discount count: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
} 