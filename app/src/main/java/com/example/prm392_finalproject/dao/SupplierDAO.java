package com.example.prm392_finalproject.dao;

import com.example.prm392_finalproject.db.ConnectionClass;
import com.example.prm392_finalproject.models.SupplierRequest;
import com.example.prm392_finalproject.models.SupplierResponse;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SupplierDAO {
    private ConnectionClass connectionClass;

    public SupplierDAO() {
        this.connectionClass = new ConnectionClass();
    }

    public List<SupplierResponse> getAllSuppliers() {
        List<SupplierResponse> suppliers = new ArrayList<>();
        try {
            String query = "SELECT * FROM Suppliers";
            PreparedStatement preparedStatement = connectionClass.CONN().prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                SupplierResponse supplier = new SupplierResponse();
                supplier.setSupplier_id(resultSet.getInt("supplier_id"));
                supplier.setName(resultSet.getString("name"));
                supplier.setAddress(resultSet.getString("address"));
                supplier.setContact_info(resultSet.getString("contact_info"));
                supplier.setPhone(resultSet.getString("phone"));
                supplier.setEmail(resultSet.getString("email"));
                supplier.setIs_active(resultSet.getBoolean("isActive"));
                suppliers.add(supplier);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return suppliers;
    }

    public SupplierResponse getSupplierById(int supplier_id) {
        SupplierResponse supplier = new SupplierResponse();

        try {
            String query = "SELECT * FROM Suppliers WHERE supplier_id = ?";
            PreparedStatement preparedStatement = connectionClass.CONN().prepareStatement(query);
            preparedStatement.setInt(1, supplier_id);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                supplier.setSupplier_id(resultSet.getInt("supplier_id"));
                supplier.setName(resultSet.getString("name"));
                supplier.setAddress(resultSet.getString("address"));
                supplier.setContact_info(resultSet.getString("contact_info"));
                supplier.setPhone(resultSet.getString("phone"));
                supplier.setEmail(resultSet.getString("email"));
                supplier.setIs_active(resultSet.getBoolean("isActive"));
            }
            return supplier;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return supplier;
    }

    public void addSupplier(SupplierRequest supplier) {
        try {
            String query = "INSERT INTO Suppliers (name, address, contact_info, phone, email, isActive) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connectionClass.CONN().prepareStatement(query);
            preparedStatement.setString(1, supplier.getName());
            preparedStatement.setString(2, supplier.getAddress());
            preparedStatement.setString(3, supplier.getContact_info());
            preparedStatement.setString(4, supplier.getPhone());
            preparedStatement.setString(5, supplier.getEmail());
            preparedStatement.setBoolean(6, supplier.isIs_active());
            preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateSupplier(SupplierRequest supplier, int supplier_id) {
        try {
            String query = "UPDATE Suppliers SET name = ?, address = ?, contact_info = ?, phone = ?, email = ?, isActive = ? WHERE supplier_id = ?";
            PreparedStatement preparedStatement = connectionClass.CONN().prepareStatement(query);
            preparedStatement.setString(1, supplier.getName());
            preparedStatement.setString(2, supplier.getAddress());
            preparedStatement.setString(3, supplier.getContact_info());
            preparedStatement.setString(4, supplier.getPhone());
            preparedStatement.setString(5, supplier.getEmail());
            preparedStatement.setBoolean(6, supplier.isIs_active());
            preparedStatement.setInt(7, supplier_id);
            preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteSupplier(int supplier_id) {
        try {
            String query = "UPDATE Suppliers SET isActive = 0 WHERE supplier_id = ?";
            PreparedStatement preparedStatement = connectionClass.CONN().prepareStatement(query);
            preparedStatement.setInt(1, supplier_id);
            preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getSupplierCount() {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM Suppliers WHERE isActive = 1";
        try (PreparedStatement ps = connectionClass.CONN().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }
}