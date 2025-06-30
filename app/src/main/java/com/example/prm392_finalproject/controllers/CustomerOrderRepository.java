package com.example.prm392_finalproject.controllers;

import android.util.Log;
import com.example.prm392_finalproject.db.ConnectionClass;
import com.example.prm392_finalproject.models.CustomerOrder;

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
                    "FROM orders o " +
                    "LEFT JOIN shipping s ON o.order_id = s.order_id " +
                    "WHERE o.customer_id = ? " +
                    "ORDER BY o.order_date DESC";

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, customerId);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                CustomerOrder order = mapResultSetToCustomerOrder(resultSet);
                orders.add(order);
            }

            Log.d(TAG, "Found " + orders.size() + " orders for customer " + customerId);
            return orders;

        } catch (SQLException e) {
            Log.e(TAG, "SQL Error getting orders by customer ID: " + e.getMessage());
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
                    "FROM orders o " +
                    "LEFT JOIN shipping s ON o.order_id = s.order_id " +
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
                    "FROM orders o " +
                    "LEFT JOIN shipping s ON o.order_id = s.order_id " +
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
                    "FROM order_items oi " +
                    "JOIN product p ON oi.product_id = p.product_id " +
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
        CustomerOrder order = new CustomerOrder();

        // Order basic info
        order.setOrderId(resultSet.getInt("order_id"));
        order.setCustomerId(resultSet.getInt("customer_id"));
        order.setOrderDate(resultSet.getTimestamp("order_date"));
        order.setStatus(resultSet.getString("status"));
        order.setTotalAmount(resultSet.getDouble("total_amount"));
        order.setNote(resultSet.getString("note"));

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
            }
        }

        return order;
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
                    "FROM orders o " +
                    "LEFT JOIN shipping s ON o.order_id = s.order_id " +
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
}