package com.example.prm392_finalproject.views.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.prm392_finalproject.models.Product;
import java.util.List;

public class ProductAdapter extends BaseAdapter {
    private Context context;
    private List<Product> products;

    public ProductAdapter(Context context, List<Product> products) {
        this.context = context;
        this.products = products;
        System.out.println("ProductAdapter: Created with " +
                (products != null ? products.size() : "null") + " products");
    }

    @Override
    public int getCount() {
        int count = products != null ? products.size() : 0;
        System.out.println("ProductAdapter: getCount() = " + count);
        return count;
    }

    @Override
    public Object getItem(int position) {
        System.out.println("ProductAdapter: getItem(" + position + ")");
        return products != null && position < products.size() ? products.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        System.out.println("ProductAdapter: getView(" + position + ") called");

        try {
            TextView textView;

            if (convertView == null) {
                System.out.println("ProductAdapter: Creating new TextView");
                textView = new TextView(context);
                textView.setPadding(20, 20, 20, 20);
                textView.setTextSize(16);
            } else {
                System.out.println("ProductAdapter: Reusing existing view");
                textView = (TextView) convertView;
            }

            if (products != null && position < products.size()) {
                Product product = products.get(position);
                if (product != null) {
                    String text = product.getName() + "\nPrice: $" + product.getPrice();
                    textView.setText(text);
                    System.out.println("ProductAdapter: Set text for position " + position + ": " + product.getName());
                } else {
                    textView.setText("Product is null");
                    System.out.println("ProductAdapter: Product at position " + position + " is null");
                }
            } else {
                textView.setText("Invalid position");
                System.out.println("ProductAdapter: Invalid position " + position);
            }

            return textView;

        } catch (Exception e) {
            System.out.println("ProductAdapter: ERROR in getView: " + e.getMessage());
            e.printStackTrace();

            // Return a simple error view
            TextView errorView = new TextView(context);
            errorView.setText("Error displaying product");
            errorView.setPadding(20, 20, 20, 20);
            return errorView;
        }
    }
}