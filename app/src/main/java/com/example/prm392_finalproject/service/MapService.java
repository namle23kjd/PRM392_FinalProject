package com.example.prm392_finalproject.service;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

// ===== MODEL CLASSES =====

class Location {
    public double lat;
    public double lng;
}

class Distance {
    public String text;
    public int value;
}

class Duration {
    public String text;
    public int value;
}

class OverviewPolyline {
    public String points;
}

class Viewport {
    public Location northeast;
    public Location southwest;
}

class Geometry {
    public Location location;
    public String location_type;
    public Viewport viewport;
}

class DirectionsLeg {
    public Distance distance;
    public Duration duration;
    public Location start_location;
    public Location end_location;
}

class DirectionsRoute {
    public DirectionsLeg[] legs;
    public OverviewPolyline overview_polyline;
}

class DirectionsResponse {
    public String status;
    public DirectionsRoute[] routes;
}

class GeocodingResult {
    public String formatted_address;
    public Geometry geometry;
}

class GeocodingResponse {
    public String status;
    public GeocodingResult[] results;
}

// ===== API INTERFACES =====

interface GoogleDirectionsAPI {
    @GET("directions/json")
    Call<DirectionsResponse> getDirections(
            @Query("origin") String origin,
            @Query("destination") String destination,
            @Query("key") String apiKey,
            @Query("mode") String mode,
            @Query("language") String language
    );
}

interface GooglePlacesAPI {
    @GET("geocode/json")
    Call<GeocodingResponse> geocodeAddress(
            @Query("address") String address,
            @Query("key") String apiKey,
            @Query("language") String language
    );

    @GET("geocode/json")
    Call<GeocodingResponse> reverseGeocode(
            @Query("latlng") String latlng,
            @Query("key") String apiKey,
            @Query("language") String language
    );
}