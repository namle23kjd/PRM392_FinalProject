package com.example.prm392_finalproject.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject.R;

import java.util.List;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {

    private List<OrderItem> orderItemList;

    public OrderItemAdapter(List<OrderItem> orderItemList) {
        this.orderItemList = orderItemList;
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_item, parent, false);
        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        OrderItem orderItem = orderItemList.get(position);
        holder.bind(orderItem);
    }

    @Override
    public int getItemCount() {
        return orderItemList.size();
    }

    public static class OrderItemViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewProductName, textViewQuantity, textViewUnitPrice, textViewTotalPrice;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewQuantity = itemView.findViewById(R.id.textViewQuantity);
            textViewUnitPrice = itemView.findViewById(R.id.textViewUnitPrice);
            textViewTotalPrice = itemView.findViewById(R.id.textViewTotalPrice);
        }

        public void bind(OrderItem orderItem) {
            textViewProductName.setText(orderItem.getProductName());
            textViewQuantity.setText("Số lượng: " + orderItem.getQuantity());
            textViewUnitPrice.setText("Đơn giá: " + String.format("%,d VND", (long) orderItem.getUnitPrice()));
            textViewTotalPrice.setText("Thành tiền: " + String.format("%,d VND", (long) orderItem.getTotalPrice()));
        }
    }

    // OrderItem class for this adapter
    public static class OrderItem {
        private int id, orderId, productId, quantity;
        private String productName;
        private double unitPrice, totalPrice;

        public OrderItem(int id, int orderId, String productName, int quantity, double unitPrice, double totalPrice) {
            this.id = id;
            this.orderId = orderId;
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.totalPrice = totalPrice;
        }

        // Getters
        public int getId() { return id; }
        public int getOrderId() { return orderId; }
        public String getProductName() { return productName; }
        public int getQuantity() { return quantity; }
        public double getUnitPrice() { return unitPrice; }
        public double getTotalPrice() { return totalPrice; }
    }
} 