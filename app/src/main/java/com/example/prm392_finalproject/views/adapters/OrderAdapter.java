package com.example.prm392_finalproject.views.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.models.zalopay.Order;
import com.example.prm392_finalproject.views.OrderDetailActivity;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    public interface OnPayClickListener {
        void onPayClick(Order order);
    }

    private List<Order> orderList;
    private OnPayClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("vi", "VN"));

    public OrderAdapter(List<Order> orderList, OnPayClickListener listener) {
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.tvOrderId.setText("Mã đơn: #" + order.getOrderId());
        holder.tvOrderDate.setText("Ngày: " + order.getOrderDate());
        holder.tvTotalAmount.setText("Tổng tiền: " + String.format("%,.0f", order.getTotalAmount()) + " VND");
        
        // Hiển thị trạng thái với màu sắc
        String statusText = order.getStatusDisplay();
        holder.tvStatus.setText(statusText);
        
        // Đổi màu text theo trạng thái
        if (order.isPaid()) {
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
        } else {
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_orange_dark));
        }
        
        // Chỉ hiển thị nút thanh toán nếu trạng thái là Pending
        if ("pending".equalsIgnoreCase(order.getStatus())) {
            holder.btnPay.setVisibility(View.VISIBLE);
            holder.btnPay.setOnClickListener(v -> listener.onPayClick(order));
        } else {
            holder.btnPay.setVisibility(View.GONE);
        }

        // Add click listener for order detail
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), OrderDetailActivity.class);
            intent.putExtra("order_id", Integer.parseInt(order.getOrderId()));
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderDate, tvTotalAmount, tvStatus;
        Button btnPay;
        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnPay = itemView.findViewById(R.id.btnPay);
        }
    }
} 