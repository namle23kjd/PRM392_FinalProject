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
    public boolean createShipping(Shipping shipping) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = connectionClass.CONN();
            if (connection == null) {
                Log.e(TAG, "Database connection failed");
                return false;
            }

            String query = "INSERT INTO shipping (order_id, shipping_address, shipping_method, " +
                    "shipping_person_name, tracking_number, expected_delivery, description, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, shipping.getOrderId());
            preparedStatement.setString(2, shipping.getShippingAddress());
            preparedStatement.setString(3, shipping.getShippingMethod());
            preparedStatement.setString(4, shipping.getShippingPersonName());
            preparedStatement.setString(5, shipping.getTrackingNumber());

            // Format date for MySQL
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String expectedDeliveryString = shipping.getExpectedDelivery() != null ?
                    sdf.format(shipping.getExpectedDelivery()) : null;
            preparedStatement.setString(6, expectedDeliveryString);

            preparedStatement.setString(7, shipping.getDescription());
            preparedStatement.setString(8, shipping.getStatus());

            int result = preparedStatement.executeUpdate();

            if (result > 0) {
                Log.d(TAG, "Shipping created successfully for order: " + shipping.getOrderId());
                return true;
            } else {
                Log.e(TAG, "Failed to create shipping record");
                return false;
            }

        } catch (SQLException e) {
            Log.e(TAG, "SQL Error during shipping creation: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error during shipping creation: " + e.getMessage());
            e.printStackTrace();
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

    // Update shipping information
    public boolean updateShipping(Shipping shipping) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = connectionClass.CONN();
            if (connection == null) return false;

            String query = "UPDATE shipping SET shipping_address=?, shipping_method=?, " +
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

            String query = "UPDATE shipping SET status=?" +
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

            String query = "SELECT * FROM shipping WHERE shipping_id = ?";
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

            String query = "SELECT * FROM shipping WHERE order_id = ?";
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

            String query = "SELECT * FROM shipping WHERE tracking_number = ?";
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

            String query = "SELECT * FROM shipping ORDER BY shipping_id DESC";
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

            String query = "SELECT * FROM shipping WHERE status = ? ORDER BY shipping_id DESC";
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

            String query = "SELECT * FROM shipping WHERE expected_delivery < NOW() AND status != 'delivered' " +
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