package com.example.prm392_finalproject.controllers;

import com.example.prm392_finalproject.dao.CategoryDAO;
import com.example.prm392_finalproject.models.CategoryRequest;
import com.example.prm392_finalproject.models.CategoryResponse;

import java.util.List;

public class CategoryController {
    private CategoryDAO categoryDAO;

    public CategoryController() {
        this.categoryDAO = new CategoryDAO();
    }

    public List<CategoryResponse> getAllCategories() {
        return categoryDAO.getAllCategories();
    }

    public CategoryResponse getCategoryById(int category_id) {
        return categoryDAO.getCategoryById(category_id);
    }

    public void addCategory(CategoryRequest category) {
        categoryDAO.addCategory(category);
    }

    public void updateCategory(CategoryRequest category, int category_id) {
        categoryDAO.updateCategory(category, category_id);
    }

    public void deleteCategory(int category_id) {
        categoryDAO.deleteCategory(category_id);
    }
}
