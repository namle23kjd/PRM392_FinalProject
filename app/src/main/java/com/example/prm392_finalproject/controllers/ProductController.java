package com.example.prm392_finalproject.controllers;

import android.util.Log;

import com.example.prm392_finalproject.dao.ProductDAO;
import com.example.prm392_finalproject.models.Product;

import java.util.List;

public class ProductController {
    private static final String TAG = "ProductController";
    private final ProductDAO productDAO;

    public ProductController() {
        this.productDAO = new ProductDAO();
    }

    // Callback interfaces for the controller
    public interface ProductListCallback {
        void onSuccess(List<Product> products);
        void onError(String error);
    }

    public interface ProductCallback {
        void onSuccess(Product product);
        void onError(String error);
    }

    public interface OperationCallback {
        void onSuccess();
        void onError(String error);
    }

    // Get all products asynchronously
    public void getAllProducts(ProductListCallback callback) {
        Log.d(TAG, "Getting all products...");

        productDAO.getAllProductsAsync(new ProductDAO.ProductListCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                Log.i(TAG, "Successfully retrieved " + products.size() + " products");
                if (callback != null) {
                    callback.onSuccess(products);
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to get all products: " + error);
                if (callback != null) {
                    callback.onError("Failed to load products: " + error);
                }
            }
        });
    }

    // Get product by ID asynchronously
    public void getProductById(int productId, ProductCallback callback) {
        Log.d(TAG, "Getting product by ID: " + productId);

        if (productId <= 0) {
            String error = "Invalid product ID: " + productId;
            Log.e(TAG, error);
            if (callback != null) {
                callback.onError(error);
            }
            return;
        }

        productDAO.getProductByIdAsync(productId, new ProductDAO.ProductCallback() {
            @Override
            public void onSuccess(Product product) {
                Log.i(TAG, "Successfully retrieved product: " + product.getName());
                if (callback != null) {
                    callback.onSuccess(product);
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to get product by ID " + productId + ": " + error);
                if (callback != null) {
                    callback.onError("Failed to find product: " + error);
                }
            }
        });
    }

    // Add product asynchronously
    public void addProduct(Product product, OperationCallback callback) {
        Log.d(TAG, "Adding new product: " + (product != null ? product.getName() : "null"));

        // Validate product data
        String validationError = validateProduct(product);
        if (validationError != null) {
            Log.e(TAG, "Product validation failed: " + validationError);
            if (callback != null) {
                callback.onError(validationError);
            }
            return;
        }

        productDAO.addProductAsync(product, new ProductDAO.OperationCallback() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "Successfully added product: " + product.getName());
                if (callback != null) {
                    callback.onSuccess();
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to add product " + product.getName() + ": " + error);
                if (callback != null) {
                    callback.onError("Failed to add product: " + error);
                }
            }
        });
    }

    // Update product asynchronously
    public void updateProduct(Product product, OperationCallback callback) {
        Log.d(TAG, "Updating product: " + (product != null ? product.getName() : "null"));

        // Validate product data
        String validationError = validateProduct(product);
        if (validationError != null) {
            Log.e(TAG, "Product validation failed: " + validationError);
            if (callback != null) {
                callback.onError(validationError);
            }
            return;
        }

        if (product.getProductId() <= 0) {
            String error = "Invalid product ID for update: " + product.getProductId();
            Log.e(TAG, error);
            if (callback != null) {
                callback.onError(error);
            }
            return;
        }

        productDAO.updateProductAsync(product, new ProductDAO.OperationCallback() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "Successfully updated product: " + product.getName());
                if (callback != null) {
                    callback.onSuccess();
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to update product " + product.getName() + ": " + error);
                if (callback != null) {
                    callback.onError("Failed to update product: " + error);
                }
            }
        });
    }

    // Delete product asynchronously
    public void deleteProduct(int productId, OperationCallback callback) {
        Log.d(TAG, "Deleting product with ID: " + productId);

        if (productId <= 0) {
            String error = "Invalid product ID for deletion: " + productId;
            Log.e(TAG, error);
            if (callback != null) {
                callback.onError(error);
            }
            return;
        }

        productDAO.deleteProductAsync(productId, new ProductDAO.OperationCallback() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "Successfully deleted product with ID: " + productId);
                if (callback != null) {
                    callback.onSuccess();
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to delete product with ID " + productId + ": " + error);
                if (callback != null) {
                    callback.onError("Failed to delete product: " + error);
                }
            }
        });
    }

    // Search products asynchronously
    public void searchProducts(String searchTerm, ProductListCallback callback) {
        Log.d(TAG, "Searching products with term: " + searchTerm);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            String error = "Search term cannot be empty";
            Log.e(TAG, error);
            if (callback != null) {
                callback.onError(error);
            }
            return;
        }

        productDAO.searchProductsAsync(searchTerm.trim(), new ProductDAO.ProductListCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                Log.i(TAG, "Search found " + products.size() + " products for term: " + searchTerm);
                if (callback != null) {
                    callback.onSuccess(products);
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Search failed for term '" + searchTerm + "': " + error);
                if (callback != null) {
                    callback.onError("Search failed: " + error);
                }
            }
        });
    }

    // Get products by category (if you have categories)
    public void getProductsByCategory(String category, ProductListCallback callback) {
        Log.d(TAG, "Getting products by category: " + category);

        if (category == null || category.trim().isEmpty()) {
            // If no category specified, get all products
            getAllProducts(callback);
            return;
        }

        // You can implement category-specific search using the existing search functionality
        // or add a new method to ProductDAO if needed
        searchProducts(category, callback);
    }

    // Get products with low stock
    public void getLowStockProducts(int threshold, ProductListCallback callback) {
        Log.d(TAG, "Getting low stock products with threshold: " + threshold);

        // Get all products first, then filter by stock level
        getAllProducts(new ProductListCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                List<Product> lowStockProducts = products.stream()
                        .filter(product -> product.getQuantityInStock() <= threshold)
                        .collect(java.util.stream.Collectors.toList());

                Log.i(TAG, "Found " + lowStockProducts.size() + " low stock products");
                if (callback != null) {
                    callback.onSuccess(lowStockProducts);
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to get low stock products: " + error);
                if (callback != null) {
                    callback.onError(error);
                }
            }
        });
    }

    // Validate product data
    private String validateProduct(Product product) {
        if (product == null) {
            return "Product cannot be null";
        }

        if (product.getName() == null || product.getName().trim().isEmpty()) {
            return "Product name is required";
        }

        if (product.getProductCode() == null || product.getProductCode().trim().isEmpty()) {
            return "Product code is required";
        }

        if (product.getPrice() < 0) {
            return "Product price cannot be negative";
        }

        if (product.getQuantityInStock() < 0) {
            return "Product quantity cannot be negative";
        }

        // Additional validation rules can be added here
        if (product.getName().length() > 100) {
            return "Product name is too long (max 100 characters)";
        }

        if (product.getProductCode().length() > 50) {
            return "Product code is too long (max 50 characters)";
        }

        return null; // No validation errors
    }

    // Clean up resources
    public void shutdown() {
        Log.d(TAG, "Shutting down ProductController");
        if (productDAO != null) {
            productDAO.shutdown();
        }
    }

    // Utility method to check if a product exists
    public void productExists(int productId, ProductCallback callback) {
        getProductById(productId, new ProductCallback() {
            @Override
            public void onSuccess(Product product) {
                if (callback != null) {
                    callback.onSuccess(product);
                }
            }

            @Override
            public void onError(String error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }
        });
    }

    // Utility method to get product count
    public void getProductCount(ProductCountCallback callback) {
        getAllProducts(new ProductListCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                if (callback != null) {
                    callback.onSuccess(products.size());
                }
            }

            @Override
            public void onError(String error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }
        });
    }

    // Callback interface for product count
    public interface ProductCountCallback {
        void onSuccess(int count);
        void onError(String error);
    }
}