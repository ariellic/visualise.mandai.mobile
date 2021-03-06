package com.itpteam11.visualisemandai;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This main activity which consist of necessary fragments for
 * the user to get their information and interact with the application
 */

public class MainActivity extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener {
    private static final String TAG = "MainActivity";

    //Constants for granting permission on location service
    private final static int LOCATION_PERMISSIONS_REQUEST = 1;
    private final static String MANAGER_ID = "SUk69wtTSbSTLUSQj5CavCJUyop1";

    //Value and child event listener list stores all created listeners as a record to be removed when app is destroy
    public static HashMap<DatabaseReference, ValueEventListener> valueEventListenerList = new HashMap<DatabaseReference, ValueEventListener>();
    public static HashMap<DatabaseReference, ChildEventListener> childEventListenerList = new HashMap<DatabaseReference, ChildEventListener>();

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
    private static StaffLocationService staffLocationService = null;

    //To communicate with smartwatch
    private static Intent listenerService = null;

    ////To provide information on  weather, temperature and PSI
    private static Intent climateService = null;

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

        //Check app permission to access location service
        checkLocationPermission();

        //Remove any previous created value event listener
        if(valueEventListenerList.size() != 0) {
            for(Map.Entry<DatabaseReference, ValueEventListener> entry : valueEventListenerList.entrySet()) {
                entry.getKey().removeEventListener(entry.getValue());
                Log.v(TAG, "Remove value event listener: " + entry.getKey());
            }
        }

        //Remove any previous created child event listener
        if(childEventListenerList.size() != 0) {
            for(Map.Entry<DatabaseReference, ChildEventListener> entry : childEventListenerList.entrySet()) {
                entry.getKey().removeEventListener(entry.getValue());
                Log.v(TAG, "Remove child event listener: " + entry.getKey());
            }
        }

        //Start service to listen for smartwatch
        if(listenerService == null) {
            listenerService = new Intent(this, ListenerService.class);
            listenerService.putExtra(ListenerService.USER_ID, userID);
            startService(listenerService);
        }

        //Retrieve user's detail from database with authenticated user ID
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        database.child("user").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Store user's details into User object
                user = dataSnapshot.getValue(User.class);

                if (user.getType().equals("master")) {
                    Intent specialPoerActivity = new Intent(MainActivity.this, SpecialPowerActivity.class);
                    specialPoerActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(specialPoerActivity);
                } else {
                    if (user.getAccount_Status().equals("enable")) {
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
                        for (String groupName : user.getGroup().keySet()) {
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
                                                if(climateService == null) {
                                                    climateService = new Intent(MainActivity.this, CheckClimateService.class);
                                                    climateService.putExtra(CheckClimateService.USER_ID, userID);
                                                    startService(climateService);
                                                }
                                                Log.d("ClimateService", "Service started");
                                            } catch (Exception e) {
                                                e.printStackTrace();
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
                    } else {
                        //Show dialog for disable user
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                        alertDialog.setTitle("Account Disabled");
                        alertDialog.setMessage("Oops, your account has been disabled. Please contact your supervisor for assistance.");
                        alertDialog.setIcon(android.R.drawable.ic_secure);
                        alertDialog.setCancelable(false);

                        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //Sign out from Firebase Authentication account
                                FirebaseAuth.getInstance().signOut();

                                //Back to sign in activity
                                Intent signInActivity = new Intent(MainActivity.this, SignInActivity.class);
                                signInActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(signInActivity);
                            }
                        });

                        alertDialog.show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to get user's detail
                Log.v(TAG, "Failed to get user's detail: " + error.toException());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_actions, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sign_out:

                //Show dialog for signing out confirmation
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("Sign Out");
                alertDialog.setMessage("Are you sure you want to sign out?");
                alertDialog.setIcon(android.R.drawable.ic_dialog_alert);

                //"Yes" Button
                alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        signOut();

                        //Back to sign in activity
                        Intent signInActivity = new Intent(MainActivity.this, SignInActivity.class);
                        signInActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(signInActivity);
                    }
                });

                //"NO" Button
                alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // No action
                    }
                });

                alertDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //To check whether user has granted permission to access location service for this app
    private void checkLocationPermission() {
        //If location permission is not granted
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Display toast if user denied permission
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                //Show dialog if user denied permission
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle("Location Permission");
                alertDialog.setMessage("Please turn on phone and grant app Location permission.");
                alertDialog.setIcon(android.R.drawable.ic_dialog_map);
                alertDialog.setPositiveButton("OK", null);
                alertDialog.show();

                //Indicate at Firebase that user disabled Location
                FirebaseDatabase.getInstance().getReference().child("user").child(userID).child("latitude").setValue(0);
                FirebaseDatabase.getInstance().getReference().child("user").child(userID).child("longitude").setValue(0);
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
                    if(staffLocationService == null) {
                        //Start staff location service to update user coordinates
                        staffLocationService = new StaffLocationService(this, userID);
                    }

                    Log.v(TAG, "Location permission granted");
                }
                else {
                    //Show dialog if user denied permission
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                    alertDialog.setTitle("Location Permission");
                    alertDialog.setMessage("Please turn on phone and grant app Location permission.");
                    alertDialog.setIcon(android.R.drawable.ic_dialog_map);
                    alertDialog.setPositiveButton("OK", null);
                    alertDialog.show();

                    //Indicate at Firebase that user disabled Location
                    FirebaseDatabase.getInstance().getReference().child("user").child(userID).child("latitude").setValue(0);
                    FirebaseDatabase.getInstance().getReference().child("user").child(userID).child("longitude").setValue(0);
                }
            }
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.v(TAG, "onConnectionSuspended called");
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) { Log.v(TAG, "onConnectionFailed called"); }
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

    private void signOut() {
        //Set user status as "off" duty
        FirebaseDatabase.getInstance().getReference().child("user").child(userID).child("status").setValue("off");
        for(String groupName : user.getGroup().keySet()) {
            FirebaseDatabase.getInstance().getReference().child("group").child(groupName).child(user.getGroup().get(groupName)).child(userID).setValue("off");
        }

        //Stop user location update
        if(staffLocationService != null) {
            staffLocationService.stopLocationUpdate();
            staffLocationService = null;
        }

        //Stop climate service
        if(backtask != null) {
            backtask.cancel();
        }

        if(climateService != null) {
            stopService(climateService);
            climateService = null;
        }

        //Stop listener service for wear
        if(listenerService != null) {
            stopService(listenerService);
            listenerService = null;
        }

        //Remove all value event listener
        if(valueEventListenerList.size() != 0) {
            for(Map.Entry<DatabaseReference, ValueEventListener> entry : valueEventListenerList.entrySet()) {
                entry.getKey().removeEventListener(entry.getValue());
                Log.v(TAG, "Remove value event listener: " + entry.getKey());
            }
        }

        //Remove all child event listener
        if(childEventListenerList.size() != 0) {
            for(Map.Entry<DatabaseReference, ChildEventListener> entry : childEventListenerList.entrySet()) {
                entry.getKey().removeEventListener(entry.getValue());
                Log.v(TAG, "Remove child event listener: " + entry.getKey());
            }
        }

        FirebaseDatabase.getInstance().getReference().child("user").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                if(user.getStatus().equals("off")){
                    //Sign out from Firebase Authentication account
                    FirebaseAuth.getInstance().signOut();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    @Override
    public void onBackPressed() {
        //Show dialog for signing out confirmation
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Sign Out");
        alertDialog.setMessage("Are you sure you want to sign out?");
        alertDialog.setIcon(android.R.drawable.ic_dialog_alert);

        //"Yes" Button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                signOut();
                finish();
            }
        });

        //"NO" Button
        alertDialog.setNegativeButton("NO", null);

        alertDialog.show();
    }
}
