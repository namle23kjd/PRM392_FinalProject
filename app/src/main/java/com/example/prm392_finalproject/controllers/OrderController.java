package com.example.prm392_finalproject.controllers;

import com.example.prm392_finalproject.dao.OrderDAO;
import com.example.prm392_finalproject.models.Order;
import com.example.prm392_finalproject.models.CustomerOrder;
import com.example.prm392_finalproject.models.OrderRequest;
import com.example.prm392_finalproject.utils.CartManager;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class OrderController {

    private final OrderDAO orderDAO;
    private final CustomerOrderRepository customerOrderRepository;

    public OrderController() {
        this.orderDAO = new OrderDAO();
        this.customerOrderRepository = new CustomerOrderRepository();
    }

    public List<com.example.prm392_finalproject.models.zalopay.Order> getOrdersByUserId(int userId) {
        return safe(() -> orderDAO.getOrdersByUserId(userId), Collections.emptyList());
    }

    public List<com.example.prm392_finalproject.models.zalopay.Order> getAllOrders() {
        return safe(() -> orderDAO.getAllOrders(), Collections.emptyList());
    }

    public void updateOrderStatus(String orderId, String newStatus) {
        safe(() -> { orderDAO.updateOrderStatus(orderId, newStatus); return null; }, null);
    }

    public Map<Integer, Integer> getOrderCountByMonth(int year) {
        return safe(() -> orderDAO.getOrderCountByMonth(year), Collections.emptyMap());
    }

    // Cập nhật method này để thực sự tạo order
    public boolean createOrder(Order order, String shippingAddress, String shippingMethod, String shippingPersonName) {
        return safe(() -> {
            // Chuyển đổi Order thành CustomerOrder.OrderItem list
            List<CustomerOrder.OrderItem> items = new ArrayList<>();
            // TODO: Cần thêm logic để lấy order items từ cart hoặc parameter

            return customerOrderRepository.createOrderForCustomer(
                    order.getCustomerId(),
                    items,
                    order.getNote(),
                    shippingAddress,
                    shippingMethod,
                    shippingPersonName,
                    order.getTotalAmount() // Truyền tổng tiền từ Order
            );
        }, false);
    }

    // Thêm method mới để tạo order từ cart
    public boolean createOrderFromCart(OrderRequest orderRequest) {
        return safe(() -> {
            // Chuyển đổi OrderRequest thành CustomerOrder.OrderItem list
            List<CustomerOrder.OrderItem> items = new ArrayList<>();
            
            for (OrderRequest.OrderItemRequest itemRequest : orderRequest.getItems()) {
                CustomerOrder.OrderItem item = new CustomerOrder.OrderItem();
                item.setProductId(itemRequest.getProductId());
                item.setQuantity(itemRequest.getQuantity());
                item.setUnitPrice(itemRequest.getUnitPrice());
                item.setTotalPrice(itemRequest.getQuantity() * itemRequest.getUnitPrice());
                items.add(item);
            }
            
            return customerOrderRepository.createOrderForCustomer(
                orderRequest.getCustomerId(),
                items,
                orderRequest.getNote(),
                orderRequest.getShippingAddress(),
                orderRequest.getShippingMethod(),
                orderRequest.getShippingPersonName(),
                orderRequest.getTotalAmount() // Truyền tổng tiền đã được tính sau discount
            );
        }, false);
    }

    public Order getOrderById(int orderId) {
        return safe(() -> {
            // TODO: Implement get order by ID logic
            // For now, return null as placeholder
            return null;
        }, null);
    }

    public boolean deleteOrder(int orderId) {
        return safe(() -> {
            // TODO: Implement order deletion logic
            // For now, return true as placeholder
            return true;
        }, false);
    }

    // Helper để tránh duplicate try-catch
    private <T> T safe(SafeSupplier<T> action, T fallback) {
        try {
            return action.get();
        } catch (Exception e) {
            System.out.println("OrderController: Operation failed: " + e.getMessage());
            e.printStackTrace();
            return fallback;
        }
    }

    @FunctionalInterface
    private interface SafeSupplier<T> {
        T get() throws Exception;
    }
} 