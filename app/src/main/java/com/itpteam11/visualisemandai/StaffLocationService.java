package com.itpteam11.visualisemandai;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.FirebaseDatabase;

/**
 *  This service updates user's current coordinates in Firebase to locate staff
 */
public class StaffLocationService extends Service implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
    //Set minimum time to update user location at 30 seconds
    private static final long MIN_TIME_TO_UPDATE = 1000*30;
    //Set minimum distance changed to update user location at 5 metres
    private static final long MIN_DISTANCE_TO_UPDATE = 5;

    private final Context context;
    private String userID;

    private boolean isLocationPermissionGranted = false;

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;

    private double latitude;
    private double longitude;

    public StaffLocationService(Context context, String userID) {
        this.context = context;
        this.userID = userID;
        startLocationUpdate();
    }

    private void startLocationUpdate() {
        //Check if the user has granted permission to access location service
        if(ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            isLocationPermissionGranted = true;

            //Initialise Google API Client
            if (googleApiClient == null) {
                googleApiClient = new GoogleApiClient.Builder(context)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();
            }

            //Connect Google API Client
            if(!googleApiClient.isConnected()){
                googleApiClient.connect();
            }

            //Initialise location request
            locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(MIN_TIME_TO_UPDATE)
                    .setSmallestDisplacement(MIN_DISTANCE_TO_UPDATE);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }

    //Callback method from LocationRequest
    @Override
    public void onLocationChanged(Location location) {
        updateLocation(location.getLatitude(), location.getLongitude());
    }

    //This method update user location into Firebase
    private void updateLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;

        FirebaseDatabase.getInstance().getReference().child("user").child(userID).child("coordinates").setValue(latitude + "-" + longitude);

        System.out.println("StaffLocationService - User location: " + latitude + "-" + longitude);
        Toast.makeText(context, latitude + "-" + longitude, Toast.LENGTH_LONG).show();
    }

    //Stop receive location updates by disconnect Google API Client
    public void stopLocationUpdate() {
        if(ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleApiClient.disconnect();
        }
    }

    public boolean isLocationPermissionGranted() { return isLocationPermissionGranted; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onConnectionSuspended(int i) {}
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}
}
