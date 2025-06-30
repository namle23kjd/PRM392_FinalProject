package com.example.prm392_finalproject.zalopay;

import android.content.Context;
import android.util.Log;

import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.models.zalopay.Order;
import com.example.prm392_finalproject.models.zalopay.OrderItem;

import okhttp3.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ZaloPayApi {
    // Sandbox environment URLs
    private static final String BASE_URL = "https://sb-openapi.zalopay.vn/v2/create";
    
    private static final OkHttpClient client = new OkHttpClient();

    // Tạo đơn hàng
    public static String createOrder(Context context, Order order) throws IOException {
        // Lấy thông tin cấu hình từ strings.xml
        String appId = context.getString(R.string.zalopay_app_id);
        String appUser = context.getString(R.string.zalopay_app_user);
        String paymentCode = context.getString(R.string.zalopay_payment_code);
        String secretKey = context.getString(R.string.zalopay_secret_key);
        
        long appTime = System.currentTimeMillis();  // Thời gian tạo đơn hàng
        // Format app_trans_id: yymmdd-Mã đơn hàng-timestamp để tránh trùng lặp
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd", Locale.getDefault());
        String datePart = sdf.format(new Date(appTime));
        String appTransId = datePart + "-" + order.getOrderId() + "-" + appTime;
        String embedData = "{}";  // Dữ liệu bổ sung
        String itemJson = createItemsJson(order.getItems());

        Log.d("ZaloPayApi", "Creating order with:");
        Log.d("ZaloPayApi", "App ID: " + appId);
        Log.d("ZaloPayApi", "App User: " + appUser);
        Log.d("ZaloPayApi", "Payment Code: " + paymentCode);
        Log.d("ZaloPayApi", "App Trans ID: " + appTransId);
        Log.d("ZaloPayApi", "Amount: " + (long)order.getTotalAmount());
        Log.d("ZaloPayApi", "App Time: " + appTime);

        // Tạo chuỗi mac đúng chuẩn ZaloPay
        String mac = generateMac(appId, appTransId, appUser, order.getTotalAmount(), appTime, embedData, itemJson, secretKey);

        // Tạo body yêu cầu
        RequestBody body = new FormBody.Builder()
            .add("app_id", appId)
            .add("app_user", appUser)
            .add("app_trans_id", appTransId)
            .add("app_time", String.valueOf(appTime))
            .add("amount", String.valueOf((long)order.getTotalAmount()))  // Số tiền thanh toán (phải là long)
            .add("item", itemJson)
            .add("embed_data", embedData)
            .add("mac", mac)
            .add("payment_code", paymentCode)
            .add("description", order.getNote())  // Mô tả đơn hàng
            .add("callback_url", "https://yourapp.com/callback")
            .add("redirect_url", "https://yourapp.com/redirect")
            .add("device_info", "{\"device\": \"android\"}")
            .add("currency", "VND")
            .add("title", order.getNote())  // Tiêu đề đơn hàng
            .add("userIP", "192.168.1.1")  // Địa chỉ IP của người dùng
            .build();

        // Tạo yêu cầu
        Request request = new Request.Builder()
            .url(BASE_URL)
            .post(body)
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            return response.body().string();  // Trả về phản hồi chứa order_token
        }
    }

    // Tạo chuỗi HMAC
    private static String generateMac(String appId, String appTransId, String appUser, double amount, long appTime, String embedData, String item, String secretKey) {
        try {
            // Đúng chuẩn ZaloPay: app_id|app_trans_id|app_user|amount|app_time|embed_data|item
            String data = appId + "|" + appTransId + "|" + appUser + "|" + (long)amount + "|" + appTime + "|" + embedData + "|" + item;
            Log.d("ZaloPayApi", "Data to sign: " + data);
            Log.d("ZaloPayApi", "Secret key: " + secretKey);
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            byte[] macBytes = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : macBytes) {
                sb.append(String.format("%02x", b));
            }
            String mac = sb.toString();
            Log.d("ZaloPayApi", "Generated MAC: " + mac);
            return mac;
        } catch (Exception e) {
            Log.e("ZaloPayApi", "Error generating MAC: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Tạo JSON mô tả items
    private static String createItemsJson(List<OrderItem> items) {
        StringBuilder json = new StringBuilder("[");
        for (OrderItem item : items) {
            json.append("{")
                .append("\"item_id\": \"").append(item.getItemId()).append("\",")
                .append("\"item_name\": \"").append(item.getItemName()).append("\",")
                .append("\"quantity\": ").append(item.getQuantity()).append(",")
                .append("\"price\": ").append(item.getUnitPrice())
                .append("},");
        }
        if (json.length() > 1) {
            json.deleteCharAt(json.length() - 1);  // Remove last comma
        }
        json.append("]");
        return json.toString();
    }
}
