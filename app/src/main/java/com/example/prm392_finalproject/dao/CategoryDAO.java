package com.example.prm392_finalproject.dao;

import com.example.prm392_finalproject.db.ConnectionClass;
import com.example.prm392_finalproject.models.CategoryRequest;
import com.example.prm392_finalproject.models.CategoryResponse;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CategoryDAO {
    private ConnectionClass connectionClass;

    public CategoryDAO() {
        this.connectionClass = new ConnectionClass();
    }

    public List<CategoryResponse> getAllCategories() {
        List<CategoryResponse> categories = new ArrayList<>();
        try {
            String query = "SELECT * FROM category";
            PreparedStatement preparedStatement = connectionClass.CONN().prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                CategoryResponse category = new CategoryResponse();
                category.setCategory_id(resultSet.getInt("category_id"));
                category.setName(resultSet.getString("name"));
                category.setDescription(resultSet.getString("description"));
                category.setIs_deleted(resultSet.getBoolean("is_deleted"));
                categories.add(category);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories.stream().filter(category -> !category.isIs_deleted())
                .collect(Collectors.toList());
    }

    public CategoryResponse getCategoryById(int category_id) {
        CategoryResponse category = null;
        try {
            String query = "SELECT * FROM category WHERE category_id = ?";
            PreparedStatement preparedStatement = connectionClass.CONN().prepareStatement(query);
            preparedStatement.setInt(1, category_id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                category = new CategoryResponse();
                category.setCategory_id(resultSet.getInt("category_id"));
                category.setName(resultSet.getString("name"));
                category.setDescription(resultSet.getString("description"));
                category.setIs_deleted(resultSet.getBoolean("is_deleted"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (category == null || category.isIs_deleted()) {
            return null;
        }

        return category;
    }

    public void addCategory(CategoryRequest category) {
        try {
            String query = "INSERT INTO category (name, description, is_deleted) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = connectionClass.CONN().prepareStatement(query);
            preparedStatement.setString(1, category.getName());
            preparedStatement.setString(2, category.getDescription());
            preparedStatement.setBoolean(3, false); 
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateCategory(CategoryRequest category, int category_id) {
        try {
            String query = "UPDATE category SET name = ?, description = ? WHERE category_id = ?";
            PreparedStatement preparedStatement = connectionClass.CONN().prepareStatement(query);
            preparedStatement.setString(1, category.getName());
            preparedStatement.setString(2, category.getDescription());
            preparedStatement.setInt(3, category_id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteCategory(int category_id) {
        try {
            String query = "UPDATE category SET is_deleted = true WHERE category_id = ?";
            PreparedStatement preparedStatement = connectionClass.CONN().prepareStatement(query);
            preparedStatement.setInt(1, category_id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}