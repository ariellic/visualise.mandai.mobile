package com.itpteam11.visualisemandai;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;

/**
 *  This service updates user's current coordinates in Firebase to locate staff
 */
public class StaffLocationService extends Service implements LocationListener {
    //Set minimum time to update user location at 30 seconds
    private static final long MIN_TIME_TO_UPDATE = 1000*30;
    //Set minimum distance changed to update user location at 5 metres
    private static final long MIN_DISTANCE_TO_UPDATE = 5;

    private final Context context;
    private String userID;

    private boolean isLocationPermissionGranted = false;
    private LocationManager locationManager;

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

            try {
                locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_TO_UPDATE, MIN_DISTANCE_TO_UPDATE, this);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_TO_UPDATE, MIN_DISTANCE_TO_UPDATE, this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void stopLocationUpdate() {
        if(ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        FirebaseDatabase.getInstance().getReference().child("user").child(userID).child("coordinates").setValue(location.getLatitude() + "|" + location.getLongitude());

        System.out.println("StaffLocationService - User location: " + location.getLatitude() + "|" + location.getLongitude());
        Toast.makeText(context, location.getLatitude() + "|" + location.getLongitude(), Toast.LENGTH_LONG).show();
    }

    public boolean isLocationPermissionGranted() { return isLocationPermissionGranted; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override
    public void onProviderEnabled(String provider) {}
    @Override
    public void onProviderDisabled(String provider) {}

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }
}
