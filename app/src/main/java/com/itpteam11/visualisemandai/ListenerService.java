package com.itpteam11.visualisemandai;

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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ListenerService extends WearableListenerService implements ConnectionCallbacks, OnConnectionFailedListener {
    private final String TAG = ListenerService.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;
    private String userID;

    public ListenerService() {}

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

        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
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

    /*@Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.v(TAG, "msg");
        String[] parts = messageEvent.getPath().split("--");
        if(parts[0].equals("TEST")){
            Log.v(TAG, parts[0] + parts[1]);
            userID = parts[1];
        }
        else {
            DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("user");
            db.child(userID).child("status").setValue(messageEvent.getPath());
           // showToast(messageEvent.getPath());
        }
    }

//    private void showToast(String message) {
//        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
//        //Update database
//    }*/

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.v(TAG, "onMessageReceived");

        //Split string to get message type
        final String[] parts = messageEvent.getPath().split(";");

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
                    String coordinates = null;
                    if(StaffLocationService.isLocationPermissionGranted()) {
                        coordinates = StaffLocationService.getLatitude() + "-" + StaffLocationService.getLongitude();
                    }

                    //Create a notification with necessary information to notify staff who is not on off
                    Notification notification = new Notification();
                    notification.sendNotification(parts[1] + " has ESCAPE! Do take a look out for it. Ensure your and visitor's safety.", coordinates, userID, receiver);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to get working staff list
                    System.out.println("Failed to get working staff list: " + error.toException());
                }
            });
        }

        //Do action according to message type
        if(parts[0].equals("Shows")){
            //Shows;showname;showstatus;reason
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
                    String coordinates = null;
                    if(StaffLocationService.isLocationPermissionGranted()) {
                        coordinates = StaffLocationService.getLatitude() + "-" + StaffLocationService.getLongitude();
                    }
                    if(parts[2].equals("Full")) {
                        //Create a notification with necessary information to notify staff who is not on off
                        Notification notification = new Notification();
                        notification.sendNotification(parts[1] + " is currently full.", coordinates, userID, receiver);
                    }
                    else if(parts[2].equals("Delay")){
                        Notification notification = new Notification();
                        notification.sendNotification(parts[1] + " is delayed for"+parts[3]+ ".", coordinates, userID, receiver);
                    }
                    else{ //Cancel
                        Notification notification = new Notification();
                        notification.sendNotification(parts[1] + " is canceled due to"+parts[3]+".", coordinates, userID, receiver);
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