package com.example.prm392_finalproject.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CustomerOrder {
    private int orderId;
    private int customerId;
    private Date orderDate;
    private String status;
    private double totalAmount;
    private String note;

    // Shipping information
    private String shippingAddress;
    private String shippingMethod;
    private String trackingNumber;
    private Date expectedDelivery;
    private Date deliveredDate;
    private String shippingStatus;

    // Order items
    private List<OrderItem> orderItems;

    // Constructors
    public CustomerOrder() {}

    public CustomerOrder(int orderId, int customerId, Date orderDate, String status,
                         double totalAmount, String note) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.orderDate = orderDate;
        this.status = status;
        this.totalAmount = totalAmount;
        this.note = note;
    }

    public CustomerOrder(int customerId, String shippingAddress, String shippingMethod,
                         String shippingPersonName, Date expectedDelivery, String description) {
        this.customerId = customerId;
        this.shippingAddress = shippingAddress;
        this.shippingMethod = shippingMethod;
        this.expectedDelivery = expectedDelivery;
        this.note = description;
        this.status = "Pending";
        this.orderDate = new Date();
    }

    // Getters and Setters
    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    // Shipping getters and setters
    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getShippingMethod() {
        return shippingMethod;
    }

    public void setShippingMethod(String shippingMethod) {
        this.shippingMethod = shippingMethod;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public Date getExpectedDelivery() {
        return expectedDelivery;
    }

    public void setExpectedDelivery(Date expectedDelivery) {
        this.expectedDelivery = expectedDelivery;
    }

    public Date getDeliveredDate() {
        return deliveredDate;
    }

    public void setDeliveredDate(Date deliveredDate) {
        this.deliveredDate = deliveredDate;
    }

    public String getShippingStatus() {
        return shippingStatus;
    }

    public void setShippingStatus(String shippingStatus) {
        this.shippingStatus = shippingStatus;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    // Utility methods
    public String getStatusDisplay() {
        // Priority: shipping status > order status
        if (shippingStatus != null && !shippingStatus.trim().isEmpty()) {
            switch (shippingStatus.toLowerCase()) {
                case "pending": return "🔄 Chờ xử lý";
                case "processing": return "⚙️ Đang xử lý";
                case "shipped": return "🚛 Đang giao hàng";
                case "delivered": return "✅ Đã giao hàng";
                case "cancelled": return "❌ Đã hủy";
                default: return "📦 " + shippingStatus;
            }
        }

        if (status != null) {
            switch (status.toLowerCase()) {
                case "pending": return "🔄 Chờ xác nhận";
                case "processing": return "⚙️ Đang xử lý";
                case "completed": return "✅ Hoàn thành";
                case "cancelled": return "❌ Đã hủy";
                case "shipped": return "🚛 Đang vận chuyển";
                case "delivered": return "✅ Đã giao hàng";
                default: return "📦 " + status;
            }
        }

        return "❓ Không xác định";
    }

    public String getStatusColor() {
        String currentStatus = shippingStatus != null ? shippingStatus : status;
        if (currentStatus == null) return "#757575";

        switch (currentStatus.toLowerCase()) {
            case "pending": return "#FF9800";
            case "processing": return "#2196F3";
            case "shipped": return "#9C27B0";
            case "delivered":
            case "completed": return "#4CAF50";
            case "cancelled": return "#F44336";
            default: return "#757575";
        }
    }

    public boolean isDelivered() {
        return "delivered".equalsIgnoreCase(shippingStatus) ||
                "completed".equalsIgnoreCase(status);
    }

    public boolean isOverdue() {
        if (expectedDelivery == null || isDelivered()) {
            return false;
        }
        return expectedDelivery.before(new Date());
    }

    public boolean isCancelled() {
        return "cancelled".equalsIgnoreCase(shippingStatus) ||
                "cancelled".equalsIgnoreCase(status);
    }

    public boolean isPending() {
        String currentStatus = shippingStatus != null ? shippingStatus : status;
        return "pending".equalsIgnoreCase(currentStatus);
    }

    public boolean isProcessing() {
        String currentStatus = shippingStatus != null ? shippingStatus : status;
        return "processing".equalsIgnoreCase(currentStatus);
    }

    public boolean isShipped() {
        return "shipped".equalsIgnoreCase(shippingStatus);
    }

    public String getFormattedOrderDate() {
        if (orderDate == null) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(orderDate);
    }

    public String getFormattedOrderDateShort() {
        if (orderDate == null) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(orderDate);
    }

    public String getFormattedExpectedDelivery() {
        if (expectedDelivery == null) return "Chưa xác định";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(expectedDelivery);
    }

    public String getFormattedDeliveredDate() {
        if (deliveredDate == null) return "Chưa giao";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(deliveredDate);
    }

    public String getFormattedTotalAmount() {
        return String.format("%,.0f VNĐ", totalAmount);
    }

    public String getFormattedTotalAmountShort() {
        if (totalAmount >= 1000000) {
            return String.format("%.1fM VNĐ", totalAmount / 1000000);
        } else if (totalAmount >= 1000) {
            return String.format("%.0fK VNĐ", totalAmount / 1000);
        } else {
            return String.format("%.0f VNĐ", totalAmount);
        }
    }

    public boolean hasShippingInfo() {
        return shippingAddress != null && !shippingAddress.trim().isEmpty();
    }

    public boolean hasTrackingNumber() {
        return trackingNumber != null && !trackingNumber.trim().isEmpty();
    }

    public boolean hasNote() {
        return note != null && !note.trim().isEmpty();
    }

    // Get estimated delivery status
    public String getDeliveryStatus() {
        if (isDelivered()) {
            return "✅ Đã giao hàng";
        }

        if (isCancelled()) {
            return "❌ Đã hủy";
        }

        if (expectedDelivery == null) {
            return "⏳ Chưa xác định thời gian giao hàng";
        }

        if (isOverdue()) {
            return "⚠️ Trễ hạn giao hàng";
        }

        return "📅 Dự kiến giao: " + getFormattedExpectedDelivery();
    }

    public String getDeliveryStatusShort() {
        if (isDelivered()) return "✅ Đã giao";
        if (isCancelled()) return "❌ Đã hủy";
        if (isOverdue()) return "⚠️ Trễ hạn";
        if (isShipped()) return "🚛 Đang giao";
        if (isProcessing()) return "⚙️ Đang xử lý";
        return "🔄 Chờ xử lý";
    }

    // Calculate days until delivery
    public int getDaysUntilDelivery() {
        if (expectedDelivery == null || isDelivered()) {
            return -1;
        }

        long diffInMillies = expectedDelivery.getTime() - new Date().getTime();
        return (int) (diffInMillies / (1000 * 60 * 60 * 24));
    }

    public String getDaysUntilDeliveryText() {
        int days = getDaysUntilDelivery();
        if (days < 0) return "";
        if (days == 0) return "Hôm nay";
        if (days == 1) return "Ngày mai";
        if (days < 7) return days + " ngày nữa";
        int weeks = days / 7;
        if (weeks == 1) return "1 tuần nữa";
        return weeks + " tuần nữa";
    }

    // Get order age
    public int getOrderAgeInDays() {
        if (orderDate == null) return 0;
        long diffInMillies = new Date().getTime() - orderDate.getTime();
        return (int) (diffInMillies / (1000 * 60 * 60 * 24));
    }

    public String getOrderAgeText() {
        int days = getOrderAgeInDays();
        if (days == 0) return "Hôm nay";
        if (days == 1) return "Hôm qua";
        if (days < 7) return days + " ngày trước";
        int weeks = days / 7;
        if (weeks == 1) return "1 tuần trước";
        if (weeks < 4) return weeks + " tuần trước";
        int months = days / 30;
        if (months == 1) return "1 tháng trước";
        return months + " tháng trước";
    }

    // Get total order items count
    public int getTotalItemsCount() {
        if (orderItems == null || orderItems.isEmpty()) {
            return 0;
        }
        int total = 0;
        for (OrderItem item : orderItems) {
            total += item.getQuantity();
        }
        return total;
    }

    public String getTotalItemsText() {
        int count = getTotalItemsCount();
        if (count == 0) return "Không có sản phẩm";
        if (count == 1) return "1 sản phẩm";
        return count + " sản phẩm";
    }

    // Check if order can be cancelled
    public boolean canBeCancelled() {
        return isPending() || isProcessing();
    }

    // Check if order can be tracked
    public boolean canBeTracked() {
        return hasTrackingNumber() && (isShipped() || isDelivered());
    }

    // Get priority level based on status and delivery date
    public int getPriorityLevel() {
        if (isOverdue()) return 3; // High priority
        if (isShipped()) return 2; // Medium priority
        if (isPending() || isProcessing()) return 1; // Low priority
        return 0; // No priority (completed/cancelled)
    }

    public String getPriorityText() {
        switch (getPriorityLevel()) {
            case 3: return "🔴 Ưu tiên cao";
            case 2: return "🟡 Ưu tiên trung bình";
            case 1: return "🟢 Ưu tiên thấp";
            default: return "";
        }
    }

    // Inner class for Order Items
    public static class OrderItem {
        private int orderItemId;
        private int orderId;
        private int productId;
        private String productName;
        private int quantity;
        private double unitPrice;
        private double totalPrice;
        private String imageUrl;
        private String productDescription;
        private String productCode;

        // Constructors
        public OrderItem() {}

        public OrderItem(int productId, String productName, int quantity, double unitPrice) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.totalPrice = quantity * unitPrice;
        }

        public OrderItem(int orderId, int productId, String productName, int quantity,
                         double unitPrice, String imageUrl) {
            this.orderId = orderId;
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.totalPrice = quantity * unitPrice;
            this.imageUrl = imageUrl;
        }

        // Getters and Setters
        public int getOrderItemId() {
            return orderItemId;
        }

        public void setOrderItemId(int orderItemId) {
            this.orderItemId = orderItemId;
        }

        public int getOrderId() {
            return orderId;
        }

        public void setOrderId(int orderId) {
            this.orderId = orderId;
        }

        public int getProductId() {
            return productId;
        }

        public void setProductId(int productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
            this.totalPrice = quantity * unitPrice;
        }

        public double getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(double unitPrice) {
            this.unitPrice = unitPrice;
            this.totalPrice = quantity * unitPrice;
        }

        public double getTotalPrice() {
            return totalPrice;
        }

        public void setTotalPrice(double totalPrice) {
            this.totalPrice = totalPrice;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getProductDescription() {
            return productDescription;
        }

        public void setProductDescription(String productDescription) {
            this.productDescription = productDescription;
        }

        public String getProductCode() {
            return productCode;
        }

        public void setProductCode(String productCode) {
            this.productCode = productCode;
        }

        // Utility methods
        public String getFormattedUnitPrice() {
            return String.format("%,.0f VNĐ", unitPrice);
        }

        public String getFormattedTotalPrice() {
            return String.format("%,.0f VNĐ", totalPrice);
        }

        public String getQuantityDisplay() {
            return "x" + quantity;
        }

        public String getFormattedUnitPriceShort() {
            if (unitPrice >= 1000000) {
                return String.format("%.1fM", unitPrice / 1000000);
            } else if (unitPrice >= 1000) {
                return String.format("%.0fK", unitPrice / 1000);
            } else {
                return String.format("%.0f", unitPrice);
            }
        }

        public boolean hasImage() {
            return imageUrl != null && !imageUrl.trim().isEmpty();
        }

        public boolean hasDescription() {
            return productDescription != null && !productDescription.trim().isEmpty();
        }

        public String getProductDisplayName() {
            if (productName == null || productName.trim().isEmpty()) {
                return "Sản phẩm #" + productId;
            }
            return productName;
        }

        @Override
        public String toString() {
            return "OrderItem{" +
                    "productId=" + productId +
                    ", productName='" + productName + '\'' +
                    ", quantity=" + quantity +
                    ", unitPrice=" + unitPrice +
                    ", totalPrice=" + totalPrice +
                    '}';
        }
    }

    // Inner class for Order Statistics
    public static class OrderStats {
        private int totalOrders;
        private int pendingOrders;
        private int completedOrders;
        private int shippingOrders;
        private int cancelledOrders;
        private double totalSpent;
        private Date firstOrderDate;
        private Date lastOrderDate;

        // Constructors
        public OrderStats() {}

        public OrderStats(int totalOrders, int pendingOrders, int completedOrders,
                          int shippingOrders, double totalSpent) {
            this.totalOrders = totalOrders;
            this.pendingOrders = pendingOrders;
            this.completedOrders = completedOrders;
            this.shippingOrders = shippingOrders;
            this.totalSpent = totalSpent;
        }

        // Getters and Setters
        public int getTotalOrders() {
            return totalOrders;
        }

        public void setTotalOrders(int totalOrders) {
            this.totalOrders = totalOrders;
        }

        public int getPendingOrders() {
            return pendingOrders;
        }

        public void setPendingOrders(int pendingOrders) {
            this.pendingOrders = pendingOrders;
        }

        public int getCompletedOrders() {
            return completedOrders;
        }

        public void setCompletedOrders(int completedOrders) {
            this.completedOrders = completedOrders;
        }

        public int getShippingOrders() {
            return shippingOrders;
        }

        public void setShippingOrders(int shippingOrders) {
            this.shippingOrders = shippingOrders;
        }

        public int getCancelledOrders() {
            return cancelledOrders;
        }

        public void setCancelledOrders(int cancelledOrders) {
            this.cancelledOrders = cancelledOrders;
        }

        public double getTotalSpent() {
            return totalSpent;
        }

        public void setTotalSpent(double totalSpent) {
            this.totalSpent = totalSpent;
        }

        public Date getFirstOrderDate() {
            return firstOrderDate;
        }

        public void setFirstOrderDate(Date firstOrderDate) {
            this.firstOrderDate = firstOrderDate;
        }

        public Date getLastOrderDate() {
            return lastOrderDate;
        }

        public void setLastOrderDate(Date lastOrderDate) {
            this.lastOrderDate = lastOrderDate;
        }

        // Utility methods
        public String getFormattedTotalSpent() {
            return String.format("%,.0f VNĐ", totalSpent);
        }

        public double getAverageOrderValue() {
            return totalOrders > 0 ? totalSpent / totalOrders : 0;
        }

        public String getFormattedAverageOrderValue() {
            return String.format("%,.0f VNĐ", getAverageOrderValue());
        }

        public int getActiveOrders() {
            return pendingOrders + shippingOrders;
        }

        public double getCompletionRate() {
            return totalOrders > 0 ? (double) completedOrders / totalOrders * 100 : 0;
        }

        public String getFormattedCompletionRate() {
            return String.format("%.1f%%", getCompletionRate());
        }

        public double getCancellationRate() {
            return totalOrders > 0 ? (double) cancelledOrders / totalOrders * 100 : 0;
        }

        public String getFormattedCancellationRate() {
            return String.format("%.1f%%", getCancellationRate());
        }

        public boolean isFirstTimeCustomer() {
            return totalOrders <= 1;
        }

        public boolean isVipCustomer() {
            return totalOrders >= 10 || totalSpent >= 10000000; // 10M VND
        }

        public String getCustomerLevel() {
            if (isVipCustomer()) return "🥇 VIP";
            if (totalOrders >= 5) return "🥈 Thân thiết";
            if (totalOrders >= 2) return "🥉 Thường xuyên";
            return "🌟 Mới";
        }

        @Override
        public String toString() {
            return "OrderStats{" +
                    "totalOrders=" + totalOrders +
                    ", pendingOrders=" + pendingOrders +
                    ", completedOrders=" + completedOrders +
                    ", shippingOrders=" + shippingOrders +
                    ", totalSpent=" + totalSpent +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "CustomerOrder{" +
                "orderId=" + orderId +
                ", customerId=" + customerId +
                ", orderDate=" + orderDate +
                ", status='" + status + '\'' +
                ", totalAmount=" + totalAmount +
                ", shippingAddress='" + shippingAddress + '\'' +
                ", trackingNumber='" + trackingNumber + '\'' +
                '}';
    }

    // Equals and HashCode based on orderId
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CustomerOrder that = (CustomerOrder) obj;
        return orderId == that.orderId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(orderId);
    }
}