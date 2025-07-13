package com.example.prm392_finalproject.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.models.Discount;
import com.google.android.material.button.MaterialButton;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class DiscountAdapter extends RecyclerView.Adapter<DiscountAdapter.DiscountViewHolder> {

    private List<Discount> discounts;
    private OnDiscountClickListener listener;

    public interface OnDiscountClickListener {
        void onEditClick(Discount discount);
        void onDeleteClick(Discount discount);
    }

    public DiscountAdapter(List<Discount> discounts, OnDiscountClickListener listener) {
        this.discounts = discounts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DiscountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_discount, parent, false);
        return new DiscountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiscountViewHolder holder, int position) {
        Discount discount = discounts.get(position);
        holder.bind(discount);
    }

    @Override
    public int getItemCount() {
        return discounts.size();
    }

    public void updateDiscounts(List<Discount> newDiscounts) {
        this.discounts = newDiscounts;
        notifyDataSetChanged();
    }

    class DiscountViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDiscountCode, tvDiscountType, tvDiscountValue, tvStartDate, tvEndDate;
        private ImageButton btnEdit, btnDelete;

        public DiscountViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDiscountCode = itemView.findViewById(R.id.tvDiscountCode);
            tvDiscountType = itemView.findViewById(R.id.tvDiscountType);
            tvDiscountValue = itemView.findViewById(R.id.tvDiscountValue);
            tvStartDate = itemView.findViewById(R.id.tvStartDate);
            tvEndDate = itemView.findViewById(R.id.tvEndDate);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(Discount discount) {
            tvDiscountCode.setText("Code: " + discount.getCode());
            tvDiscountType.setText("Type: " + discount.getDiscount_type());
            tvDiscountValue.setText("Value: " + discount.getDiscount_value());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            if (discount.getStart_date() != null) {
                tvStartDate.setText(discount.getStart_date().format(formatter));
            }
            if (discount.getEnd_date() != null) {
                tvEndDate.setText(discount.getEnd_date().format(formatter));
            }

            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(discount);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(discount);
                }
            });
        }
    }
} 