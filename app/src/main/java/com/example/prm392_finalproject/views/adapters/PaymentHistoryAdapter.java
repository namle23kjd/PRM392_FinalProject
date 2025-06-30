package com.example.prm392_finalproject.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.models.Payment;
import java.util.List;

public class PaymentHistoryAdapter extends RecyclerView.Adapter<PaymentHistoryAdapter.PaymentViewHolder> {
    private List<Payment> paymentList;
    public PaymentHistoryAdapter(List<Payment> paymentList) {
        this.paymentList = paymentList;
    }
    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payment, parent, false);
        return new PaymentViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        Payment payment = paymentList.get(position);
        holder.tvOrderId.setText("Mã đơn: #" + payment.getOrderId());
        holder.tvPaymentDate.setText("Ngày thanh toán: " + payment.getPaymentDate());
        holder.tvAmount.setText("Số tiền: " + String.format("%,.0f", payment.getAmount()) + " VND");
        holder.tvPaymentMethod.setText("Phương thức: " + payment.getPaymentMethod());
        holder.tvPaymentStatus.setText("Trạng thái: " + payment.getPaymentStatus());
    }
    @Override
    public int getItemCount() {
        return paymentList.size();
    }
    public void updateData(List<Payment> newList) {
        this.paymentList = newList;
        notifyDataSetChanged();
    }
    public static class PaymentViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvPaymentDate, tvAmount, tvPaymentMethod, tvPaymentStatus;
        public PaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvPaymentDate = itemView.findViewById(R.id.tvPaymentDate);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvPaymentMethod = itemView.findViewById(R.id.tvPaymentMethod);
            tvPaymentStatus = itemView.findViewById(R.id.tvPaymentStatus);
        }
    }
} 