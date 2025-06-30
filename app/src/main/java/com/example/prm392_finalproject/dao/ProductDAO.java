package com.example.prm392_finalproject.dao;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.prm392_finalproject.db.ConnectionClass;
import com.example.prm392_finalproject.models.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductDAO {
    private static final String TAG = "ProductDAO";
    private ConnectionClass connectionClass;
    private ExecutorService executor;
    private Handler mainHandler;

    public ProductDAO() {
        connectionClass = new ConnectionClass();
        executor = Executors.newFixedThreadPool(3); // Pool of 3 threads for DB operations
        mainHandler = new Handler(Looper.getMainLooper());
    }

    // Callback interfaces for async operations
    public interface ProductListCallback {
        void onSuccess(List<Product> products);
        void onError(String error);
    }

    public interface ProductCallback {
        void onSuccess(Product product);
        void onError(String error);
    }

    public interface OperationCallback {
        void onSuccess();
        void onError(String error);
    }

    // Async method to get all products
    public void getAllProductsAsync(ProductListCallback callback) {
        executor.execute(() -> {
            List<Product> products = new ArrayList<>();
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                Log.d(TAG, "Starting getAllProducts operation");

                conn = connectionClass.CONN();
                if (conn == null) {
                    Log.e(TAG, "Connection is null in getAllProducts");
                    postError(callback, "Database connection failed");
                    return;
                }

                Log.d(TAG, "Database connection successful");

                String query = "SELECT * FROM Product ORDER BY name";
                stmt = conn.prepareStatement(query);
                rs = stmt.executeQuery();

                Log.d(TAG, "Query executed, processing results...");

                int count = 0;
                while (rs.next()) {
                    Product product = extractProduct(rs);
                    if (product != null) {
                        products.add(product);
                        count++;
                    }
                }

                Log.i(TAG, "Fetched " + count + " products from database");
                postSuccess(callback, products);

            } catch (SQLException e) {
                Log.e(TAG, "SQL Error in getAllProducts: " + e.getMessage(), e);
                postError(callback, "Database query failed: " + e.getMessage());
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error in getAllProducts: " + e.getMessage(), e);
                postError(callback, "Unexpected error: " + e.getMessage());
            } finally {
                closeResources(rs, stmt, conn);
            }
        });
    }

    // Async method to get product by ID
    public void getProductByIdAsync(int productId, ProductCallback callback) {
        executor.execute(() -> {
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                conn = connectionClass.CONN();
                if (conn == null) {
                    Log.e(TAG, "Connection is null in getProductById");
                    postError(callback, "Database connection failed");
                    return;
                }

                String query = "SELECT * FROM Product WHERE product_id = ?";
                stmt = conn.prepareStatement(query);
                stmt.setInt(1, productId);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    Product product = extractProduct(rs);
                    if (product != null) {
                        postSuccess(callback, product);
                    } else {
                        postError(callback, "Failed to extract product data");
                    }
                } else {
                    postError(callback, "Product not found with ID: " + productId);
                }

            } catch (SQLException e) {
                Log.e(TAG, "SQL Error in getProductById: " + e.getMessage(), e);
                postError(callback, "Database query failed: " + e.getMessage());
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error in getProductById: " + e.getMessage(), e);
                postError(callback, "Unexpected error: " + e.getMessage());
            } finally {
                closeResources(rs, stmt, conn);
            }
        });
    }

    // Async method to add product
    public void addProductAsync(Product product, OperationCallback callback) {
        executor.execute(() -> {
            Connection conn = null;
            PreparedStatement stmt = null;

            try {
                conn = connectionClass.CONN();
                if (conn == null) {
                    Log.e(TAG, "Connection is null in addProduct");
                    postError(callback, "Database connection failed");
                    return;
                }

                // Fixed: Use correct column names from your database schema
                String query = "INSERT INTO Product (name, description, stock_quantity, product_code, price, quantity_in_stock, is_active, category_id, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, product.getName());
                stmt.setString(2, product.getDescription());
                stmt.setInt(3, product.getQuantityInStock()); // stock_quantity
                stmt.setString(4, product.getProductCode());
                stmt.setDouble(5, product.getPrice());
                stmt.setInt(6, product.getQuantityInStock()); // quantity_in_stock
                stmt.setBoolean(7, product.isActive());
                stmt.setInt(8, product.getCategoryId() != null ? product.getCategoryId() : 1);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    Log.i(TAG, "Product added successfully: " + product.getName());
                    postSuccess(callback);
                } else {
                    postError(callback, "Failed to add product - no rows affected");
                }

            } catch (SQLException e) {
                Log.e(TAG, "SQL Error in addProduct: " + e.getMessage(), e);
                postError(callback, "Database insert failed: " + e.getMessage());
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error in addProduct: " + e.getMessage(), e);
                postError(callback, "Unexpected error: " + e.getMessage());
            } finally {
                closeResources(null, stmt, conn);
            }
        });
    }

    // Async method to update product
    public void updateProductAsync(Product product, OperationCallback callback) {
        executor.execute(() -> {
            Connection conn = null;
            PreparedStatement stmt = null;

            try {
                conn = connectionClass.CONN();
                if (conn == null) {
                    Log.e(TAG, "Connection is null in updateProduct");
                    postError(callback, "Database connection failed");
                    return;
                }

                // Fixed: Use correct column names from your database schema
                String query = "UPDATE Product SET name = ?, description = ?, stock_quantity = ?, product_code = ?, price = ?, quantity_in_stock = ?, is_active = ?, category_id = ?, updated_at = NOW() WHERE product_id = ?";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, product.getName());
                stmt.setString(2, product.getDescription());
                stmt.setInt(3, product.getQuantityInStock()); // stock_quantity
                stmt.setString(4, product.getProductCode());
                stmt.setDouble(5, product.getPrice());
                stmt.setInt(6, product.getQuantityInStock()); // quantity_in_stock
                stmt.setBoolean(7, product.isActive());
                stmt.setInt(8, product.getCategoryId() != null ? product.getCategoryId() : 1);
                stmt.setInt(9, product.getProductId()); // WHERE product_id = ?

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    Log.i(TAG, "Product updated successfully: " + product.getName());
                    postSuccess(callback);
                } else {
                    postError(callback, "Product not found or no changes made");
                }

            } catch (SQLException e) {
                Log.e(TAG, "SQL Error in updateProduct: " + e.getMessage(), e);
                postError(callback, "Database update failed: " + e.getMessage());
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error in updateProduct: " + e.getMessage(), e);
                postError(callback, "Unexpected error: " + e.getMessage());
            } finally {
                closeResources(null, stmt, conn);
            }
        });
    }

    // Async method to delete product
    public void deleteProductAsync(int productId, OperationCallback callback) {
        executor.execute(() -> {
            Connection conn = null;
            PreparedStatement stmt = null;

            try {
                conn = connectionClass.CONN();
                if (conn == null) {
                    Log.e(TAG, "Connection is null in deleteProduct");
                    postError(callback, "Database connection failed");
                    return;
                }

                String query = "DELETE FROM Product WHERE product_id = ?";
                stmt = conn.prepareStatement(query);
                stmt.setInt(1, productId);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    Log.i(TAG, "Product deleted successfully: ID " + productId);
                    postSuccess(callback);
                } else {
                    postError(callback, "Product not found with ID: " + productId);
                }

            } catch (SQLException e) {
                Log.e(TAG, "SQL Error in deleteProduct: " + e.getMessage(), e);
                postError(callback, "Database delete failed: " + e.getMessage());
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error in deleteProduct: " + e.getMessage(), e);
                postError(callback, "Unexpected error: " + e.getMessage());
            } finally {
                closeResources(null, stmt, conn);
            }
        });
    }

    // Async method to search products
    public void searchProductsAsync(String searchTerm, ProductListCallback callback) {
        executor.execute(() -> {
            List<Product> products = new ArrayList<>();
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                conn = connectionClass.CONN();
                if (conn == null) {
                    Log.e(TAG, "Connection is null in searchProducts");
                    postError(callback, "Database connection failed");
                    return;
                }

                // Fixed: Use correct column names from your database schema
                String query = "SELECT * FROM Product WHERE (name LIKE ? OR product_code LIKE ? OR description LIKE ?) ORDER BY name";
                stmt = conn.prepareStatement(query);
                String likeSearch = "%" + searchTerm + "%";
                stmt.setString(1, likeSearch);
                stmt.setString(2, likeSearch);
                stmt.setString(3, likeSearch);

                rs = stmt.executeQuery();
                while (rs.next()) {
                    Product product = extractProduct(rs);
                    if (product != null) {
                        products.add(product);
                    }
                }

                Log.i(TAG, "Search found " + products.size() + " products for term: " + searchTerm);
                postSuccess(callback, products);

            } catch (SQLException e) {
                Log.e(TAG, "SQL Error in searchProducts: " + e.getMessage(), e);
                postError(callback, "Database search failed: " + e.getMessage());
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error in searchProducts: " + e.getMessage(), e);
                postError(callback, "Unexpected error: " + e.getMessage());
            } finally {
                closeResources(rs, stmt, conn);
            }
        });
    }

    // Helper method to extract Product from ResultSet
    private Product extractProduct(ResultSet rs) throws SQLException {
        try {
            Product product = new Product();

            // Fixed: Use correct column names from your database schema
            product.setProductId(rs.getInt("product_id"));
            product.setName(rs.getString("name"));
            product.setDescription(rs.getString("description"));
            product.setPrice(rs.getDouble("price"));
            product.setStockQuantity(rs.getInt("stock_quantity"));
            product.setQuantityInStock(rs.getInt("quantity_in_stock"));
            product.setProductCode(rs.getString("product_code"));
            product.setActive(rs.getBoolean("is_active"));
            product.setCategoryId(rs.getInt("category_id"));

            // Handle nullable columns safely
            String imageUrl = rs.getString("image_url");
            product.setImageUrl(imageUrl != null ? imageUrl : "");

            String createdAt = rs.getString("created_at");
            product.setCreatedAt(createdAt != null ? createdAt : "");

            String updatedAt = rs.getString("updated_at");
            product.setUpdatedAt(updatedAt != null ? updatedAt : "");

            String specifications = rs.getString("specifications");
            product.setSpecifications(specifications != null ? specifications : "");

            String color = rs.getString("color");
            product.setColor(color != null ? color : "");

            // Handle DECIMAL columns (might be null) - compatible with older JDBC
            double weightValue = rs.getDouble("weight");
            if (!rs.wasNull()) {
                product.setWeight(weightValue);
            } else {
                product.setWeight(null);
            }

            String dimensions = rs.getString("dimensions");
            product.setDimensions(dimensions != null ? dimensions : "");

            double costValue = rs.getDouble("cost");
            if (!rs.wasNull()) {
                product.setCost(costValue);
            } else {
                product.setCost(null);
            }

            // Handle INTEGER columns (might be null) - compatible with older JDBC
            int warrantyValue = rs.getInt("warranty_period");
            if (!rs.wasNull()) {
                product.setWarrantyPeriod(warrantyValue);
            } else {
                product.setWarrantyPeriod(null);
            }

            String originCountry = rs.getString("origin_country");
            product.setOriginCountry(originCountry != null ? originCountry : "");

            String releaseDate = rs.getString("release_date");
            product.setReleaseDate(releaseDate != null ? releaseDate : "");

            String qrCode = rs.getString("qr_code");
            product.setQrCode(qrCode != null ? qrCode : "");

            return product;
        } catch (SQLException e) {
            Log.e(TAG, "Error extracting product from ResultSet: " + e.getMessage(), e);
            throw e; // Re-throw to be handled by calling method
        }
    }

    // Helper methods to post results to main thread
    private void postSuccess(ProductListCallback callback, List<Product> products) {
        mainHandler.post(() -> callback.onSuccess(products));
    }

    private void postSuccess(ProductCallback callback, Product product) {
        mainHandler.post(() -> callback.onSuccess(product));
    }

    private void postSuccess(OperationCallback callback) {
        mainHandler.post(callback::onSuccess);
    }

    private void postError(ProductListCallback callback, String error) {
        mainHandler.post(() -> callback.onError(error));
    }

    private void postError(ProductCallback callback, String error) {
        mainHandler.post(() -> callback.onError(error));
    }

    private void postError(OperationCallback callback, String error) {
        mainHandler.post(() -> callback.onError(error));
    }

    // Helper method to properly close database resources
    private void closeResources(ResultSet rs, PreparedStatement stmt, Connection conn) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            Log.e(TAG, "Error closing database resources: " + e.getMessage(), e);
        }
    }

    // Clean up method - call this when done with the DAO
    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}