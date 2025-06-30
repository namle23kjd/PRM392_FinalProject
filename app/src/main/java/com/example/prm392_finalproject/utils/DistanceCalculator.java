// ===== DistanceCalculator.java =====
package com.example.prm392_finalproject.utils;

import android.content.Context;
import android.util.Log;

import com.example.prm392_finalproject.service.MapsServiceManager;
import com.google.android.gms.maps.model.LatLng;

public class DistanceCalculator {
    private static final String TAG = "DistanceCalculator";

    // Tọa độ kho hàng (bạn có thể thay đổi theo vị trí thực tế)
    public static final LatLng WAREHOUSE_LOCATION = new LatLng(10.7769, 106.7009); // TP.HCM
    public static final String WAREHOUSE_ADDRESS = "Kho hàng chính - TP. Hồ Chí Minh";

    private MapsServiceManager mapsServiceManager;
    private Context context;

    public DistanceCalculator(Context context) {
        this.context = context;
        this.mapsServiceManager = new MapsServiceManager(context);
    }

    // Tính khoảng cách từ kho đến địa chỉ khách hàng
    public void calculateDistanceToCustomer(String customerAddress, DistanceCallback callback) {
        Log.d(TAG, "Calculating distance to: " + customerAddress);

        // Bước 1: Geocode địa chỉ khách hàng
        mapsServiceManager.geocodeAddress(customerAddress, new MapsServiceManager.GeocodingCallback() {
            @Override
            public void onSuccess(LatLng latLng, String formattedAddress) {
                Log.d(TAG, "Customer location found: " + latLng.toString());

                // Bước 2: Tính khoảng cách từ kho đến khách hàng
                calculateDistance(WAREHOUSE_LOCATION, latLng, formattedAddress, callback);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Geocoding failed: " + error);
                callback.onError("Không tìm thấy địa chỉ: " + error);
            }
        });
    }

    // Tính khoảng cách giữa 2 điểm đã có tọa độ
    public void calculateDistance(LatLng origin, LatLng destination, String destinationAddress, DistanceCallback callback) {
        mapsServiceManager.getDirections(origin, destination, new MapsServiceManager.DirectionsCallback() {
            @Override
            public void onSuccess(java.util.List<LatLng> routePoints, String distance, String duration) {
                Log.d(TAG, "Distance calculated: " + distance + ", Duration: " + duration);

                DistanceResult result = new DistanceResult();
                result.distance = distance;
                result.duration = duration;
                result.customerLocation = destination;
                result.customerAddress = destinationAddress;
                result.warehouseLocation = origin;
                result.warehouseAddress = WAREHOUSE_ADDRESS;
                result.routePoints = routePoints;

                callback.onSuccess(result);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Distance calculation failed: " + error);
                callback.onError("Không thể tính khoảng cách: " + error);
            }
        });
    }

    // Kiểm tra xem địa chỉ có trong phạm vi giao hàng không
    public boolean isWithinDeliveryRange(String distanceText, int maxDistanceKm) {
        try {
            // Extract số từ chuỗi distance (ví dụ: "15.2 km" -> 15.2)
            String numberOnly = distanceText.replaceAll("[^\\d.,]", "");
            double distance = Double.parseDouble(numberOnly.replace(",", "."));

            return distance <= maxDistanceKm;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing distance: " + e.getMessage());
            return false;
        }
    }

    // Tính phí giao hàng dựa trên khoảng cách
    public double calculateShippingFee(String distanceText, String shippingMethod) {
        try {
            String numberOnly = distanceText.replaceAll("[^\\d.,]", "");
            double distance = Double.parseDouble(numberOnly.replace(",", "."));

            double baseFee;
            double perKmFee;

            switch (shippingMethod.toLowerCase()) {
                case "express":
                    baseFee = 30000; // 30k VNĐ
                    perKmFee = 3000; // 3k VNĐ/km
                    break;
                case "standard":
                default:
                    baseFee = 20000; // 20k VNĐ
                    perKmFee = 2000; // 2k VNĐ/km
                    break;
            }

            return baseFee + (distance * perKmFee);

        } catch (Exception e) {
            Log.e(TAG, "Error calculating shipping fee: " + e.getMessage());
            return 20000; // Default fee
        }
    }

    // ===== CALLBACK & RESULT CLASSES =====

    public interface DistanceCallback {
        void onSuccess(DistanceResult result);
        void onError(String error);
    }

    public static class DistanceResult {
        public String distance;
        public String duration;
        public LatLng warehouseLocation;
        public String warehouseAddress;
        public LatLng customerLocation;
        public String customerAddress;
        public java.util.List<LatLng> routePoints;

        public String getFormattedInfo() {
            return "📍 Từ: " + warehouseAddress + "\n" +
                    "📍 Đến: " + customerAddress + "\n" +
                    "📏 Khoảng cách: " + distance + "\n" +
                    "⏱️ Thời gian dự kiến: " + duration;
        }

        public boolean isValidForDelivery(int maxKm) {
            try {
                String numberOnly = distance.replaceAll("[^\\d.,]", "");
                double dist = Double.parseDouble(numberOnly.replace(",", "."));
                return dist <= maxKm;
            } catch (Exception e) {
                return false;
            }
        }
    }
}