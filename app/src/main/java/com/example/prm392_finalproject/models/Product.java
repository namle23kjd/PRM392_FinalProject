package com.example.prm392_finalproject.models;

import java.math.BigDecimal;

import java.io.Serializable;

public class Product implements Serializable {
    private static final long serialVersionUID = 1L;
    private int productId;
    private String name;
    private String description;
    private int stockQuantity;
    private String imageUrl;
    private String createdAt;
    private String updatedAt;
    private String productCode;
    private String specifications;
    private String color;
    private Double weight;
    private String dimensions;
    private double price;
    private Double cost;
    private int quantityInStock;
    private Integer warrantyPeriod;
    private String originCountry;
    private String releaseDate;
    private String qrCode;
    private boolean isActive;
    private Integer categoryId;
    private String category;

    // Default constructor
    public Product() {
        this.isActive = true;
        this.quantityInStock = 0;
    }

    // Full constructor
    public Product(int productId, String name, String description, int stockQuantity,
                   String imageUrl, String createdAt, String updatedAt, String productCode,
                   String specifications, String color, Double weight, String dimensions,
                   double price, Double cost, int quantityInStock, Integer warrantyPeriod,
                   String originCountry, String releaseDate, String qrCode, boolean isActive,
                   Integer categoryId, String category) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.stockQuantity = stockQuantity;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.productCode = productCode;
        this.specifications = specifications;
        this.color = color;
        this.weight = weight;
        this.dimensions = dimensions;
        this.price = price;
        this.cost = cost;
        this.quantityInStock = quantityInStock;
        this.warrantyPeriod = warrantyPeriod;
        this.originCountry = originCountry;
        this.releaseDate = releaseDate;
        this.qrCode = qrCode;
        this.isActive = isActive;
        this.categoryId = categoryId;
        this.category = category;
    }

    // Getters and Setters
    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
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

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getSpecifications() {
        return specifications;
    }

    public void setSpecifications(String specifications) {
        this.specifications = specifications;
    }

    public String getColor() {
        return color;

    }

    public void setColor(String color) {
        this.color = color;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getDimensions() {
        return dimensions;
    }

    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    // BigDecimal setter for price (for database compatibility)
    public void setPrice(BigDecimal price) {
        this.price = price != null ? price.doubleValue() : 0.0;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public int getQuantityInStock() {
        return quantityInStock;
    }

    public void setQuantityInStock(int quantityInStock) {
        this.quantityInStock = quantityInStock;
    }

    public Integer getWarrantyPeriod() {
        return warrantyPeriod;
    }

    public void setWarrantyPeriod(Integer warrantyPeriod) {
        this.warrantyPeriod = warrantyPeriod;
    }

    public String getOriginCountry() {
        return originCountry;
    }

    public void setOriginCountry(String originCountry) {
        this.originCountry = originCountry;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }


    // Utility method for debugging
    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", quantityInStock=" + quantityInStock +
                ", isActive=" + isActive +
                '}';
    }
}