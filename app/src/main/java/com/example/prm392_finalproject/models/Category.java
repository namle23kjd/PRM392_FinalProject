package com.example.prm392_finalproject.models;

public class Category {
    private int category_id;
    private String name;
    private String description;
    private boolean is_deleted;

    public Category() {
    }

    public Category(int category_id, String name, String description) {
        this.category_id = category_id;
        this.name = name;
        this.description = description;
        this.is_deleted = false;
    }

    public int getCategory_id() {
        return category_id;
    }

    public void setCategory_id(int category_id) {
        this.category_id = category_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isIs_deleted() {
        return is_deleted;
    }

    public void setIs_deleted(boolean is_deleted) {
        this.is_deleted = is_deleted;
    }
}