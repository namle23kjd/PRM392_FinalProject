# Hướng dẫn cấu hình ZaloPay Sandbox

## 🔧 Cấu hình cần thiết

### 1. Đăng ký tài khoản ZaloPay Developer
- Truy cập: https://docs.zalopay.vn/
- Đăng ký tài khoản developer
- Tạo ứng dụng mới trong sandbox environment

### 2. Cập nhật thông tin cấu hình

Mở file `app/src/main/res/values/strings.xml` và cập nhật các thông tin sau:

```xml
<!-- ===== ZALOPAY CONFIGURATION ===== -->
<string name="zalopay_app_id">YOUR_APP_ID</string>
<string name="zalopay_app_user">YOUR_APP_USER</string>
<string name="zalopay_payment_code">YOUR_PAYMENT_CODE</string>
<string name="zalopay_secret_key">YOUR_SECRET_KEY</string>
<string name="zalopay_app_scheme">YOUR_APP_SCHEME</string>
```

### 3. Thông tin cần thay thế:

- **YOUR_APP_ID**: App ID từ ZaloPay Developer Portal
- **YOUR_APP_USER**: ID người dùng (có thể là user123 cho test)
- **YOUR_PAYMENT_CODE**: Mã thanh toán từ ZaloPay
- **YOUR_SECRET_KEY**: Secret key từ ZaloPay Developer Portal
- **YOUR_APP_SCHEME**: Scheme cho deep link (ví dụ: myapp)

### 4. Cấu hình AndroidManifest.xml

Đảm bảo PaymentActivity đã được cấu hình đúng trong AndroidManifest.xml:

```xml
<activity
    android:name=".views.PaymentActivity"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data
            android:host="app"
            android:scheme="@string/zalopay_app_scheme" />
    </intent-filter>
</activity>
```

## 🧪 Test ZaloPay Sandbox

### 1. Test với Mock Response (Không cần thông tin thật)
Hiện tại ứng dụng đã được cấu hình để sử dụng mock response khi `secret_key` là "sandbox_secret_key". Điều này cho phép bạn test flow thanh toán mà không cần thông tin ZaloPay thật.

**Để test với mock response:**
- Giữ nguyên `zalopay_secret_key` là "sandbox_secret_key"
- Chạy ứng dụng và nhấn "Pay Now"
- Ứng dụng sẽ sử dụng mock order token và cố gắng mở ZaloPay

### 2. Test với ZaloPay thật
**Để test với ZaloPay thật:**
- Cài đặt ZaloPay app trên thiết bị test
- Đăng nhập bằng tài khoản sandbox
- Cập nhật thông tin thật trong `strings.xml`
- Chạy ứng dụng và test thanh toán

### 3. Debug logs
Kiểm tra logcat với tag "PaymentActivity" để xem chi tiết:
```
adb logcat | grep PaymentActivity
```

## ⚠️ Lưu ý quan trọng

1. **Secret Key**: Không bao giờ commit secret key vào git
2. **Sandbox vs Production**: Đảm bảo đang sử dụng sandbox environment
3. **Network**: Cần kết nối internet để test
4. **ZaloPay App**: Phải cài đặt ZaloPay app để test với thông tin thật
5. **Mock Response**: Khi sử dụng mock response, ZaloPay sẽ không mở nhưng bạn có thể test flow của ứng dụng

## 🔍 Troubleshooting

### Lỗi thường gặp:

1. **"NetworkOnMainThreadException"**
   - ✅ Đã sửa: Network request giờ chạy trên background thread

2. **"Failed to get order token"**
   - Kiểm tra thông tin cấu hình
   - Kiểm tra kết nối internet
   - Kiểm tra log để xem response từ API

3. **"Payment error"**
   - Kiểm tra ZaloPay app đã cài đặt chưa
   - Kiểm tra tài khoản sandbox

4. **ZaloPay không mở**
   - Kiểm tra app scheme trong AndroidManifest.xml
   - Kiểm tra ZaloPay app đã cài đặt
   - Nếu đang dùng mock response, ZaloPay sẽ không mở (điều này là bình thường)

## 🚀 Các cải tiến đã thực hiện

1. **Background Thread**: Network request giờ chạy trên background thread
2. **Mock Response**: Hỗ trợ test mà không cần thông tin ZaloPay thật
3. **Better Error Handling**: Xử lý lỗi tốt hơn với logging chi tiết
4. **UI Feedback**: Disable button khi đang xử lý, hiển thị thông báo rõ ràng
5. **Resource Management**: Proper cleanup của ExecutorService

## 📞 Hỗ trợ

Nếu gặp vấn đề, tham khảo:
- [ZaloPay Documentation](https://docs.zalopay.vn/)
- [ZaloPay SDK GitHub](https://github.com/ZaloPay/zalopay-sdk-android) 