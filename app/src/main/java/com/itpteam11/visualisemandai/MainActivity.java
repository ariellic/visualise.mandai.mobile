package com.itpteam11.visualisemandai;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.jar.*;

/**
 * This main activity which consist of necessary fragments for
 * the user to get their information and interact with the application
 */

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    //Constants for granting permission on location service
    private final static int LOCATION_PERMISSIONS_REQUEST = 1;
    private final static String MANAGER_ID = "SUk69wtTSbSTLUSQj5CavCJUyop1";

    private static final String TAG = "MainActivity";

    //Tab UI widgets
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private int[] tabIcons = {
            R.drawable.info,
            R.drawable.notification,
            R.drawable.send};

    //Value about the user to be pass to fragments
    private User user;
    private String userID;
    private String userGrp;
    private String[] userGroupList;

    TimerTask backtask = null;

    GoogleApiClient mGoogleApiClient;

    //To locate staff coordinates
    private StaffLocationService staffLocationService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get authenticated user ID from sign in activity
        Intent intent = getIntent();
        userID = intent.getStringExtra("userID");
        if(null == mGoogleApiClient) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            Log.v(TAG, "GoogleApiClient created");
        }

        if(!mGoogleApiClient.isConnected()){
            mGoogleApiClient.connect();
            Log.v(TAG, "Connecting to GoogleApiClient..");
        }

        final Intent climateServiceIntent = new Intent(this, CheckClimateService.class);
        startService(new Intent(this, ListenerService.class));
        System.out.println("MainActivity - User ID from Intent mainActivity: " + userID);
        System.out.println("MainActivity - Firebase user ID: " + FirebaseAuth.getInstance().getCurrentUser().getUid());

        //Retrieve user's detail from database with authenticated user ID
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        database.child("user").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Store user's details into User object
                user = dataSnapshot.getValue(User.class);
                userGroupList = user.getGroup().keySet().toArray(new String[user.getGroup().keySet().size()]);

                for (String key : user.getGroup().keySet()) {
                    userGrp = key;
                }
                //Setup Action bar
                toolbar = (Toolbar) findViewById(R.id.main_activity_toolbar);
                setSupportActionBar(toolbar);

                getSupportActionBar().setDisplayHomeAsUpEnabled(false);

                //Setup page viewer for fragments
                viewPager = (ViewPager) findViewById(R.id.main_activity_viewpager);
                setupViewPager(viewPager, user.getType());

                //Setup tabs
                tabLayout = (TabLayout) findViewById(R.id.main_activity_tabs);
                tabLayout.setupWithViewPager(viewPager);

                //Assign tabs with their respective icon
                setupTabIcons();

                //Set title
                setTitle("Welcome " + user.getName());

                //Set user status as "working"
                FirebaseDatabase.getInstance().getReference().child("user").child(userID).child("status").setValue("working");
                for(String groupName : user.getGroup().keySet()) {
                    FirebaseDatabase.getInstance().getReference().child("group").child(groupName).child(user.getGroup().get(groupName)).child(userID).setValue("working");
                }

                new SendActivityPhoneMessage("GROUP;" + userGrp, "").start();

                // To start the service to check the climate data every 20 seconds (can be changed)
                if (userID.equals(MANAGER_ID)) {
                    final Handler handler = new Handler();
                    Timer timer = new Timer();
                    backtask = new TimerTask() {
                        @Override
                        public void run() {
                            handler.post(new Runnable() {
                                public void run() {
                                    try {
                                        startService(climateServiceIntent);
                                        Log.d("ClimateService", "Service started");
                                    } catch (Exception e) {
                                        // TODO Auto-generated catch block
                                    }
                                }
                            });
                        }
                    };
                    Log.d("TimerTask", "TimerTask completed");
                    timer.schedule(backtask, 0, 20000); //execute in every 20000 ms*/
                }

                //Start listening to user's subscribed service for notification of changes
                new ServiceSubscribeListener(userID, user.getService()).startListening();

                //Check app permission to access location service
                checkLocationPermission();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to get user's detail
                System.out.println("Failed to get user's detail: " + error.toException());
            }
        });
    }

    //To check whether user has granted permission to access location service for this app
    private void checkLocationPermission() {
        //If location permission is not granted
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Display toast if user denied permission
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                Toast.makeText(this, "Please enable Location permission at:\nSettings>Apps>Visualise Mandai>Permissions>Location", Toast.LENGTH_LONG).show();
            }
            else {
                //Request user to grant permission for location service and callback to onRequestPermissionsResult()
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSIONS_REQUEST);
            }
        }
        else {
            //Callback to onRequestPermissionsResult() when user already granted permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSIONS_REQUEST);
        }
    }

    //Callback method from ActivityCompat.requestPermissions()
    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSIONS_REQUEST: {
                //If user granted permission to access location
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Start staff location service to update user coordinates
                    staffLocationService = new StaffLocationService(this, userID);
                    System.out.println("MainActivity - Location permission granted");
                }
                else {
                    //Display toast if user denied permission
                    Toast.makeText(this, "Please enable Location permission at:\n" +
                            "Settings>Apps>Visualise Mandai>Permissions>Location", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.v(TAG, "onConnectionSuspended called");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.v(TAG, "onConnectionFailed called");
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.v(TAG, "onConnected called");
    }
    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG, "onStart called");
    }

    class SendActivityPhoneMessage extends Thread {
        String path;
        String message;

        // Constructor to send a message to the data layer
        SendActivityPhoneMessage(String p, String msg) {
            path = p;
            message = msg;
        }

        public void run() {
            NodeApi.GetLocalNodeResult nodes = Wearable.NodeApi.getLocalNode(mGoogleApiClient).await();
            Node node = nodes.getNode();
            Log.v(TAG, "Activity Node is : " + node.getId() + " - " + node.getDisplayName());
            MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), path, message.getBytes()).await();
            if (result.getStatus().isSuccess()) {
                Log.v(TAG, "Activity Message: {" + path + "} sent to: " + node.getDisplayName());
            } else {
                // Log an error
                Log.v(TAG, "ERROR: failed to send Activity Message");
            }

        }
    }

    // Execute the asynchronous task to retrieve the weather and PSI data from NEA website
    private void getNeaDataset() {
        final String weatherURL = "http://www.nea.gov.sg/api/WebAPI/?dataset=2hr_nowcast&keyref=781CF461BB6606ADEA01E0CAF8B35274D184749DB043FB09";
        final String psiURL = "http://www.nea.gov.sg/api/WebAPI/?dataset=psi_update&keyref=781CF461BB6606ADEA01E0CAF8B35274D184749DB043FB09";
        final String tempURL = "http://api.openweathermap.org/data/2.5/weather?lat=1.404043&lon=103.793045&appid=179acd6a18cfec63680175ff28ffdb06&mode=xml";
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask backtask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            new NeaDatasetAsyncTask().execute(weatherURL);
                            new NeaDatasetAsyncTask().execute(psiURL);
                            new NeaDatasetAsyncTask().execute(tempURL);
                            Log.d("NEA Data", "Getting dataset");
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                        }
                    }
                });
            }
        };
        Log.d("NEA Data", "Dataset retrieved");
        timer.schedule(backtask, 0, 60000); //execute in every 20000 ms*/
    }

    //This method setup the necessary tabs depending on user type
    private void setupViewPager(ViewPager viewPager, String userType)
    {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        //Create Bundle to pass value from MainActivity to information fragment
        Bundle infoBundle = new Bundle();
        infoBundle.putString("userID", userID);
        infoBundle.putString("group",userGrp);
        infoBundle.putParcelable("user", user);
        infoBundle.putCharSequenceArray("groupList",userGroupList);

        //Information tab
        if(userType.equals("manager")) {
            ManagerInfoFragment managerInfoFragment = new ManagerInfoFragment();
            managerInfoFragment.setArguments(infoBundle);

            adapter.addFragment(managerInfoFragment, "Info");
        }
        else {
            StaffInfoFragment staffInfoFragment = new StaffInfoFragment();
            staffInfoFragment.setArguments(infoBundle);

            adapter.addFragment(staffInfoFragment, "Info");
        }

        //Create Bundle to pass value from MainActivity to notification fragment
        Bundle notificationBundle = new Bundle();
        notificationBundle.putString("userID", userID);

        //Notification tab
        NotificationFragment notificationFragment = new NotificationFragment();
        notificationFragment.setArguments(notificationBundle);

        adapter.addFragment(notificationFragment, "Notification");

        //Send Notice tab
        if(userType.equals("manager")) {
            SendNotificationFragment sendNotificationFragment = new SendNotificationFragment();
            adapter.addFragment(sendNotificationFragment, "Send Notice");
        }

        viewPager.setAdapter(adapter);
    }

    //This method add icons to respective tabs
    private void setupTabIcons()
    {
        for(int i=0; i<tabLayout.getTabCount(); i++)
            tabLayout.getTabAt(i).setIcon(tabIcons[i]);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //Set user status as "off" duty
        FirebaseDatabase.getInstance().getReference().child("user").child(userID).child("status").setValue("off");
        for(String groupName : user.getGroup().keySet()) {
            FirebaseDatabase.getInstance().getReference().child("group").child(groupName).child(user.getGroup().get(groupName)).child(userID).setValue("off");
        }

        //Stop user location update
        if(staffLocationService != null)
            staffLocationService.stopLocationUpdate();

        //Stop climate service
        if(backtask != null) {
            backtask.cancel();
        }

        //Stop listener service for wear
        stopService(new Intent(this, ListenerService.class));

        //Sign out from Firebase Authentication account
        FirebaseAuth.getInstance().signOut();

    }
}
