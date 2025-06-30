package com.example.prm392_finalproject.views;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.controllers.ShippingRepository;
import com.example.prm392_finalproject.models.Shipping;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "MapActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private ShippingRepository shippingRepository;

    // UI Components
    private TextView tvShippingInfo, tvConnectionStatus;
    private Button btnUpdateLocation, btnNavigate, btnRefresh;

    // Data
    private Shipping currentShipping;
    private int shippingId;
    private LatLng currentLocation;
    private LatLng destinationLocation;
    private Marker currentLocationMarker;
    private Marker destinationMarker;
    private Polyline routePolyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        initViews();
        initData();
        setupMap();
        checkLocationPermission();
    }

    private void initViews() {
        tvShippingInfo = findViewById(R.id.tvShippingInfo);
        tvConnectionStatus = findViewById(R.id.tvConnectionStatus);
        btnUpdateLocation = findViewById(R.id.btnUpdateLocation);
        btnNavigate = findViewById(R.id.btnNavigate);
        btnRefresh = findViewById(R.id.btnRefresh);

        btnUpdateLocation.setOnClickListener(v -> updateCurrentLocation());
        btnNavigate.setOnClickListener(v -> navigateToDestination());
        btnRefresh.setOnClickListener(v -> refreshData());
    }

    private void initData() {
        shippingRepository = new ShippingRepository();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Get shipping ID from intent
        Intent intent = getIntent();
        shippingId = intent.getIntExtra("shipping_id", -1);

        if (shippingId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin giao hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadShippingData();
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Configure map settings
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false); // We have our own button

        // Set map style
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Set map click listener
        mMap.setOnMapClickListener(latLng -> {
            String coordinates = String.format(Locale.getDefault(),
                    "📍 Lat: %.6f, Lng: %.6f", latLng.latitude, latLng.longitude);
            Toast.makeText(this, coordinates, Toast.LENGTH_SHORT).show();
        });

        // Set marker click listeners
        mMap.setOnMarkerClickListener(marker -> {
            marker.showInfoWindow();
            return true;
        });

        // Load shipping location
        if (currentShipping != null) {
            loadDestinationLocation();
        }

        // Get current location
        getCurrentLocation();

        updateConnectionStatus(true);
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Cần quyền truy cập vị trí để sử dụng tính năng này",
                        Toast.LENGTH_LONG).show();
                updateConnectionStatus(false);
            }
        }
    }

    private void loadShippingData() {
        new LoadShippingTask().execute(shippingId);
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (mMap != null) {
            mMap.setMyLocationEnabled(true);
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            updateCurrentLocationMarker();

                            // Move camera to current location if no destination set
                            if (destinationLocation == null) {
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                            } else {
                                showBothLocations();
                            }
                            updateConnectionStatus(true);
                        } else {
                            // Default to Ho Chi Minh City if can't get location
                            currentLocation = new LatLng(10.8231, 106.6297);
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12));
                            Toast.makeText(MapActivity.this, "Không thể lấy vị trí hiện tại. Sử dụng vị trí mặc định.",
                                    Toast.LENGTH_SHORT).show();
                            updateConnectionStatus(false);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get location: " + e.getMessage());
                    updateConnectionStatus(false);
                });
    }

    private void loadDestinationLocation() {
        if (currentShipping == null || currentShipping.getShippingAddress() == null) {
            return;
        }

        new GeocodeAddressTask().execute(currentShipping.getShippingAddress());
    }

    private void updateCurrentLocationMarker() {
        if (mMap == null || currentLocation == null) return;

        if (currentLocationMarker != null) {
            currentLocationMarker.remove();
        }

        currentLocationMarker = mMap.addMarker(new MarkerOptions()
                .position(currentLocation)
                .title("🚚 Vị trí shipper")
                .snippet("Đang ở đây")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
    }

    private void updateDestinationMarker() {
        if (mMap == null || destinationLocation == null || currentShipping == null) return;

        if (destinationMarker != null) {
            destinationMarker.remove();
        }

        String snippet = "👤 " + currentShipping.getShippingPersonName() + "\n📱 " +
                currentShipping.getTrackingNumber();

        destinationMarker = mMap.addMarker(new MarkerOptions()
                .position(destinationLocation)
                .title("🎯 Điểm giao hàng")
                .snippet(snippet)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    }

    private void showBothLocations() {
        if (currentLocation != null && destinationLocation != null && mMap != null) {
            // Calculate bounds to show both locations
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(currentLocation);
            builder.include(destinationLocation);

            LatLngBounds bounds = builder.build();
            int padding = 150; // padding in pixels

            try {
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
            } catch (Exception e) {
                Log.e(TAG, "Error moving camera: " + e.getMessage());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12));
            }

            // Draw route line
            drawRoute();
        }
    }

    private void drawRoute() {
        if (currentLocation == null || destinationLocation == null || mMap == null) return;

        if (routePolyline != null) {
            routePolyline.remove();
        }

        // Simple straight line route (in real app, use Directions API)
        PolylineOptions polylineOptions = new PolylineOptions()
                .add(currentLocation)
                .add(destinationLocation)
                .width(8)
                .color(ContextCompat.getColor(this, R.color.accent))
                .geodesic(true);

        routePolyline = mMap.addPolyline(polylineOptions);
    }

    private void updateCurrentLocation() {
        getCurrentLocation();
        Toast.makeText(this, "Đã cập nhật vị trí hiện tại", Toast.LENGTH_SHORT).show();
    }

    private void navigateToDestination() {
        if (destinationLocation == null) {
            Toast.makeText(this, "Không có điểm đến để dẫn đường", Toast.LENGTH_SHORT).show();
            return;
        }

        // Open Google Maps app for navigation
        Intent intent = new Intent(Intent.ACTION_VIEW,
                android.net.Uri.parse("google.navigation:q=" +
                        destinationLocation.latitude + "," + destinationLocation.longitude));
        intent.setPackage("com.google.android.apps.maps");

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            // Fallback to browser
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" +
                            destinationLocation.latitude + "," + destinationLocation.longitude));
            startActivity(browserIntent);
        }
    }

    private void refreshData() {
        loadShippingData();
        getCurrentLocation();
        Toast.makeText(this, "Đã làm mới dữ liệu", Toast.LENGTH_SHORT).show();
    }

    private void updateShippingInfo() {
        if (currentShipping == null) return;

        String info = "🚚 " + currentShipping.getTrackingNumber() + "\n" +
                "📦 Đơn hàng: #" + currentShipping.getOrderId() + "\n" +
                "👤 " + currentShipping.getShippingPersonName() + "\n" +
                "📍 " + currentShipping.getShippingAddress() + "\n" +
                "📊 " + currentShipping.getStatusDisplay();

        tvShippingInfo.setText(info);
    }

    private void updateConnectionStatus(boolean connected) {
        if (tvConnectionStatus != null) {
            if (connected) {
                tvConnectionStatus.setText("🟢 Kết nối");
                tvConnectionStatus.setTextColor(ContextCompat.getColor(this, R.color.status_active));
            } else {
                tvConnectionStatus.setText("🔴 Mất kết nối");
                tvConnectionStatus.setTextColor(ContextCompat.getColor(this, R.color.status_inactive));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentShipping != null) {
            updateShippingInfo();
        }
    }

    // AsyncTask classes
    private class LoadShippingTask extends AsyncTask<Integer, Void, Shipping> {
        @Override
        protected Shipping doInBackground(Integer... shippingIds) {
            try {
                return shippingRepository.getShippingById(shippingIds[0]);
            } catch (Exception e) {
                Log.e(TAG, "Error loading shipping data: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(Shipping shipping) {
            if (shipping != null) {
                currentShipping = shipping;
                updateShippingInfo();
                if (mMap != null) {
                    loadDestinationLocation();
                }
            } else {
                Toast.makeText(MapActivity.this, "Không tìm thấy thông tin giao hàng",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private class GeocodeAddressTask extends AsyncTask<String, Void, LatLng> {
        @Override
        protected LatLng doInBackground(String... addresses) {
            Geocoder geocoder = new Geocoder(MapActivity.this, Locale.getDefault());
            try {
                List<Address> addressList = geocoder.getFromLocationName(addresses[0], 1);
                if (addressList != null && addressList.size() > 0) {
                    Address address = addressList.get(0);
                    return new LatLng(address.getLatitude(), address.getLongitude());
                }
            } catch (IOException e) {
                Log.e(TAG, "Geocoding error: " + e.getMessage());
            }

            // Default to Ho Chi Minh City center if geocoding fails
            return new LatLng(10.8231, 106.6297);
        }

        @Override
        protected void onPostExecute(LatLng latLng) {
            destinationLocation = latLng;
            updateDestinationMarker();

            if (currentLocation != null) {
                showBothLocations();
            } else if (mMap != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destinationLocation, 15));
            }
        }
    }
}