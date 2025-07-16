package com.example.prm392_finalproject.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.models.zalopay.Order;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class StaffOrderAdapter extends RecyclerView.Adapter<StaffOrderAdapter.StaffOrderViewHolder> {
    private List<Order> orderList;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public StaffOrderAdapter(List<Order> orderList) {
        this.orderList = orderList;
    }

    public StaffOrderAdapter(List<Order> orderList, OnOrderClickListener listener) {
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StaffOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new StaffOrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StaffOrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        
        holder.tvOrderId.setText("Order #" + order.getOrderId());
        holder.tvOrderDate.setText("Date: " + order.getOrderDate());
        holder.tvTotalAmount.setText("Total: $" + String.format("%.2f", order.getTotalAmount()));
        holder.tvStatus.setText("Status: " + order.getStatus());
        
        // Staff chỉ có thể xem, không thể thanh toán
        holder.btnPay.setVisibility(View.GONE);
        
        // Add click listener to the entire item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class StaffOrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderDate, tvTotalAmount, tvStatus;
        View btnPay;

        public StaffOrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnPay = itemView.findViewById(R.id.btnPay);
        }
    }
} 