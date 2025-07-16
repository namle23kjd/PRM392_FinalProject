package com.example.prm392_finalproject.controllers;

import android.util.Log;
import com.example.prm392_finalproject.db.ConnectionClass;
import com.example.prm392_finalproject.models.CustomerOrder;
import com.example.prm392_finalproject.dao.ProductDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerOrderRepository {
    private static final String TAG = "CustomerOrderRepository";
    private ConnectionClass connectionClass;

    public CustomerOrderRepository() {
        connectionClass = new ConnectionClass();
    }

    // Get all orders for a specific customer
    public List<CustomerOrder> getOrdersByCustomerId(int customerId) {
        List<CustomerOrder> orders = new ArrayList<>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            Log.d(TAG, "Getting orders for customer ID: " + customerId);
            connection = connectionClass.CONN();
            if (connection == null) {
                Log.e(TAG, "Database connection is null");
                return orders;
            }

            String query = "SELECT o.*, s.shipping_address, s.shipping_method, s.tracking_number, " +
                    "s.expected_delivery, s.delivered_date, s.status as shipping_status " +
                    "FROM Orders o " +
                    "LEFT JOIN Shipping s ON o.order_id = s.order_id " +
                    "WHERE o.customer_id = ? " +
                    "ORDER BY o.order_date DESC";

            Log.d(TAG, "Executing query: " + query + " with customer_id = " + customerId);
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, customerId);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                CustomerOrder order = mapResultSetToCustomerOrder(resultSet);
                if (order != null) {
                    orders.add(order);
                    Log.d(TAG, "Added order: " + order.getOrderId() + " with status: " + order.getStatus());
                }
            }

            Log.d(TAG, "Found " + orders.size() + " orders for customer " + customerId);
            return orders;

        } catch (SQLException e) {
            Log.e(TAG, "SQL Error getting orders by customer ID: " + e.getMessage());
            e.printStackTrace();
            return orders;
        } catch (Exception e) {
            Log.e(TAG, "General Error getting orders by customer ID: " + e.getMessage());
            e.printStackTrace();
            return orders;
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                Log.e(TAG, "Error closing resources: " + e.getMessage());
            }
        }
    }

    // Get orders by customer ID and status filter
    public List<CustomerOrder> getOrdersByCustomerIdAndStatus(int customerId, String statusFilter) {
        List<CustomerOrder> orders = new ArrayList<>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = connectionClass.CONN();
            if (connection == null) return orders;

            // Parse status filter (can be comma-separated)
            String[] statuses = statusFilter.split(",");
            StringBuilder placeholders = new StringBuilder();
            for (int i = 0; i < statuses.length; i++) {
                if (i > 0) placeholders.append(",");
                placeholders.append("?");
            }

            String query = "SELECT o.*, s.shipping_address, s.shipping_method, s.tracking_number, " +
                    "s.expected_delivery, s.delivered_date, s.status as shipping_status " +
                    "FROM Orders o " +
                    "LEFT JOIN Shipping s ON o.order_id = s.order_id " +
                    "WHERE o.customer_id = ? AND (o.status IN (" + placeholders + ") " +
                    "OR s.status IN (" + placeholders + ")) " +
                    "ORDER BY o.order_date DESC";

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, customerId);

            // Set status parameters twice (for order status and shipping status)
            int paramIndex = 2;
            for (String status : statuses) {
                preparedStatement.setString(paramIndex++, status.trim());
            }
            for (String status : statuses) {
                preparedStatement.setString(paramIndex++, status.trim());
            }

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                CustomerOrder order = mapResultSetToCustomerOrder(resultSet);
                orders.add(order);
            }

            return orders;

        } catch (SQLException e) {
            Log.e(TAG, "SQL Error filtering orders: " + e.getMessage());
            return orders;
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                Log.e(TAG, "Error closing resources: " + e.getMessage());
            }
        }
    }

    // Get specific order by order ID for customer
    public CustomerOrder getOrderByIdForCustomer(int orderId, int customerId) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = connectionClass.CONN();
            if (connection == null) return null;

            String query = "SELECT o.*, s.shipping_address, s.shipping_method, s.tracking_number, " +
                    "s.expected_delivery, s.delivered_date, s.status as shipping_status " +
                    "FROM Orders o " +
                    "LEFT JOIN Shipping s ON o.order_id = s.order_id " +
                    "WHERE o.order_id = ? AND o.customer_id = ?";

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, orderId);
            preparedStatement.setInt(2, customerId);

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return mapResultSetToCustomerOrder(resultSet);
            }

            return null;

        } catch (SQLException e) {
            Log.e(TAG, "SQL Error getting order by ID: " + e.getMessage());
            return null;
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                Log.e(TAG, "Error closing resources: " + e.getMessage());
            }
        }
    }

    // Get order items for a specific order
    public List<CustomerOrder.OrderItem> getOrderItems(int orderId) {
        List<CustomerOrder.OrderItem> items = new ArrayList<>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = connectionClass.CONN();
            if (connection == null) return items;

            String query = "SELECT oi.*, p.name as product_name, p.image_url " +
                    "FROM Order_Items oi " +
                    "JOIN Product p ON oi.product_id = p.product_id " +
                    "WHERE oi.order_id = ?";

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, orderId);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                CustomerOrder.OrderItem item = new CustomerOrder.OrderItem();
                item.setProductId(resultSet.getInt("product_id"));
                item.setProductName(resultSet.getString("product_name"));
                item.setQuantity(resultSet.getInt("quantity"));
                item.setUnitPrice(resultSet.getDouble("unit_price"));
                item.setTotalPrice(resultSet.getDouble("total_price"));
                item.setImageUrl(resultSet.getString("image_url"));
                items.add(item);
            }

            return items;

        } catch (SQLException e) {
            Log.e(TAG, "SQL Error getting order items: " + e.getMessage());
            return items;
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                Log.e(TAG, "Error closing resources: " + e.getMessage());
            }
        }
    }

    // Helper method to map ResultSet to CustomerOrder object
    private CustomerOrder mapResultSetToCustomerOrder(ResultSet resultSet) throws SQLException {
        try {
            CustomerOrder order = new CustomerOrder();

            // Order basic info
            int orderId = resultSet.getInt("order_id");
            int customerId = resultSet.getInt("customer_id");
            String status = resultSet.getString("status");
            double totalAmount = resultSet.getDouble("total_amount");
            
            order.setOrderId(orderId);
            order.setCustomerId(customerId);
            order.setOrderDate(resultSet.getTimestamp("order_date"));
            order.setStatus(status);
            order.setTotalAmount(totalAmount);
            order.setNote(resultSet.getString("note"));

            Log.d(TAG, "Mapped order: ID=" + orderId + ", Customer=" + customerId + ", Status=" + status + ", Amount=" + totalAmount);

            // Shipping info (if exists)
            String shippingAddress = resultSet.getString("shipping_address");
            if (shippingAddress != null) {
                order.setShippingAddress(shippingAddress);
                order.setShippingMethod(resultSet.getString("shipping_method"));
                order.setTrackingNumber(resultSet.getString("tracking_number"));
                order.setExpectedDelivery(resultSet.getTimestamp("expected_delivery"));
                order.setDeliveredDate(resultSet.getTimestamp("delivered_date"));

                // Use shipping status if available, otherwise use order status
                String shippingStatus = resultSet.getString("shipping_status");
                if (shippingStatus != null && !shippingStatus.trim().isEmpty()) {
                    order.setShippingStatus(shippingStatus);
                    Log.d(TAG, "Order " + orderId + " has shipping status: " + shippingStatus);
                }
                
                Log.d(TAG, "Order " + orderId + " has shipping address: " + shippingAddress);
            } else {
                Log.d(TAG, "Order " + orderId + " has no shipping info");
            }

            return order;
        } catch (Exception e) {
            Log.e(TAG, "Error mapping ResultSet to CustomerOrder: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Get customer order statistics
    public CustomerOrder.OrderStats getCustomerOrderStats(int customerId) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = connectionClass.CONN();
            if (connection == null) return new CustomerOrder.OrderStats();

            String query = "SELECT " +
                    "COUNT(*) as total_orders, " +
                    "SUM(CASE WHEN o.status IN ('Pending', 'Processing') OR s.status IN ('Pending', 'Processing') THEN 1 ELSE 0 END) as pending_orders, " +
                    "SUM(CASE WHEN o.status IN ('Completed', 'Delivered') OR s.status IN ('Delivered') THEN 1 ELSE 0 END) as completed_orders, " +
                    "SUM(CASE WHEN s.status = 'Shipped' THEN 1 ELSE 0 END) as shipping_orders, " +
                    "SUM(o.total_amount) as total_spent " +
                    "FROM Orders o " +
                    "LEFT JOIN Shipping s ON o.order_id = s.order_id " +
                    "WHERE o.customer_id = ?";

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, customerId);

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                CustomerOrder.OrderStats stats = new CustomerOrder.OrderStats();
                stats.setTotalOrders(resultSet.getInt("total_orders"));
                stats.setPendingOrders(resultSet.getInt("pending_orders"));
                stats.setCompletedOrders(resultSet.getInt("completed_orders"));
                stats.setShippingOrders(resultSet.getInt("shipping_orders"));
                stats.setTotalSpent(resultSet.getDouble("total_spent"));
                return stats;
            }

            return new CustomerOrder.OrderStats();

        } catch (SQLException e) {
            Log.e(TAG, "SQL Error getting customer stats: " + e.getMessage());
            return new CustomerOrder.OrderStats();
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                Log.e(TAG, "Error closing resources: " + e.getMessage());
            }
        }
    }

    // Tạo đơn hàng mới cho customer
    public boolean createOrderForCustomer(int customerId, List<CustomerOrder.OrderItem> items, String note, String shippingAddress, String shippingMethod, String shippingPersonName, double totalAmount) {
        Connection connection = null;
        PreparedStatement orderStmt = null;
        PreparedStatement itemStmt = null;
        PreparedStatement shippingStmt = null;
        ResultSet generatedKeys = null;
        try {
            connection = connectionClass.CONN();
            if (connection == null) return false;
            connection.setAutoCommit(false); // Transaction

            // Sử dụng totalAmount đã được truyền vào (đã bao gồm discount)
            // Không cần tính lại vì đã được tính sẵn từ CreateOrderActivity

            // 2. Insert vào Orders
            String orderSql = "INSERT INTO Orders (customer_id, order_date, status, total_amount, note) VALUES (?, NOW(), 'pending', ?, ?)";
            orderStmt = connection.prepareStatement(orderSql, PreparedStatement.RETURN_GENERATED_KEYS);
            orderStmt.setInt(1, customerId);
            orderStmt.setDouble(2, totalAmount);
            orderStmt.setString(3, note);
            int affectedRows = orderStmt.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Creating order failed, no rows affected.");

            generatedKeys = orderStmt.getGeneratedKeys();
            int orderId = -1;
            if (generatedKeys.next()) {
                orderId = generatedKeys.getInt(1);
            } else {
                throw new SQLException("Creating order failed, no ID obtained.");
            }

            // 3. Insert từng item vào Order_Items
            String itemSql = "INSERT INTO Order_Items (order_id, product_id, quantity, unit_price, total_price) VALUES (?, ?, ?, ?, ?)";
            itemStmt = connection.prepareStatement(itemSql);
            for (CustomerOrder.OrderItem item : items) {
                itemStmt.setInt(1, orderId);
                itemStmt.setInt(2, item.getProductId());
                itemStmt.setInt(3, item.getQuantity());
                itemStmt.setDouble(4, item.getUnitPrice());
                itemStmt.setDouble(5, item.getUnitPrice() * item.getQuantity());
                itemStmt.addBatch();
            }
            itemStmt.executeBatch();

            // 4. Insert vào Shipping
            String shippingSql = "INSERT INTO Shipping (order_id, shipping_address, shipping_method, shipping_person_name, status) VALUES (?, ?, ?, ?, 'Pending')";
            shippingStmt = connection.prepareStatement(shippingSql);
            shippingStmt.setInt(1, orderId);
            shippingStmt.setString(2, shippingAddress);
            shippingStmt.setString(3, shippingMethod);
            shippingStmt.setString(4, shippingPersonName);
            shippingStmt.executeUpdate();

            connection.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            try { if (connection != null) connection.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            return false;
        } finally {
            try { if (generatedKeys != null) generatedKeys.close(); } catch (Exception e) {}
            try { if (orderStmt != null) orderStmt.close(); } catch (Exception e) {}
            try { if (itemStmt != null) itemStmt.close(); } catch (Exception e) {}
            try { if (shippingStmt != null) shippingStmt.close(); } catch (Exception e) {}
            try { if (connection != null) connection.setAutoCommit(true); } catch (Exception e) {}
            try { if (connection != null) connection.close(); } catch (Exception e) {}
        }
    }
}