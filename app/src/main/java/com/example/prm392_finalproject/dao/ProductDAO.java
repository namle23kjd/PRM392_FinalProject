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

        System.out.println("ProductDAO: getAllProducts() started");

        Connection conn = connectionClass.CONN();
        if (conn == null) {
            System.out.println("ProductDAO: ERROR - Connection is NULL");
            Log.e("ProductDAO", "Connection is null in getAllProducts");
            return products;
        }

        System.out.println("ProductDAO: Database connection successful");

        try {
            String query = "SELECT * FROM Product";
            System.out.println("ProductDAO: Executing query: " + query);

            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            System.out.println("ProductDAO: Query executed, processing results...");

            int count = 0;
            while (rs.next()) {
                System.out.println("ProductDAO: Processing row " + (count + 1));

                Product p = extractProduct(rs);
                if (p != null) {
                    products.add(p);
                    count++;
                    System.out.println("ProductDAO: Added product: ID=" + p.getProductId() +
                            ", Name=" + p.getName() + ", Code=" + p.getProductCode());
                } else {
                    System.out.println("ProductDAO: Failed to extract product from row " + (count + 1));
                }
            }

            System.out.println("ProductDAO: Finished processing. Total products: " + count);
            Log.i("ProductDAO", "Fetched " + count + " products from DB.");

        } catch (Exception e) {
            System.out.println("ProductDAO: EXCEPTION in getAllProducts: " + e.getMessage());
            Log.e("ProductDAO", "Error fetching all products: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("ProductDAO: Returning " + products.size() + " products");
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
            String query = "SELECT * FROM Product WHERE productId = ?";
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
            String query = "INSERT INTO Product (name, productCode, price, quantityInStock, description, active) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, product.getName());
            stmt.setString(2, product.getProductCode());
            stmt.setDouble(3, product.getPrice());
            stmt.setInt(4, product.getQuantityInStock());
            stmt.setString(5, product.getDescription());
            stmt.setInt(6, product.isActive() ? 1 : 0);
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
            String query = "UPDATE Product SET name = ?, productCode = ?, price = ?, quantityInStock = ?, description = ?, active = ? WHERE productId = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, product.getName());
            stmt.setString(2, product.getProductCode());
            stmt.setDouble(3, product.getPrice());
            stmt.setInt(4, product.getQuantityInStock());
            stmt.setString(5, product.getDescription());
            stmt.setInt(6, product.isActive() ? 1 : 0);
            stmt.setInt(7, product.getProductId());
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
            String query = "DELETE FROM Product WHERE productId = ?";
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
            String query = "SELECT * FROM Product WHERE name LIKE ? OR productCode LIKE ? OR description LIKE ?";
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

            // Debug từng field
            int productId = rs.getInt("productId");
            String name = rs.getString("name");
            String productCode = rs.getString("productCode");
            double price = rs.getDouble("price");
            int quantityInStock = rs.getInt("quantityInStock");
            String description = rs.getString("description");
            boolean active = rs.getInt("active") == 1;

            System.out.println("ProductDAO: Extracted data - ID:" + productId +
                    ", Name:" + name + ", Code:" + productCode + ", Price:" + price);

            p.setProductId(productId);
            p.setName(name);
            p.setProductCode(productCode);
            p.setPrice(price);
            p.setQuantityInStock(quantityInStock);
            p.setDescription(description);
            p.setActive(active);

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
