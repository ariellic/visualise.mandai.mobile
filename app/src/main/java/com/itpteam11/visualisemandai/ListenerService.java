package com.itpteam11.visualisemandai;


import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ListenerService extends WearableListenerService implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    GoogleApiClient mGoogleApiClient;
    private final String TAG = ListenerService.class.getSimpleName();
    String  userID;
public ListenerService() {

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
//    }
}