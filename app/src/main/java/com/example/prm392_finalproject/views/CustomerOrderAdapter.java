package com.example.prm392_finalproject.views;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.models.CustomerOrder;

import java.util.List;

public class CustomerOrderAdapter extends BaseAdapter {
    private Context context;
    private List<CustomerOrder> orderList;
    private LayoutInflater inflater;

    public CustomerOrderAdapter(Context context, List<CustomerOrder> orderList) {
        this.context = context;
        this.orderList = orderList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return orderList != null ? orderList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return orderList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return orderList.get(position).getOrderId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            // Sử dụng layout item_customer_order
            convertView = inflater.inflate(R.layout.item_customer_order, parent, false);
            holder = new ViewHolder();

            // Khởi tạo các view với đúng ID từ layout
            holder.tvOrderId = convertView.findViewById(R.id.tvOrderId);
            holder.tvOrderDate = convertView.findViewById(R.id.tvOrderDate);
            holder.tvOrderStatus = convertView.findViewById(R.id.tvOrderStatus);
            holder.tvOrderAmount = convertView.findViewById(R.id.tvOrderAmount);
            holder.tvShippingAddress = convertView.findViewById(R.id.tvShippingAddress);
            holder.tvTrackingNumber = convertView.findViewById(R.id.tvTrackingNumber);
            holder.tvExpectedDelivery = convertView.findViewById(R.id.tvExpectedDelivery);
            holder.tvDeliveryStatus = convertView.findViewById(R.id.tvDeliveryStatus);
            holder.tvDistanceInfo = convertView.findViewById(R.id.tvDistanceInfo);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        CustomerOrder order = orderList.get(position);

        // Set basic order information
        holder.tvOrderId.setText("Đơn hàng #" + order.getOrderId());
        holder.tvOrderDate.setText("📅 " + order.getFormattedOrderDate());
        holder.tvOrderStatus.setText(order.getStatusDisplay());
        holder.tvOrderAmount.setText("💰 " + order.getFormattedTotalAmount());

        // Set status color
        try {
            holder.tvOrderStatus.setTextColor(Color.parseColor(order.getStatusColor()));
        } catch (Exception e) {
            holder.tvOrderStatus.setTextColor(Color.parseColor("#757575"));
        }

        // Set shipping information
        if (order.hasShippingInfo()) {
            holder.tvShippingAddress.setVisibility(View.VISIBLE);
            holder.tvShippingAddress.setText("📍 " + order.getShippingAddress());

            // Show distance info hint
            holder.tvDistanceInfo.setVisibility(View.VISIBLE);
            holder.tvDistanceInfo.setText("📏 Nhấn giữ để kiểm tra khoảng cách");
        } else {
            holder.tvShippingAddress.setVisibility(View.GONE);
            holder.tvDistanceInfo.setVisibility(View.GONE);
        }

        // Set tracking information
        if (order.hasTrackingNumber()) {
            holder.tvTrackingNumber.setVisibility(View.VISIBLE);
            holder.tvTrackingNumber.setText("📦 " + order.getTrackingNumber());
        } else {
            holder.tvTrackingNumber.setVisibility(View.GONE);
        }

        // Set delivery information
        if (order.getExpectedDelivery() != null) {
            holder.tvExpectedDelivery.setVisibility(View.VISIBLE);
            holder.tvExpectedDelivery.setText("🚚 " + order.getFormattedExpectedDelivery());
        } else {
            holder.tvExpectedDelivery.setVisibility(View.GONE);
        }

        // Set delivery status
        holder.tvDeliveryStatus.setText(order.getDeliveryStatus());

        // Set delivery status color
        if (order.isOverdue()) {
            holder.tvDeliveryStatus.setTextColor(Color.parseColor("#F44336"));
        } else if (order.isDelivered()) {
            holder.tvDeliveryStatus.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            holder.tvDeliveryStatus.setTextColor(Color.parseColor("#FF9800"));
        }

        // Set background based on order status
        setOrderBackgroundColor(convertView, order);

        return convertView;
    }

    private void setOrderBackgroundColor(View view, CustomerOrder order) {
        if (order.isOverdue()) {
            view.setBackgroundColor(Color.parseColor("#FFEBEE")); // Light red
        } else if (order.isDelivered()) {
            view.setBackgroundColor(Color.parseColor("#E8F5E8")); // Light green
        } else if (order.isShipped()) {
            view.setBackgroundColor(Color.parseColor("#F3E5F5")); // Light purple
        } else {
            view.setBackgroundColor(Color.parseColor("#FFFFFF")); // White
        }
    }

    // Update adapter data
    public void updateOrders(List<CustomerOrder> newOrders) {
        if (this.orderList != null) {
            this.orderList.clear();
            this.orderList.addAll(newOrders);
        } else {
            this.orderList = newOrders;
        }
        notifyDataSetChanged();
    }

    // Add new order
    public void addOrder(CustomerOrder order) {
        if (this.orderList != null) {
            this.orderList.add(0, order); // Add to top
            notifyDataSetChanged();
        }
    }

    // Remove order
    public void removeOrder(int orderId) {
        if (this.orderList != null) {
            for (int i = 0; i < orderList.size(); i++) {
                if (orderList.get(i).getOrderId() == orderId) {
                    orderList.remove(i);
                    notifyDataSetChanged();
                    break;
                }
            }
        }
    }

    // Update specific order
    public void updateOrder(CustomerOrder updatedOrder) {
        if (this.orderList != null) {
            for (int i = 0; i < orderList.size(); i++) {
                if (orderList.get(i).getOrderId() == updatedOrder.getOrderId()) {
                    orderList.set(i, updatedOrder);
                    notifyDataSetChanged();
                    break;
                }
            }
        }
    }

    // Filter orders by status
    public void filterByStatus(String status) {
        // This would require keeping a reference to the original list
        // For now, we'll rely on the repository to filter
        notifyDataSetChanged();
    }

    // Get order at position
    public CustomerOrder getOrderAt(int position) {
        if (orderList != null && position >= 0 && position < orderList.size()) {
            return orderList.get(position);
        }
        return null;
    }

    // Check if list is empty
    public boolean isEmpty() {
        return orderList == null || orderList.isEmpty();
    }

    // Get total count
    public int getOrderCount() {
        return orderList != null ? orderList.size() : 0;
    }

    // ViewHolder pattern for better performance
    private static class ViewHolder {
        TextView tvOrderId;
        TextView tvOrderDate;
        TextView tvOrderStatus;
        TextView tvOrderAmount;
        TextView tvShippingAddress;
        TextView tvTrackingNumber;
        TextView tvExpectedDelivery;
        TextView tvDeliveryStatus;
        TextView tvDistanceInfo;
    }
}