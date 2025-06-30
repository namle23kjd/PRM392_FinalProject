package com.example.prm392_finalproject.models;

public class ProductRequest {
    private String name;
    private String description;
    private int stockQuantity;
    private String imageUrl;
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

    public ProductRequest() {
        this.isActive = true;
        this.quantityInStock = 0;
    }

    public ProductRequest(String name, String description, int stockQuantity, String imageUrl,
                          String productCode, String specifications, String color, Double weight,
                          String dimensions, double price, Double cost, int quantityInStock,
                          Integer warrantyPeriod, String originCountry, String releaseDate,
                          String qrCode, boolean isActive, Integer categoryId) {
        this.name = name;
        this.description = description;
        this.stockQuantity = stockQuantity;
        this.imageUrl = imageUrl;
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
    }

    // Getters and Setters
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
}
