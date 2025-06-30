package com.example.prm392_finalproject.views;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.models.zalopay.Order;
import com.example.prm392_finalproject.zalopay.ZaloPayApi;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONException;
import org.json.JSONObject;

import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;
import com.example.prm392_finalproject.dao.OrderDAO;
import com.example.prm392_finalproject.dao.PaymentDAO;

public class PaymentActivity extends AppCompatActivity {

    private static final String TAG = "PaymentActivity";
    private static String ORDER_TOKEN = ""; // order_token nhận từ ZaloPay API
    private static final String PREFS_NAME = "ZaloPayPrefs";
    private static final String KEY_PAYMENT_RESULT = "payment_result";
    private static final String KEY_PAYMENT_MESSAGE = "payment_message";

    private TextView paymentStatus;
    private TextView paymentError;
    private String appScheme;
    private ExecutorService executorService;
    private Order currentOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Khởi tạo ExecutorService cho background tasks
        executorService = Executors.newSingleThreadExecutor();

        // Lấy app scheme từ strings.xml
        appScheme = getString(R.string.zalopay_app_scheme) + "://app";

        // Khởi tạo ZaloPaySDK
        ZaloPaySDK.init(2554, Environment.SANDBOX);

        // Khởi tạo các view
        paymentStatus = findViewById(R.id.paymentStatus);
        paymentError = findViewById(R.id.paymentError);
        Button btnPay = findViewById(R.id.btnPay);
        TextView tvOrderInfo = findViewById(R.id.tvOrderInfo);
        ProgressBar paymentProgress = findViewById(R.id.paymentProgress);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("order")) {
            currentOrder = (Order) intent.getSerializableExtra("order");
            // Hiển thị thông tin đơn hàng lên UI
            if (currentOrder != null) {
                tvOrderInfo.setText("Mã đơn: " + currentOrder.getOrderId() + 
                                  "\nNgày đặt: " + currentOrder.getOrderDate() + 
                                  "\nTổng tiền: " + currentOrder.getTotalAmount() + " VND");
                tvOrderInfo.setVisibility(View.VISIBLE);
                
                // Ẩn nút Pay Now và hiển thị loading
                btnPay.setVisibility(View.GONE);
                paymentProgress.setVisibility(View.VISIBLE);
                
                // Tự động bắt đầu thanh toán nếu có order
                startPayment();
            }
        } else {
            // Nếu không có order, hiển thị nút Pay Now bình thường
            btnPay.setVisibility(View.VISIBLE);
            paymentProgress.setVisibility(View.GONE);
        }

        btnPay.setOnClickListener(view -> {
            startPayment();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Shutdown executor service
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    // Parse order token từ response
    private String parseOrderToken(String response) {
        try {
            Log.d(TAG, "Parsing response: " + response);
            
            // Sử dụng JSONObject để parse response
            JSONObject jsonResponse = new JSONObject(response);
            
            // Kiểm tra return_code
            int returnCode = jsonResponse.getInt("return_code");
            if (returnCode == 1) {
                // Lấy order_token
                if (jsonResponse.has("order_token")) {
                    String token = jsonResponse.getString("order_token");
                    Log.d(TAG, "Parsed token using JSONObject: " + token);
                    return token;
                } else {
                    Log.e(TAG, "order_token not found in response");
                }
            } else {
                String returnMessage = jsonResponse.optString("return_message", "Unknown error");
                Log.e(TAG, "API returned error: " + returnMessage);
            }
            
            return null;
        } catch (JSONException e) {
            Log.e(TAG, "JSON parsing error: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to string parsing if JSON parsing fails
            return parseOrderTokenFallback(response);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing order token: " + e.getMessage());
                e.printStackTrace();
            return null;
        }
    }
    
    // Fallback method using string parsing
    private String parseOrderTokenFallback(String response) {
        try {
            Log.d(TAG, "Using fallback parsing method");
            
            // Tìm "order_token" trong response
            if (response.contains("\"order_token\"")) {
                // Tìm vị trí bắt đầu của order_token value
                int startIndex = response.indexOf("\"order_token\"") + 14; // "order_token":"
                Log.d(TAG, "Start index: " + startIndex);
                
                // Tìm dấu ngoặc kép đầu tiên sau "order_token":
                int firstQuote = response.indexOf("\"", startIndex);
                if (firstQuote != -1) {
                    // Tìm dấu ngoặc kép thứ hai (kết thúc của value)
                    int secondQuote = response.indexOf("\"", firstQuote + 1);
                    if (secondQuote != -1) {
                        String token = response.substring(firstQuote + 1, secondQuote);
                        Log.d(TAG, "Parsed token using fallback: " + token);
                        return token;
            }
                }
            }
            
            Log.e(TAG, "Could not parse order_token from response");
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Fallback parsing error: " + e.getMessage());
            return null;
        }
    }

    // Gọi hàm thanh toán
    private void payWithZaloPay() {
        Log.d(TAG, "Starting ZaloPay payment with token: " + ORDER_TOKEN);
        try {
        ZaloPaySDK.getInstance().payOrder(
                PaymentActivity.this,
                ORDER_TOKEN,
                    appScheme,
                new MyZaloPayListener()
        );
        } catch (Exception e) {
            Log.e(TAG, "Error starting ZaloPay payment: " + e.getMessage());
            Toast.makeText(this, "Error starting payment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Nhận callback deeplink
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent called: " + intent.getDataString());
        ZaloPaySDK.getInstance().onResult(intent);

        // Parse deeplink data để xác định trạng thái giao dịch
        if (intent.getData() != null) {
            String dataString = intent.getDataString();
            Log.d(TAG, "Deeplink data: " + dataString);
            // Ví dụ: yourappscheme://app?apptransid=...&status=1&zptransid=...
            String status = null;
            String apptransid = null;
            String zptransid = null;
            String[] params = dataString.split("[?&]");
            for (String param : params) {
                if (param.startsWith("status=")) status = param.replace("status=", "");
                if (param.startsWith("apptransid=")) apptransid = param.replace("apptransid=", "");
                if (param.startsWith("zptransid=")) zptransid = param.replace("zptransid=", "");
            }
            Log.d(TAG, "Parsed status: " + status + ", apptransid: " + apptransid + ", zptransid: " + zptransid);
            if (status != null) {
                Intent nextIntent;
                String message;
                if ("1".equals(status)) {
                    // Thành công
                    nextIntent = new Intent(this, PaymentSuccessActivity.class);
                    message = "Giao dịch thành công!\nMã giao dịch: " + (zptransid != null ? zptransid : "");
                } else if ("2".equals(status)) {
                    // Bị hủy
                    nextIntent = new Intent(this, PaymentFailedActivity.class);
                    message = "Bạn đã hủy giao dịch thanh toán.";
                } else {
                    // Thất bại
                    nextIntent = new Intent(this, PaymentFailedActivity.class);
                    message = "Giao dịch thất bại!";
                }
                nextIntent.putExtra("message", message);
                startActivity(nextIntent);
                finish();
                return;
            }
        }

        // Nếu không parse được, fallback về SharedPreferences như cũ
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String result = prefs.getString(KEY_PAYMENT_RESULT, null);
        String message = prefs.getString(KEY_PAYMENT_MESSAGE, null);
        if (result != null && message != null) {
            Intent nextIntent;
            if ("Thanh toán thành công".equals(result)) {
                nextIntent = new Intent(this, PaymentSuccessActivity.class);
            } else {
                nextIntent = new Intent(this, PaymentFailedActivity.class);
            }
            nextIntent.putExtra("message", message);
            startActivity(nextIntent);
            prefs.edit().remove(KEY_PAYMENT_RESULT).remove(KEY_PAYMENT_MESSAGE).apply();
            finish();
        }
    }

    // Xử lý kết quả thanh toán
    private class MyZaloPayListener implements PayOrderListener {
        @Override
        public void onPaymentSucceeded(String transactionId, String transToken, String appTransID) {
            Log.d(TAG, "Payment succeeded - transactionId: " + transactionId);
            // Chỉ lưu lịch sử thanh toán vào bảng Payments, KHÔNG cập nhật trạng thái đơn hàng
            executorService.execute(() -> {
                if (currentOrder != null) {
                    PaymentDAO paymentDAO = new PaymentDAO();
                    paymentDAO.insertPayment(
                        Integer.parseInt(currentOrder.getOrderId()),
                        currentOrder.getTotalAmount(),
                        "ZaloPay",
                        "Completed"
                    );
                }
                // Sau khi cập nhật xong, chuyển UI về main thread
                runOnUiThread(() -> {
                    String message = "Giao dịch của bạn đã được thanh toán thành công!\nMã giao dịch: " + transactionId;
                    paymentStatus.setText("Payment Successful! Transaction ID: " + transactionId);
                    paymentStatus.setVisibility(TextView.VISIBLE);
                    paymentError.setVisibility(TextView.GONE);
                    Toast.makeText(PaymentActivity.this, "Payment successful!", Toast.LENGTH_LONG).show();
                    savePaymentResult("Thanh toán thành công", message);
                    Intent intent = new Intent(PaymentActivity.this, PaymentSuccessActivity.class);
                    intent.putExtra("message", message);
                    startActivity(intent);
                    finish();
                });
            });
        }

        @Override
        public void onPaymentCanceled(String zpTransToken, String appTransID) {
            // Thanh toán bị hủy
            Log.d(TAG, "Payment canceled - zpTransToken: " + zpTransToken);
            executorService.execute(() -> {
                if (currentOrder != null) {
                    PaymentDAO paymentDAO = new PaymentDAO();
                    paymentDAO.insertPayment(
                        Integer.parseInt(currentOrder.getOrderId()),
                        currentOrder.getTotalAmount(),
                        "ZaloPay",
                        "Failed"
                    );
                }
            });
            runOnUiThread(() -> {
                String message = "Bạn đã hủy giao dịch thanh toán.";
                paymentStatus.setText("Payment Canceled!");
                paymentStatus.setVisibility(TextView.VISIBLE);
                paymentError.setVisibility(TextView.GONE);
                Toast.makeText(PaymentActivity.this, "Payment canceled", Toast.LENGTH_LONG).show();
                // Lưu trạng thái vào SharedPreferences
                savePaymentResult("Thanh toán bị hủy", message);
                // Chuyển sang màn hình thất bại
                Intent intent = new Intent(PaymentActivity.this, PaymentFailedActivity.class);
                intent.putExtra("message", message);
                startActivity(intent);
                finish();
            });
        }

        @Override
        public void onPaymentError(ZaloPayError zaloPayError, String zpTransToken, String appTransID) {
            // Thanh toán gặp lỗi
            Log.e(TAG, "Payment error: " + zaloPayError.toString());
            executorService.execute(() -> {
                if (currentOrder != null) {
                    PaymentDAO paymentDAO = new PaymentDAO();
                    paymentDAO.insertPayment(
                        Integer.parseInt(currentOrder.getOrderId()),
                        currentOrder.getTotalAmount(),
                        "ZaloPay",
                        "Failed"
                    );
                }
            });
            runOnUiThread(() -> {
                String message = "Giao dịch thất bại: " + zaloPayError.toString();
                paymentStatus.setVisibility(TextView.GONE);
                paymentError.setText("Payment Error: " + zaloPayError.toString());
                paymentError.setVisibility(TextView.VISIBLE);
                Toast.makeText(PaymentActivity.this, "Payment error: " + zaloPayError.toString(), Toast.LENGTH_LONG).show();
                // Lưu trạng thái vào SharedPreferences
                savePaymentResult("Thanh toán thất bại", message);
                // Chuyển sang màn hình thất bại
                Intent intent = new Intent(PaymentActivity.this, PaymentFailedActivity.class);
                intent.putExtra("message", message);
                startActivity(intent);
                finish();
            });
        }
    }

    // Lưu trạng thái giao dịch vào SharedPreferences
    private void savePaymentResult(String result, String message) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit()
            .putString(KEY_PAYMENT_RESULT, result)
            .putString(KEY_PAYMENT_MESSAGE, message)
            .apply();
    }

    // Method để bắt đầu thanh toán
    private void startPayment() {
        Log.d(TAG, "Starting payment process");
        
        // Disable button để tránh multiple clicks
        Button btnPay = findViewById(R.id.btnPay);
        ProgressBar paymentProgress = findViewById(R.id.paymentProgress);
        btnPay.setEnabled(false);
        
        // Hiển thị thông báo đang xử lý
        Toast.makeText(this, "Đang tạo đơn hàng...", Toast.LENGTH_SHORT).show();
        
        // Chạy network request trên background thread
        executorService.execute(() -> {
            try {
                // Tạo orderId duy nhất cho mỗi lần thanh toán
                String uniqueOrderId = String.valueOf(System.currentTimeMillis());
                
                // Sử dụng thông tin từ currentOrder nếu có, nếu không thì tạo order mới
                Order order;
                if (currentOrder != null) {
                    // Sử dụng thông tin đơn hàng từ OrderManagementActivity
                    order = new Order(
                        currentOrder.getOrderId(),
                        currentOrder.getOrderDate(),
                        "Pending",
                        currentOrder.getTotalAmount(),
                        "Payment for order " + currentOrder.getOrderId(),
                        new ArrayList<>()
                    );
                    Log.d(TAG, "Using existing order: " + currentOrder.getOrderId() + " with amount: " + currentOrder.getTotalAmount());
                } else {
                    // Tạo order mới với dữ liệu mẫu
                    order = new Order(uniqueOrderId, "2023-07-03", "Completed", 1240.00, "Fast shipping please", new ArrayList<>());
                    Log.d(TAG, "Creating new order with amount: " + order.getTotalAmount());
                }
                
                String response = ZaloPayApi.createOrder(PaymentActivity.this, order); // Truyền context vào
                Log.d(TAG, "ZaloPay API response: " + response);
                
                // Parse response để lấy order_token
                String orderToken = parseOrderToken(response);
                Log.d(TAG, "Parsed order token: " + orderToken);
                
                if (orderToken != null && !orderToken.isEmpty()) {
                    ORDER_TOKEN = orderToken;
                    // Chạy UI update trên main thread
                    runOnUiThread(() -> {
                        btnPay.setEnabled(true);
                        paymentProgress.setVisibility(View.GONE);
                        payWithZaloPay();
                    });
                } else {
                    runOnUiThread(() -> {
                        btnPay.setEnabled(true);
                        paymentProgress.setVisibility(View.GONE);
                        Toast.makeText(PaymentActivity.this, "Failed to get order token from ZaloPay", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Order token is null or empty");
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Error creating order: " + e.getMessage());
                runOnUiThread(() -> {
                    btnPay.setEnabled(true);
                    paymentProgress.setVisibility(View.GONE);
                    Toast.makeText(PaymentActivity.this, "Error creating order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Unexpected error: " + e.getMessage());
                runOnUiThread(() -> {
                    btnPay.setEnabled(true);
                    paymentProgress.setVisibility(View.GONE);
                    Toast.makeText(PaymentActivity.this, "Unexpected error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
