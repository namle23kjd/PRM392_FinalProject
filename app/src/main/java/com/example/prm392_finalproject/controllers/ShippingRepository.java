package com.example.prm392_finalproject.controllers;

import android.util.Log;
import com.example.prm392_finalproject.db.ConnectionClass;
import com.example.prm392_finalproject.models.Shipping;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ShippingRepository {
    private static final String TAG = "ShippingRepository";
    private ConnectionClass connectionClass;

    public ShippingRepository() {
        connectionClass = new ConnectionClass();
    }

    // Create new shipping record
    // Thêm method createShipping với debug logs chi tiết
    public boolean createShipping(Shipping shipping) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            Log.d(TAG, "=== DEBUG: Starting createShipping ===");
            Log.d(TAG, "Input Shipping data:");
            Log.d(TAG, "- OrderId: " + shipping.getOrderId());
            Log.d(TAG, "- Address: " + shipping.getShippingAddress());
            Log.d(TAG, "- Method: " + shipping.getShippingMethod());
            Log.d(TAG, "- PersonName: " + shipping.getShippingPersonName());
            Log.d(TAG, "- TrackingNumber: " + shipping.getTrackingNumber());
            Log.d(TAG, "- ExpectedDelivery: " + shipping.getExpectedDelivery());
            Log.d(TAG, "- Description: " + shipping.getDescription());
            Log.d(TAG, "- Status: " + shipping.getStatus());

            // Test database connection
            Log.d(TAG, "Attempting to connect to database...");
            connection = connectionClass.CONN();

            if (connection == null) {
                Log.e(TAG, "❌ Database connection is NULL");
                return false;
            }

            Log.d(TAG, "✅ Database connection successful");
            Log.d(TAG, "Connection info: " + connection.toString());

            String query = "INSERT INTO Shipping (order_id, shipping_address, shipping_method, " +
                    "shipping_person_name, tracking_number, expected_delivery, description, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            Log.d(TAG, "SQL Query: " + query);

            preparedStatement = connection.prepareStatement(query);

            // Set parameters với log
            Log.d(TAG, "Setting parameters...");
            preparedStatement.setInt(1, shipping.getOrderId());
            Log.d(TAG, "✅ Param 1 (order_id): " + shipping.getOrderId());

            preparedStatement.setString(2, shipping.getShippingAddress());
            Log.d(TAG, "✅ Param 2 (shipping_address): " + shipping.getShippingAddress());

            preparedStatement.setString(3, shipping.getShippingMethod());
            Log.d(TAG, "✅ Param 3 (shipping_method): " + shipping.getShippingMethod());

            preparedStatement.setString(4, shipping.getShippingPersonName());
            Log.d(TAG, "✅ Param 4 (shipping_person_name): " + shipping.getShippingPersonName());

            preparedStatement.setString(5, shipping.getTrackingNumber());
            Log.d(TAG, "✅ Param 5 (tracking_number): " + shipping.getTrackingNumber());

            // Format date for MySQL
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String expectedDeliveryString = shipping.getExpectedDelivery() != null ?
                    sdf.format(shipping.getExpectedDelivery()) : null;
            preparedStatement.setString(6, expectedDeliveryString);
            Log.d(TAG, "✅ Param 6 (expected_delivery): " + expectedDeliveryString);

            preparedStatement.setString(7, shipping.getDescription());
            Log.d(TAG, "✅ Param 7 (description): " + shipping.getDescription());

            preparedStatement.setString(8, shipping.getStatus());
            Log.d(TAG, "✅ Param 8 (status): " + shipping.getStatus());

            Log.d(TAG, "Executing query...");
            int result = preparedStatement.executeUpdate();
            Log.d(TAG, "Query execution result: " + result + " rows affected");

            if (result > 0) {
                Log.d(TAG, "✅ Shipping created successfully for order: " + shipping.getOrderId());
                return true;
            } else {
                Log.e(TAG, "❌ Failed to create shipping record - 0 rows affected");
                return false;
            }

        } catch (SQLException e) {
            Log.e(TAG, "❌ SQL Error during shipping creation:");
            Log.e(TAG, "Error Code: " + e.getErrorCode());
            Log.e(TAG, "SQL State: " + e.getSQLState());
            Log.e(TAG, "Error Message: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            Log.e(TAG, "❌ General Error during shipping creation: " + e.getMessage());
            Log.e(TAG, "Exception type: " + e.getClass().getSimpleName());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                    Log.d(TAG, "✅ PreparedStatement closed");
                }
                if (connection != null) {
                    connection.close();
                    Log.d(TAG, "✅ Connection closed");
                }
            } catch (SQLException e) {
                Log.e(TAG, "❌ Error closing resources: " + e.getMessage());
            }
            Log.d(TAG, "=== DEBUG: End createShipping ===");
        }
    }

    // Update shipping information
    public boolean updateShipping(Shipping shipping) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = connectionClass.CONN();
            if (connection == null) return false;

            String query = "UPDATE Shipping SET shipping_address=?, shipping_method=?, " +
                    "shipping_person_name=?, expected_delivery=?, description=?, status=? " +
                    "WHERE shipping_id=?";

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, shipping.getShippingAddress());
            preparedStatement.setString(2, shipping.getShippingMethod());
            preparedStatement.setString(3, shipping.getShippingPersonName());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String expectedDeliveryString = shipping.getExpectedDelivery() != null ?
                    sdf.format(shipping.getExpectedDelivery()) : null;
            preparedStatement.setString(4, expectedDeliveryString);

            preparedStatement.setString(5, shipping.getDescription());
            preparedStatement.setString(6, shipping.getStatus());
            preparedStatement.setInt(7, shipping.getShippingId());

            int result = preparedStatement.executeUpdate();

            if (result > 0) {
                Log.d(TAG, "Shipping updated successfully: " + shipping.getShippingId());
                return true;
            }

            return false;

        } catch (SQLException e) {
            Log.e(TAG, "SQL Error updating shipping: " + e.getMessage());
            return false;
        } finally {
            try {
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                Log.e(TAG, "Error closing resources: " + e.getMessage());
            }
        }
    }

    // Update shipping status
    public boolean updateShippingStatus(int shippingId, String status) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = connectionClass.CONN();
            if (connection == null) return false;

            String query = "UPDATE Shipping SET status=?" +
                    (status.equals("delivered") ? ", delivered_date=NOW()" : "") +
                    " WHERE shipping_id=?";

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, status);
            preparedStatement.setInt(2, shippingId);

            int result = preparedStatement.executeUpdate();

            if (result > 0) {
                Log.d(TAG, "Shipping status updated successfully: " + shippingId + " -> " + status);
                return true;
            }

            return false;

        } catch (SQLException e) {
            Log.e(TAG, "SQL Error updating shipping status: " + e.getMessage());
            return false;
        } finally {
            try {
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                Log.e(TAG, "Error closing resources: " + e.getMessage());
            }
        }
    }

    // Get shipping by ID
    public Shipping getShippingById(int shippingId) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = connectionClass.CONN();
            if (connection == null) return null;

            String query = "SELECT * FROM Shipping WHERE shipping_id = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, shippingId);

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return mapResultSetToShipping(resultSet);
            }

            return null;

        } catch (SQLException e) {
            Log.e(TAG, "SQL Error getting shipping by ID: " + e.getMessage());
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

    // Get shipping by order ID
    public Shipping getShippingByOrderId(int orderId) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = connectionClass.CONN();
            if (connection == null) return null;

            String query = "SELECT * FROM Shipping WHERE order_id = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, orderId);

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return mapResultSetToShipping(resultSet);
            }

            return null;

        } catch (SQLException e) {
            Log.e(TAG, "SQL Error getting shipping by order ID: " + e.getMessage());
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

    // Get shipping by tracking number
    public Shipping getShippingByTrackingNumber(String trackingNumber) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = connectionClass.CONN();
            if (connection == null) return null;

            String query = "SELECT * FROM Shipping WHERE tracking_number = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, trackingNumber);

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return mapResultSetToShipping(resultSet);
            }

            return null;

        } catch (SQLException e) {
            Log.e(TAG, "SQL Error getting shipping by tracking number: " + e.getMessage());
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

    // Get all shipping records
    public List<Shipping> getAllShippings() {
        List<Shipping> shippings = new ArrayList<>();
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            connection = connectionClass.CONN();
            if (connection == null) return shippings;

            String query = "SELECT * FROM Shipping ORDER BY shipping_id DESC";
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                shippings.add(mapResultSetToShipping(resultSet));
            }

            return shippings;

        } catch (SQLException e) {
            Log.e(TAG, "SQL Error getting all shippings: " + e.getMessage());
            return shippings;
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                Log.e(TAG, "Error closing resources: " + e.getMessage());
            }
        }
    }

    // Get shipping records by status
    public List<Shipping> getShippingsByStatus(String status) {
        List<Shipping> shippings = new ArrayList<>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = connectionClass.CONN();
            if (connection == null) return shippings;

            String query = "SELECT * FROM Shipping WHERE status = ? ORDER BY shipping_id DESC";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, status);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                shippings.add(mapResultSetToShipping(resultSet));
            }

            return shippings;

        } catch (SQLException e) {
            Log.e(TAG, "SQL Error getting shippings by status: " + e.getMessage());
            return shippings;
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

    // Get overdue shipments
    public List<Shipping> getOverdueShipments() {
        List<Shipping> shippings = new ArrayList<>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = connectionClass.CONN();
            if (connection == null) return shippings;

            String query = "SELECT * FROM Shipping WHERE expected_delivery < NOW() AND status != 'delivered' " +
                    "ORDER BY expected_delivery ASC";
            preparedStatement = connection.prepareStatement(query);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                shippings.add(mapResultSetToShipping(resultSet));
            }

            return shippings;

        } catch (SQLException e) {
            Log.e(TAG, "SQL Error getting overdue shipments: " + e.getMessage());
            return shippings;
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

    // Helper method to map ResultSet to Shipping object
    private Shipping mapResultSetToShipping(ResultSet resultSet) throws SQLException {
        Shipping shipping = new Shipping();
        shipping.setShippingId(resultSet.getInt("shipping_id"));
        shipping.setOrderId(resultSet.getInt("order_id"));
        shipping.setShippingAddress(resultSet.getString("shipping_address"));
        shipping.setShippingMethod(resultSet.getString("shipping_method"));
        shipping.setShippingPersonName(resultSet.getString("shipping_person_name"));
        shipping.setTrackingNumber(resultSet.getString("tracking_number"));
        shipping.setExpectedDelivery(resultSet.getTimestamp("expected_delivery"));
        shipping.setDeliveredDate(resultSet.getTimestamp("delivered_date"));
        shipping.setDescription(resultSet.getString("description"));
        shipping.setStatus(resultSet.getString("status"));

        return shipping;

    }
}