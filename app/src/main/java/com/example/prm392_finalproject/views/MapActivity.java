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
    private TextView tvShippingInfo;
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
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Set map click listener
        mMap.setOnMapClickListener(latLng -> {
            Toast.makeText(this, "Lat: " + latLng.latitude + ", Lng: " + latLng.longitude,
                    Toast.LENGTH_SHORT).show();
        });

        // Load shipping location
        if (currentShipping != null) {
            loadDestinationLocation();
        }

        // Get current location
        getCurrentLocation();
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

        mMap.setMyLocationEnabled(true);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            updateCurrentLocationMarker();

                            // Move camera to current location if no destination set
                            if (destinationLocation == null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                            } else {
                                showBothLocations();
                            }
                        } else {
                            // Default to Ho Chi Minh City if can't get location
                            currentLocation = new LatLng(10.8231, 106.6297);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12));
                            Toast.makeText(MapActivity.this, "Không thể lấy vị trí hiện tại",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
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
                .title("Vị trí hiện tại")
                .snippet("Shipper đang ở đây")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
    }

    private void updateDestinationMarker() {
        if (mMap == null || destinationLocation == null) return;

        if (destinationMarker != null) {
            destinationMarker.remove();
        }

        destinationMarker = mMap.addMarker(new MarkerOptions()
                .position(destinationLocation)
                .title("Điểm giao hàng")
                .snippet(currentShipping.getShippingPersonName())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    }

    private void showBothLocations() {
        if (currentLocation != null && destinationLocation != null) {
            // Calculate bounds to show both locations
            double minLat = Math.min(currentLocation.latitude, destinationLocation.latitude);
            double maxLat = Math.max(currentLocation.latitude, destinationLocation.latitude);
            double minLng = Math.min(currentLocation.longitude, destinationLocation.longitude);
            double maxLng = Math.max(currentLocation.longitude, destinationLocation.longitude);

            LatLng southwest = new LatLng(minLat - 0.01, minLng - 0.01);
            LatLng northeast = new LatLng(maxLat + 0.01, maxLng + 0.01);

            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                    new com.google.android.gms.maps.model.LatLngBounds(southwest, northeast), 100));

            // Draw route line
            drawRoute();
        }
    }

    private void drawRoute() {
        if (currentLocation == null || destinationLocation == null) return;

        if (routePolyline != null) {
            routePolyline.remove();
        }

        // Simple straight line route (in real app, use Directions API)
        PolylineOptions polylineOptions = new PolylineOptions()
                .add(currentLocation)
                .add(destinationLocation)
                .width(8)
                .color(getResources().getColor(R.color.route_color))
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
            Toast.makeText(this, "Cần cài đặt Google Maps để dẫn đường", Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshData() {
        loadShippingData();
        getCurrentLocation();
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

    // AsyncTask classes
    private class LoadShippingTask extends AsyncTask<Integer, Void, Shipping> {
        @Override
        protected Shipping doInBackground(Integer... shippingIds) {
            return shippingRepository.getShippingById(shippingIds[0]);
        }

        @Override
        protected void onPostExecute(Shipping shipping) {
            if (shipping != null) {
                currentShipping = shipping;
                updateShippingInfo();
                loadDestinationLocation();
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
            } else {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destinationLocation, 15));
            }
        }
    }
}