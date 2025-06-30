package com.example.prm392_finalproject.service;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

// Interface cho Google Directions API
public interface GoogleDirectionsAPI {

    @GET("directions/json")
    Call<DirectionsResponse> getDirections(
            @Query("origin") String origin,
            @Query("destination") String destination,
            @Query("key") String apiKey,
            @Query("mode") String mode,
            @Query("language") String language
    );
}

// Response model cho Directions API
class DirectionsResponse {
    public DirectionsRoute[] routes;
    public String status;
}

class DirectionsRoute {
    public DirectionsLeg[] legs;
    public OverviewPolyline overview_polyline;
}

class DirectionsLeg {
    public DirectionsDistance distance;
    public DirectionsDuration duration;
    public DirectionsStep[] steps;
}

class DirectionsDistance {
    public String text;
    public int value;
}

class DirectionsDuration {
    public String text;
    public int value;
}

class DirectionsStep {
    public DirectionsDistance distance;
    public DirectionsDuration duration;
    public String html_instructions;
    public OverviewPolyline polyline;
}

class OverviewPolyline {
    public String points;
}