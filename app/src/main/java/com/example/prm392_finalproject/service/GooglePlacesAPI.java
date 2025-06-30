package com.example.prm392_finalproject.service;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

// Interface cho Google Places API
public interface GooglePlacesAPI {

    // Geocoding - chuyển địa chỉ thành coordinates
    @GET("geocode/json")
    Call<GeocodingResponse> geocodeAddress(
            @Query("address") String address,
            @Query("key") String apiKey,
            @Query("language") String language
    );

    // Reverse Geocoding - chuyển coordinates thành địa chỉ
    @GET("geocode/json")
    Call<GeocodingResponse> reverseGeocode(
            @Query("latlng") String latlng,
            @Query("key") String apiKey,
            @Query("language") String language
    );

    // Places Autocomplete
    @GET("place/autocomplete/json")
    Call<AutocompleteResponse> getPlaceAutocomplete(
            @Query("input") String input,
            @Query("key") String apiKey,
            @Query("language") String language,
            @Query("components") String components
    );
}

// Response models cho Places API
class GeocodingResponse {
    public GeocodingResult[] results;
    public String status;
}

class GeocodingResult {
    public String formatted_address;
    public Geometry geometry;
    public AddressComponent[] address_components;
}

class Geometry {
    public Location location;
    public String location_type;
}

class Location {
    public double lat;
    public double lng;
}

class AddressComponent {
    public String long_name;
    public String short_name;
    public String[] types;
}

class AutocompleteResponse {
    public AutocompletePrediction[] predictions;
    public String status;
}

class AutocompletePrediction {
    public String description;
    public String place_id;
    public StructuredFormatting structured_formatting;
}

class StructuredFormatting {
    public String main_text;
    public String secondary_text;
}