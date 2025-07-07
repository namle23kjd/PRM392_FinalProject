package com.example.prm392_finalproject.dao;

import android.util.Log;

import com.example.prm392_finalproject.db.ConnectionClass;
import com.example.prm392_finalproject.models.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    private ConnectionClass connectionClass;

    public ProductDAO() {
        connectionClass = new ConnectionClass();
    }

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        try {
            Connection conn = connectionClass.CONN();
            if (conn == null) {
                System.out.println("ProductDAO: Connection is null in getAllProducts");
                return products;
            }
            String query = "SELECT p.*, c.name as category_name FROM Product p LEFT JOIN Category c ON p.category_id = c.category_id WHERE p.is_active = 1";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Product p = extractProduct(resultSet);
                if (p != null) {
                    products.add(p);
                }
            }
        } catch (Exception e) {
            System.out.println("ProductDAO: Exception: " + e.getMessage());
            e.printStackTrace();
        }
        return products;
    }


    public Product getProductById(int productId) {
        Product product = null;
        Connection conn = connectionClass.CONN();
        if (conn == null) {
            Log.e("ProductDAO", "Connection is null in getProductById");
            return null;
        }

        try {
            String query = "SELECT * FROM Product WHERE product_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                product = extractProduct(rs);
            }
        } catch (Exception e) {
            Log.e("ProductDAO", "Error fetching product by ID: " + e.getMessage());
        }
        return product;
    }

    public void addProduct(Product product) {
        Connection conn = connectionClass.CONN();
        if (conn == null) {
            Log.e("ProductDAO", "Connection is null in addProduct");
            return;
        }

        try {
            String query = "INSERT INTO Product (name, description, stock_quantity, image_url, created_at, updated_at, product_code, specifications, color, weight, dimensions, price, cost, quantity_in_stock, warranty_period, origin_country, release_date, qr_code, is_active, category_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, product.getName());
            stmt.setString(2, product.getDescription());
            stmt.setInt(3, product.getStockQuantity());
            stmt.setString(4, product.getImageUrl());
            stmt.setString(5, product.getCreatedAt());
            stmt.setString(6, product.getUpdatedAt());
            stmt.setString(7, product.getProductCode());
            stmt.setString(8, product.getSpecifications());
            stmt.setString(9, product.getColor());
            if (product.getWeight() != null) stmt.setDouble(10, product.getWeight()); else stmt.setNull(10, java.sql.Types.DOUBLE);
            stmt.setString(11, product.getDimensions());
            stmt.setDouble(12, product.getPrice());
            if (product.getCost() != null) stmt.setDouble(13, product.getCost()); else stmt.setNull(13, java.sql.Types.DOUBLE);
            stmt.setInt(14, product.getQuantityInStock());
            if (product.getWarrantyPeriod() != null) stmt.setInt(15, product.getWarrantyPeriod()); else stmt.setNull(15, java.sql.Types.INTEGER);
            stmt.setString(16, product.getOriginCountry());
            stmt.setString(17, product.getReleaseDate());
            stmt.setString(18, product.getQrCode());
            stmt.setBoolean(19, product.isActive());
            if (product.getCategoryId() != null) stmt.setInt(20, product.getCategoryId()); else stmt.setNull(20, java.sql.Types.INTEGER);
            stmt.executeUpdate();
        } catch (Exception e) {
            Log.e("ProductDAO", "Error adding product: " + e.getMessage());
        }
    }

    public void updateProduct(Product product) {
        Connection conn = connectionClass.CONN();
        if (conn == null) {
            Log.e("ProductDAO", "Connection is null in updateProduct");
            return;
        }

        try {
            String query = "UPDATE Product SET name = ?, description = ?, stock_quantity = ?, image_url = ?, updated_at = ?, product_code = ?, specifications = ?, color = ?, weight = ?, dimensions = ?, price = ?, cost = ?, quantity_in_stock = ?, warranty_period = ?, origin_country = ?, release_date = ?, qr_code = ?, is_active = ?, category_id = ? WHERE product_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, product.getName());
            stmt.setString(2, product.getDescription());
            stmt.setInt(3, product.getStockQuantity());
            stmt.setString(4, product.getImageUrl());
            stmt.setString(5, product.getUpdatedAt());
            stmt.setString(6, product.getProductCode());
            stmt.setString(7, product.getSpecifications());
            stmt.setString(8, product.getColor());
            if (product.getWeight() != null) stmt.setDouble(9, product.getWeight()); else stmt.setNull(9, java.sql.Types.DOUBLE);
            stmt.setString(10, product.getDimensions());
            stmt.setDouble(11, product.getPrice());
            if (product.getCost() != null) stmt.setDouble(12, product.getCost()); else stmt.setNull(12, java.sql.Types.DOUBLE);
            stmt.setInt(13, product.getQuantityInStock());
            if (product.getWarrantyPeriod() != null) stmt.setInt(14, product.getWarrantyPeriod()); else stmt.setNull(14, java.sql.Types.INTEGER);
            stmt.setString(15, product.getOriginCountry());
            stmt.setString(16, product.getReleaseDate());
            stmt.setString(17, product.getQrCode());
            stmt.setBoolean(18, product.isActive());
            if (product.getCategoryId() != null) stmt.setInt(19, product.getCategoryId()); else stmt.setNull(19, java.sql.Types.INTEGER);
            stmt.setInt(20, product.getProductId());
            stmt.executeUpdate();
        } catch (Exception e) {
            Log.e("ProductDAO", "Error updating product: " + e.getMessage());
        }
    }

    public void deleteProduct(int productId) {
        Connection conn = connectionClass.CONN();
        if (conn == null) {
            Log.e("ProductDAO", "Connection is null in deleteProduct");
            return;
        }

        try {
            String query = "DELETE FROM Product WHERE product_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, productId);
            stmt.executeUpdate();
        } catch (Exception e) {
            Log.e("ProductDAO", "Error deleting product: " + e.getMessage());
        }
    }

    public List<Product> searchProducts(String searchTerm) {
        List<Product> products = new ArrayList<>();
        Connection conn = connectionClass.CONN();
        if (conn == null) {
            Log.e("ProductDAO", "Connection is null in searchProducts");
            return products;
        }

        try {
            String query = "SELECT * FROM Product WHERE name LIKE ? OR product_code LIKE ? OR description LIKE ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            String likeSearch = "%" + searchTerm + "%";
            stmt.setString(1, likeSearch);
            stmt.setString(2, likeSearch);
            stmt.setString(3, likeSearch);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                products.add(extractProduct(rs));
            }
        } catch (Exception e) {
            Log.e("ProductDAO", "Error searching products: " + e.getMessage());
        }
        return products;
    }

    private Product extractProduct(ResultSet rs) {
        try {
            System.out.println("ProductDAO: Extracting product from ResultSet");

            Product p = new Product();

            // Sửa lại tên cột cho đúng với database
            int productId = rs.getInt("product_id");
            String name = rs.getString("name");
            String description = rs.getString("description");
            int stockQuantity = rs.getInt("stock_quantity");
            String imageUrl = rs.getString("image_url");
            String createdAt = rs.getString("created_at");
            String updatedAt = rs.getString("updated_at");
            String productCode = rs.getString("product_code");
            String specifications = rs.getString("specifications");
            String color = rs.getString("color");
            Double weight = rs.getObject("weight") != null ? rs.getDouble("weight") : null;
            String dimensions = rs.getString("dimensions");
            double price = rs.getDouble("price");
            Double cost = rs.getObject("cost") != null ? rs.getDouble("cost") : null;
            int quantityInStock = rs.getInt("quantity_in_stock");
            Integer warrantyPeriod = rs.getObject("warranty_period") != null ? rs.getInt("warranty_period") : null;
            String originCountry = rs.getString("origin_country");
            String releaseDate = rs.getString("release_date");
            String qrCode = rs.getString("qr_code");
            boolean isActive = rs.getBoolean("is_active");
            Integer categoryId = rs.getObject("category_id") != null ? rs.getInt("category_id") : null;
            String category = rs.getString("category_name");

            p.setProductId(productId);
            p.setName(name);
            p.setDescription(description);
            p.setStockQuantity(stockQuantity);
            p.setImageUrl(imageUrl);
            p.setCreatedAt(createdAt);
            p.setUpdatedAt(updatedAt);
            p.setProductCode(productCode);
            p.setSpecifications(specifications);
            p.setColor(color);
            p.setWeight(weight);
            p.setDimensions(dimensions);
            p.setPrice(price);
            p.setCost(cost);
            p.setQuantityInStock(quantityInStock);
            p.setWarrantyPeriod(warrantyPeriod);
            p.setOriginCountry(originCountry);
            p.setReleaseDate(releaseDate);
            p.setQrCode(qrCode);
            p.setActive(isActive);
            p.setCategoryId(categoryId);
            p.setCategory(category);

            System.out.println("ProductDAO: Product object created successfully");
            return p;

        } catch (Exception e) {
            System.out.println("ProductDAO: ERROR extracting product: " + e.getMessage());
            Log.e("ProductDAO", "Error extracting product: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
