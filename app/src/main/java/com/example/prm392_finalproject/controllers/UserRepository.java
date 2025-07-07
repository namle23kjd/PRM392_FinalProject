package com.example.prm392_finalproject.controllers;

import android.util.Log;
import com.example.prm392_finalproject.db.ConnectionClass;
import com.example.prm392_finalproject.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRepository {
    private static final String TAG = "UserRepository";
    private ConnectionClass connectionClass;

    public UserRepository() {
        connectionClass = new ConnectionClass();
    }

    // Register new user
    public boolean registerUser(User user) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        
        try {
            connection = connectionClass.CONN();
            if (connection == null) {
                Log.e(TAG, "Database connection failed");
                return false;
            }

            // Check if email already exists
            if (isEmailExists(user.getEmail())) {
                Log.e(TAG, "Email already exists: " + user.getEmail());
                return false;
            }

            String query = "INSERT INTO User (gender, first_name, last_name, email, phone, address, dob, role, password) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, user.getGender());
            preparedStatement.setString(2, user.getFirstName());
            preparedStatement.setString(3, user.getLastName());
            preparedStatement.setString(4, user.getEmail());
            preparedStatement.setString(5, user.getPhone());
            preparedStatement.setString(6, user.getAddress());
            
            // Format date for MySQL
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String dobString = user.getDob() != null ? sdf.format(user.getDob()) : null;
            preparedStatement.setString(7, dobString);
            
            preparedStatement.setString(8, user.getRole());
            preparedStatement.setString(9, user.getPassword());

            int result = preparedStatement.executeUpdate();
            
            if (result > 0) {
                Log.d(TAG, "User registered successfully: " + user.getEmail());
                return true;
            } else {
                Log.e(TAG, "Failed to register user");
                return false;
            }

        } catch (SQLException e) {
            Log.e(TAG, "SQL Error during registration: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error during registration: " + e.getMessage());
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

    // Login user with email and password
    public User loginUser(String email, String password) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            connection = connectionClass.CONN();
            if (connection == null) return null;

            String query = "SELECT * FROM User WHERE email = ? AND password = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, password);
            
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                User user = new User();
                user.setCustomerId(resultSet.getInt("customer_id"));
                user.setGender(resultSet.getString("gender"));
                user.setFirstName(resultSet.getString("first_name"));
                user.setLastName(resultSet.getString("last_name"));
                user.setEmail(resultSet.getString("email"));
                user.setPhone(resultSet.getString("phone"));
                user.setAddress(resultSet.getString("address"));
                user.setDob(resultSet.getDate("dob"));
                user.setCreatedAt(resultSet.getTimestamp("created_at"));
                user.setRole(resultSet.getString("role"));
                user.setPassword(resultSet.getString("password"));
                
                Log.d(TAG, "Login successful for user: " + user.getFullName());
                return user;
            }
            
            Log.e(TAG, "Login failed: Invalid email or password");
            return null;

        } catch (SQLException e) {
            Log.e(TAG, "SQL Error during login: " + e.getMessage());
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

    // Check if email already exists
    private boolean isEmailExists(String email) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            connection = connectionClass.CONN();
            if (connection == null) return false;

            String query = "SELECT COUNT(*) FROM User WHERE email = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count > 0;
            }
            
            return false;

        } catch (SQLException e) {
            Log.e(TAG, "SQL Error checking email: " + e.getMessage());
            return false;
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

    // Get user by email (for login)
    public User getUserByEmail(String email) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            connection = connectionClass.CONN();
            if (connection == null) return null;

            String query = "SELECT * FROM User WHERE email = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                User user = new User();
                user.setCustomerId(resultSet.getInt("customer_id"));
                user.setGender(resultSet.getString("gender"));
                user.setFirstName(resultSet.getString("first_name"));
                user.setLastName(resultSet.getString("last_name"));
                user.setEmail(resultSet.getString("email"));
                user.setPhone(resultSet.getString("phone"));
                user.setAddress(resultSet.getString("address"));
                user.setDob(resultSet.getDate("dob"));
                user.setCreatedAt(resultSet.getTimestamp("created_at"));
                user.setRole(resultSet.getString("role"));
                user.setPassword(resultSet.getString("password"));
                
                return user;
            }
            
            return null;

        } catch (SQLException e) {
            Log.e(TAG, "SQL Error getting user by email: " + e.getMessage());
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

    // Get all users (for admin purposes)
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = connectionClass.CONN();
            if (connection == null) return users;

            String query = "SELECT * FROM User ORDER BY created_at DESC";
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
            
            while (resultSet.next()) {
                User user = new User();
                user.setCustomerId(resultSet.getInt("customer_id"));
                user.setGender(resultSet.getString("gender"));
                user.setFirstName(resultSet.getString("first_name"));
                user.setLastName(resultSet.getString("last_name"));
                user.setEmail(resultSet.getString("email"));
                user.setPhone(resultSet.getString("phone"));
                user.setAddress(resultSet.getString("address"));
                user.setDob(resultSet.getDate("dob"));
                user.setCreatedAt(resultSet.getTimestamp("created_at"));
                user.setRole(resultSet.getString("role"));
                user.setPassword(resultSet.getString("password"));
                
                users.add(user);
            }
            
            return users;

        } catch (SQLException e) {
            Log.e(TAG, "SQL Error getting all users: " + e.getMessage());
            return users;
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

    // Update user information
    public boolean updateUser(User user) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        
        try {
            connection = connectionClass.CONN();
            if (connection == null) return false;

            String query = "UPDATE User SET gender=?, first_name=?, last_name=?, phone=?, address=?, dob=? " +
                          "WHERE customer_id=?";

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, user.getGender());
            preparedStatement.setString(2, user.getFirstName());
            preparedStatement.setString(3, user.getLastName());
            preparedStatement.setString(4, user.getPhone());
            preparedStatement.setString(5, user.getAddress());
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String dobString = user.getDob() != null ? sdf.format(user.getDob()) : null;
            preparedStatement.setString(6, dobString);
            
            preparedStatement.setInt(7, user.getCustomerId());

            int result = preparedStatement.executeUpdate();
            return result > 0;

        } catch (SQLException e) {
            Log.e(TAG, "SQL Error updating user: " + e.getMessage());
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

    public Map<String, Integer> getUserCountByRole() {
        Map<String, Integer> roleCount = new HashMap<>();
        String sql = "SELECT role, COUNT(*) as count FROM User GROUP BY role";
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = connectionClass.CONN();
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                roleCount.put(rs.getString("role"), rs.getInt("count"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (connection != null) connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return roleCount;
    }
} 