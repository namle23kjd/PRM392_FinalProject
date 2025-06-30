package com.example.prm392_finalproject.views;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.models.Shipping;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ShippingAdapter extends ArrayAdapter<Shipping> {
    private Context context;
    private List<Shipping> shippingList;
    private LayoutInflater inflater;

    public ShippingAdapter(Context context, List<Shipping> shippingList) {
        super(context, R.layout.item_shipping, shippingList);
        this.context = context;
        this.shippingList = shippingList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_shipping, parent, false);
            holder = new ViewHolder();
            holder.tvShippingId = convertView.findViewById(R.id.tvShippingId);
            holder.tvOrderId = convertView.findViewById(R.id.tvOrderId);
            holder.tvPersonName = convertView.findViewById(R.id.tvPersonName);
            holder.tvAddress = convertView.findViewById(R.id.tvAddress);
            holder.tvTrackingNumber = convertView.findViewById(R.id.tvTrackingNumber);
            holder.tvStatus = convertView.findViewById(R.id.tvStatus);
            holder.tvExpectedDelivery = convertView.findViewById(R.id.tvExpectedDelivery);
            holder.tvShippingMethod = convertView.findViewById(R.id.tvShippingMethod);
            holder.tvOverdueWarning = convertView.findViewById(R.id.tvOverdueWarning);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Shipping shipping = shippingList.get(position);

        // Set basic information
        holder.tvShippingId.setText("ID: " + shipping.getShippingId());
        holder.tvOrderId.setText("Đơn hàng: #" + shipping.getOrderId());
        holder.tvPersonName.setText("Người nhận: " + shipping.getShippingPersonName());
        holder.tvAddress.setText("Địa chỉ: " + shipping.getShippingAddress());
        holder.tvTrackingNumber.setText("Tracking: " + shipping.getTrackingNumber());
        holder.tvShippingMethod.setText("Phương thức: " + shipping.getShippingMethod());

        // Set status with color coding
        holder.tvStatus.setText(shipping.getStatusDisplay());
        setStatusColor(holder.tvStatus, shipping.getStatus());

        // Set expected delivery date
        if (shipping.getExpectedDelivery() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            holder.tvExpectedDelivery.setText("Dự kiến: " + sdf.format(shipping.getExpectedDelivery()));
        } else {
            holder.tvExpectedDelivery.setText("Dự kiến: Chưa xác định");
        }

        // Show overdue warning
        if (shipping.isOverdue()) {
            holder.tvOverdueWarning.setVisibility(View.VISIBLE);
            holder.tvOverdueWarning.setText("⚠️ TRỄ HẠN");
            convertView.setBackgroundColor(Color.parseColor("#FFEBEE")); // Light red background
        } else {
            holder.tvOverdueWarning.setVisibility(View.GONE);
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }

        return convertView;
    }

    private void setStatusColor(TextView textView, String status) {
        switch (status.toLowerCase()) {
            case "pending":
                textView.setTextColor(Color.parseColor("#FF9800")); // Orange
                break;
            case "preparing":
                textView.setTextColor(Color.parseColor("#2196F3")); // Blue
                break;
            case "shipping":
                textView.setTextColor(Color.parseColor("#9C27B0")); // Purple
                break;
            case "delivered":
                textView.setTextColor(Color.parseColor("#4CAF50")); // Green
                break;
            case "cancelled":
                textView.setTextColor(Color.parseColor("#F44336")); // Red
                break;
            default:
                textView.setTextColor(Color.parseColor("#757575")); // Gray
                break;
        }
    }

    static class ViewHolder {
        TextView tvShippingId;
        TextView tvOrderId;
        TextView tvPersonName;
        TextView tvAddress;
        TextView tvTrackingNumber;
        TextView tvStatus;
        TextView tvExpectedDelivery;
        TextView tvShippingMethod;
        TextView tvOverdueWarning;
    }
}