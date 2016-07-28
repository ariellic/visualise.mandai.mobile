package com.itpteam11.visualisemandai;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
/**
 * This ListernService is to retrieve data pass from the paired watch
 * and pass the data to Firebase.
 */
public class ListenerService extends WearableListenerService implements ConnectionCallbacks, OnConnectionFailedListener {
    private final String TAG = ListenerService.class.getSimpleName();
    public final static String USER_ID = "userID";

    private GoogleApiClient mGoogleApiClient;
    private String userID;
    private String groupID;

    public ListenerService() {}

    @Override
    public int onStartCommand (Intent intent, int flags, int startId){
        if(intent != null) {
            userID = intent.getStringExtra(USER_ID);
        }
        else {
            userID = "No User ID in ListenerService at this timestamp: " + new Date().getTime();
        }
        System.out.println("ListenerService - userID: " + userID);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "Created");

        if(null == mGoogleApiClient) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            Log.v(TAG, "Listener GoogleApiClient created");
        }

        if(!mGoogleApiClient.isConnected()){
            mGoogleApiClient.connect();
            Log.v(TAG, "Connecting to GoogleApiClient..");
        }
    }
    @Override
    public void onDestroy() {
        Log.v(TAG, "Destroyed");

        if(null != mGoogleApiClient){
            if(mGoogleApiClient.isConnected()){
                mGoogleApiClient.disconnect();
                Log.v(TAG, "GoogleApiClient disconnected");
            }
        }

        super.onDestroy();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.v(TAG,"onConnectionSuspended called");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.v(TAG,"onConnectionFailed called");
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.v(TAG,"onConnected called");
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);
        Log.v(TAG, "Data Changed");
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.v(TAG, "onMessageReceived");

        //Split string to get message type
        final String[] parts = messageEvent.getPath().split(";");
        if(parts[0].equals("GROUP")){
            groupID = parts[1];
            Log.v(TAG,"the group is "+ groupID);
        }
        //Do action according to message type
        if(parts[0].equals("escape")){
            //Get the list of current staff who is not on off
            FirebaseDatabase.getInstance().getReference().child("user").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Map<String, Boolean> receiver = new HashMap<String, Boolean>();

                    //Populate the list with staff who is not on off
                    for(DataSnapshot child : dataSnapshot.getChildren()) {
                        if(!child.child("status").getValue(String.class).equals("off")) {
                            receiver.put(child.getKey(), false);
                        }
                    }

                    //Get user's coordinates to indicate where the animal has escaped
                    Double latitude = null;
                    Double longitude = null;
                    if (StaffLocationService.isLocationPermissionGranted()) {
                        latitude = StaffLocationService.getLatitude();
                        longitude = StaffLocationService.getLongitude();
                    }

                    //Create a notification with necessary information to notify staff who is not on off
                    Notification notification = new Notification();
                    notification.sendNotification(Notification.ESCAPE_NOTIFICATION, parts[1] + " has ESCAPE! Do take a look out for it. Ensure your and visitor's safety.", latitude, longitude, userID, receiver, null);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to get working staff list
                    System.out.println("Failed to get working staff list: " + error.toException());
                }
            });
        }
        // Status
        else if(parts[0].equals("status")){

            if(parts[1].contains("Break")){
                FirebaseDatabase.getInstance().getReference().child("user").child(userID).child("status").setValue("break");
                FirebaseDatabase.getInstance().getReference().child("group").child(groupID).child("staff").child(userID).setValue("break");

            }
            else if(parts[1].equals("Back to Work")){
                FirebaseDatabase.getInstance().getReference().child("user").child(userID).child("status").setValue("working");
                FirebaseDatabase.getInstance().getReference().child("group").child(groupID).child("staff").child(userID).setValue("working");
            }
            else{ //off work
                FirebaseDatabase.getInstance().getReference().child("user").child(userID).child("status").setValue("off");
                FirebaseDatabase.getInstance().getReference().child("group").child(groupID).child("staff").child(userID).setValue("off");

            }
        }
        //Do action according to message type (Shows;show name;show status;reason)
        else if(parts[0].equals("Shows")){

            //Get the list of current staff who is not on off
            FirebaseDatabase.getInstance().getReference().child("user").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Map<String, Boolean> receiver = new HashMap<String, Boolean>();

                    //Populate the list with staff who is not on off
                    for(DataSnapshot child : dataSnapshot.getChildren()) {
                        if(!child.child("status").getValue(String.class).equals("off")) {
                            receiver.put(child.getKey(), false);
                        }
                    }

                    //Get user's coordinates
                    Double latitude = null;
                    Double longitude = null;
                    if (StaffLocationService.isLocationPermissionGranted()) {
                        latitude = StaffLocationService.getLatitude();
                        longitude = StaffLocationService.getLongitude();
                    }

                    if(parts[2].equals("Full")) {
                        //Create a notification with necessary information to notify staff who is not on off
                        Notification notification = new Notification();
                        notification.sendNotification(Notification.NORMAL_NOTIFICATION, parts[1] + " is currently full.", latitude, longitude, userID, receiver, null);
                    }
                    else if(parts[2].equals("Delay")){
                        Notification notification = new Notification();
                        notification.sendNotification(Notification.NORMAL_NOTIFICATION, parts[1] + " is delayed for "+parts[3]+ ".", latitude, longitude, userID, receiver, null);
                    }
                    else{ //Cancel
                        Notification notification = new Notification();
                        notification.sendNotification(Notification.NORMAL_NOTIFICATION, parts[1] + " is canceled due to "+parts[3]+".", latitude, longitude, userID, receiver, null);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to get working staff list
                    System.out.println("Failed to get working staff list: " + error.toException());
                }
            });
        }
        else if (parts[0].equals("Tram")) {
            //Get the list of current staff who is not on off
            FirebaseDatabase.getInstance().getReference().child("user").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Map<String, Boolean> receiver = new HashMap<String, Boolean>();

                    //Populate the list with staff who is not on off
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        if (!child.child("status").getValue(String.class).equals("off")) {
                            receiver.put(child.getKey(), false);
                        }
                    }

                    //Get user's coordinates
                    Double latitude = null;
                    Double longitude = null;
                    if (StaffLocationService.isLocationPermissionGranted()) {
                        latitude = StaffLocationService.getLatitude();
                        longitude = StaffLocationService.getLongitude();
                    }

                    if(parts[2].equals("Crowded")) {
                        Notification notification = new Notification();
                        notification.sendNotification(Notification.NORMAL_NOTIFICATION, "Tram station " + parts[1] + " is currently very crowded now. More trams are needed.", latitude, longitude, userID, receiver, null);

                        FirebaseDatabase.getInstance().getReference().child("service").child("tram").child("station" + parts[1]).setValue("Crowded");
                    }
                    else if(parts[2].equals("Normal")) {
                        Notification notification = new Notification();
                        notification.sendNotification(Notification.NORMAL_NOTIFICATION, "Tram station " + parts[1] + " is currently normal now.", latitude, longitude, userID, receiver, null);

                        FirebaseDatabase.getInstance().getReference().child("service").child("tram").child("station" + parts[1]).setValue("Normal");
                    } 
                 }
                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to get working staff list
                    System.out.println("Failed to get working staff list: " + error.toException());
                }
            });
        }
    }
}