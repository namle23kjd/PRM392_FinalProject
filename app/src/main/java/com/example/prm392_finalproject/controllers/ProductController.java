    package com.example.prm392_finalproject.controllers;

    import com.example.prm392_finalproject.dao.ProductDAO;
    import com.example.prm392_finalproject.models.Product;

    import java.util.Collections;
    import java.util.List;

    public class ProductController {

        private final ProductDAO productDAO;

        public ProductController() {
            this.productDAO = new ProductDAO();
        }

        public List<Product> getAllProducts() {
            return safe(() -> productDAO.getAllProducts(), Collections.emptyList());
        }

        public Product getProductById(int productId) {
            return safe(() -> productDAO.getProductById(productId), null);
        }

        public void addProduct(Product product) {
            safe(() -> { productDAO.addProduct(product); return null; }, null);
        }

        public void updateProduct(Product product) {
            safe(() -> { productDAO.updateProduct(product); return null; }, null);
        }

        public void deleteProduct(int productId) {
            safe(() -> { productDAO.deleteProduct(productId); return null; }, null);
        }

        public List<Product> searchProducts(String searchTerm) {
            return safe(() -> productDAO.searchProducts(searchTerm), Collections.emptyList());
        }

        // Helper để tránh duplicate try-catch
        private <T> T safe(SafeSupplier<T> action, T fallback) {
            try {
                return action.get();
            } catch (Exception e) {
                System.out.println("ProductController: Operation failed: " + e.getMessage());
                e.printStackTrace();
                return fallback;
            }
        }

        @FunctionalInterface
        private interface SafeSupplier<T> {
            T get() throws Exception;
        }
    }
