package com.example.prm392_finalproject.service;

import android.content.Context;
import android.util.Log;

import com.example.prm392_finalproject.R;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapsServiceManager {
    private static final String TAG = "MapsServiceManager";
    private static final String GOOGLE_MAPS_BASE_URL = "https://maps.googleapis.com/maps/api/";

    private Context context;
    private String apiKey;
    private GoogleDirectionsAPI directionsAPI;
    private GooglePlacesAPI placesAPI;

    public MapsServiceManager(Context context) {
        this.context = context;
        this.apiKey = context.getString(R.string.google_maps_api_key);
        setupRetrofit();
    }

    private void setupRetrofit() {
        // Setup HTTP logging
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Setup OkHttp client
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        // Setup Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GOOGLE_MAPS_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        directionsAPI = retrofit.create(GoogleDirectionsAPI.class);
        placesAPI = retrofit.create(GooglePlacesAPI.class);
    }

    // ===== DIRECTIONS API =====

    public void getDirections(LatLng origin, LatLng destination, DirectionsCallback callback) {
        String originStr = origin.latitude + "," + origin.longitude;
        String destinationStr = destination.latitude + "," + destination.longitude;

        Call<DirectionsResponse> call = directionsAPI.getDirections(
                originStr,
                destinationStr,
                apiKey,
                "driving",
                "vi"
        );

        call.enqueue(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DirectionsResponse directionsResponse = response.body();

                    if ("OK".equals(directionsResponse.status) &&
                            directionsResponse.routes != null &&
                            directionsResponse.routes.length > 0) {

                        DirectionsRoute route = directionsResponse.routes[0];

                        // Decode polyline points
                        List<LatLng> points = decodePolyline(route.overview_polyline.points);

                        // Get distance and duration
                        String distance = "";
                        String duration = "";

                        if (route.legs != null && route.legs.length > 0) {
                            DirectionsLeg leg = route.legs[0];
                            distance = leg.distance.text;
                            duration = leg.duration.text;
                        }

                        callback.onSuccess(points, distance, duration);
                    } else {
                        callback.onError("Không tìm thấy đường đi");
                    }
                } else {
                    callback.onError("Lỗi kết nối API: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                Log.e(TAG, "Directions API error: " + t.getMessage());
                callback.onError("Lỗi kết nối mạng");
            }
        });
    }

    // ===== GEOCODING API =====

    public void geocodeAddress(String address, GeocodingCallback callback) {
        Call<GeocodingResponse> call = placesAPI.geocodeAddress(address, apiKey, "vi");

        call.enqueue(new Callback<GeocodingResponse>() {
            @Override
            public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GeocodingResponse geocodingResponse = response.body();

                    if ("OK".equals(geocodingResponse.status) &&
                            geocodingResponse.results != null &&
                            geocodingResponse.results.length > 0) {

                        GeocodingResult result = geocodingResponse.results[0];
                        Location location = result.geometry.location;
                        LatLng latLng = new LatLng(location.lat, location.lng);

                        callback.onSuccess(latLng, result.formatted_address);
                    } else {
                        callback.onError("Không tìm thấy địa chỉ");
                    }
                } else {
                    callback.onError("Lỗi geocoding: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GeocodingResponse> call, Throwable t) {
                Log.e(TAG, "Geocoding error: " + t.getMessage());
                callback.onError("Lỗi kết nối mạng");
            }
        });
    }

    public void reverseGeocode(LatLng latLng, ReverseGeocodingCallback callback) {
        String latlngStr = latLng.latitude + "," + latLng.longitude;

        Call<GeocodingResponse> call = placesAPI.reverseGeocode(latlngStr, apiKey, "vi");

        call.enqueue(new Callback<GeocodingResponse>() {
            @Override
            public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GeocodingResponse geocodingResponse = response.body();

                    if ("OK".equals(geocodingResponse.status) &&
                            geocodingResponse.results != null &&
                            geocodingResponse.results.length > 0) {

                        GeocodingResult result = geocodingResponse.results[0];
                        callback.onSuccess(result.formatted_address);
                    } else {
                        callback.onError("Không tìm thấy địa chỉ");
                    }
                } else {
                    callback.onError("Lỗi reverse geocoding: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GeocodingResponse> call, Throwable t) {
                Log.e(TAG, "Reverse geocoding error: " + t.getMessage());
                callback.onError("Lỗi kết nối mạng");
            }
        });
    }

    // ===== UTILITY METHODS =====

    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)), (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    // ===== CALLBACK INTERFACES =====

    public interface DirectionsCallback {
        void onSuccess(List<LatLng> routePoints, String distance, String duration);
        void onError(String error);
    }

    public interface GeocodingCallback {
        void onSuccess(LatLng latLng, String formattedAddress);
        void onError(String error);
    }

    public interface ReverseGeocodingCallback {
        void onSuccess(String address);
        void onError(String error);
    }
}
