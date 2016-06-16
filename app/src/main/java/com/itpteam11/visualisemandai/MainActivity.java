package com.itpteam11.visualisemandai;

import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
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

/**
 * This main activity which consist of necessary fragments for
 * the user to get their information and interact with the application
 */

public class MainActivity extends AppCompatActivity  implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MainActivity";
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private int[] tabIcons = {
            R.drawable.info,
            R.drawable.notification};

    private User user;
    private String userID;
    private String userGrp;
    GoogleApiClient mGoogleApiClient;

    private String abbr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get authenticated user ID from sign in activity
        Intent intent = getIntent();
        userID = intent.getStringExtra("userID");
        abbr = "";
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
                for ( String key : user.getGroup().keySet() ) {
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
                setupTabIcons(user.getType());

                //Set title
                setTitle("Welcome " + user.getName());
                new SendActivityPhoneMessage("TEST--"+userID,"").start();
                getNeaDataset();
                trackDataChange("weather");
                trackDataChange("psi");

                //Start listening to user's subscribed service for notification of changes
                new ServiceSubscribeListener(userID, user.getService()).startListening();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to get user's detail
                System.out.println("Failed to get user's detail: " + error.toException());
            }
        });
    }

    //For fragment
    public String getUserID(){ return userID; }

    //Send userid to ListenerService

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

    // To check if the PSI/weather data from the database has changed
    private void trackDataChange(final String dataCat) {
        final DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference().child("service").child(dataCat).child("value");
        dataRef.keepSynced(true);
        dataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d("DATACHANGE", dataCat + " data changed: " + dataSnapshot.getValue().toString());
                    // To get the full weather description from the abbreviations retrieved
                    if (dataCat.equals("weather")) {
                        String weather = dataSnapshot.getValue().toString();
                        if (isAbbr(weather)) {
                            abbr = weather;
                            getAbbrTranslatedWeather(weather);
                        }
                        // If the data has been changed to weather description
                        else {
                            Log.d("DATACHANGE", "Weather description: " + weather);
                            String[] rainyWeather = new String[]{"DR", "HG", "HR", "HS", "HT", "LR", "LS", "PS", "RA", "SH", "SK", "SR", "TL", "WR", "WS"};
                            List rainyAbbrList = Arrays.asList(rainyWeather);
                            if (rainyAbbrList.contains(abbr)) {
                                Log.d("NOTIFY", "Notify rainy");
                                notifyRain(weather, dataRef);
                            } else if (abbr.equals("SU")) {
                                Log.d("NOTIFY", "Notify sunny");
                                notifySunny();
                            }
                        }
                    } else if (dataCat.equals("psi")) {
                        int psi = Integer.parseInt(dataSnapshot.getValue().toString());
                        if (inUnhealthyRange(psi)) {
                            notifyPsi(psi, "Unhealthy");
                        } else if (inVeryUnhealthyRange(psi)) {
                            notifyPsi(psi, "Very unhealthy");
                        } else if (inHazardousRange(psi)) {
                            notifyPsi(psi, "Hazardous");
                        }
                    }
                } else {
                    Log.d("DATACHANGE", "Data not found");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void setNotification(String title, String intro, String desc) {
        Log.d("NOTIFY", "In setNotification method");
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(MainActivity.this)
                        .setSmallIcon(android.R.drawable.ic_popup_reminder)
                        .setContentTitle(title)
                        .setContentText(intro)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(desc));

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(MainActivity.this);
        notificationManager.notify(0, mBuilder.build());
    }

    private void notifyRain(String weather, DatabaseReference ref){
        String title = "Weather alert: " + weather;
        String intro = "It's going to rain!";
        String desc = "Hi " + user.getName() + "! It's going to rain soon, do advise the visitors to stay sheltered and do the same for yourself too!";
        setNotification(title, intro, desc);

        Map<String, Object> timeTitle = new HashMap<>();
        timeTitle.put(new Date().getTime() + "/title", title);

        // Limiting the number of notifications to be shown to 5
        final DatabaseReference notiRef = FirebaseDatabase.getInstance().getReference().child("service").child("weather").child("notifications");
        notiRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount()>5){
                    long num = dataSnapshot.getChildrenCount()-5;
                    Query deleteQuery = notiRef.limitToFirst((int) num);
                    deleteQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot postSnapshot: dataSnapshot.getChildren()){
                                postSnapshot.getRef().removeValue();
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        notiRef.updateChildren(timeTitle);
    }

    private void notifySunny(){
        setNotification("Weather alert: Sunny", "The sun is smiling at us!", "Hi " + user.getName() + "! Do drink more water as the weather is getting warmer.");
    }

    private void notifyPsi(int psi, String descriptor){
        setNotification("Haze alert: " + descriptor, "PSI is at " + psi, user.getName() + ", PSI is at " + psi + " now! Do wear a mask wherever you are outdoors and do alert the visitors to wear one too.");
    }

    private boolean inUnhealthyRange(int psi){
        return (psi>=101 && psi<=200);
    }

    private boolean inVeryUnhealthyRange(int psi){
        return (psi>=201 && psi<=300);
    }

    private boolean inHazardousRange(int psi){
        return (psi>300);
    }
    // To get the full weather description from the abbreviation
    private void getAbbrTranslatedWeather(String weatherData){

        final DatabaseReference weatherRef = FirebaseDatabase.getInstance().getReference().child("service").child("weather").child("value");
        DatabaseReference abbrRef = FirebaseDatabase.getInstance().getReference().child("service").child("weather").child("abbreviations").child(weatherData);
        abbrRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot abbrDataSnapshot) {
                if (abbrDataSnapshot.exists()){
                    String abbrTranslated = abbrDataSnapshot.getValue().toString();
                    Log.d("DATACHANGE", "Abbreviation translate to " + abbrTranslated);
                    weatherRef.setValue(abbrTranslated); // Change abbreviation value in DB to the full weather description
                }
                else {
                    Log.d("DATACHANGE", "No such abbreviation exists");
                }
            }
            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Log.d("DATACHANGE", "onCancelled");
            }
        });
    }

    // Check if data in DB is an abbreviation or an already translated description
    private boolean isAbbr(String weatherData) {
        return (weatherData.length() == 2);
    }

    // Execute the asynchronous task to retrieve the weather and PSI data from NEA website
    private void getNeaDataset() {
        final String weatherURL = "http://www.nea.gov.sg/api/WebAPI/?dataset=2hr_nowcast&keyref=781CF461BB6606ADEA01E0CAF8B35274D184749DB043FB09";
        final String psiURL = "http://www.nea.gov.sg/api/WebAPI/?dataset=psi_update&keyref=781CF461BB6606ADEA01E0CAF8B35274D184749DB043FB09";
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
                            Log.d("NEA Data", "Getting dataset");
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                        }
                    }
                });
            }
        };
        Log.d("NEA Data", "Dataset retrieved");
        timer.schedule(backtask , 0, 60000); //execute in every 20000 ms*/
    }

    private void setupViewPager(ViewPager viewPager, String userType)
    {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        if(userType.equals("manager")) {
            //Create Bundle to pass value from MainActivity to information fragment
            Bundle infoBundle = new Bundle();
            infoBundle.putString("userID", userID);
            infoBundle.putString("group",userGrp);

            ManagerInfoFragment managerInfoFragment = new ManagerInfoFragment();
            managerInfoFragment.setArguments(infoBundle);

            adapter.addFragment(managerInfoFragment, "Info");
        }
        else {
            //adapter.addFragment(new InfoFragment(), "Info");
        }

        //Create Bundle to pass value from MainActivity to notification fragment
        Bundle notificationBundle = new Bundle();
        notificationBundle.putString("userID", userID);

        NotificationFragment notificationFragment = new NotificationFragment();
        notificationFragment.setArguments(notificationBundle);

        adapter.addFragment(notificationFragment, "Notification");
        viewPager.setAdapter(adapter);
    }

    //Add icons to respective tabs
    private void setupTabIcons(String userType)
    {
        int j = 0;

        for(int i=0; i<tabIcons.length; i++){
            if(!userType.equals("manager")){
                tabLayout.getTabAt(j).setIcon(tabIcons[i]);
            }
            else {
                tabLayout.getTabAt(j).setIcon(tabIcons[i]);
                j++;
            }
        }
    }

    class ViewPagerAdapter extends FragmentPagerAdapter
    {
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

        //Sign out from Firebase Authentication account
        FirebaseAuth.getInstance().signOut();
    }
}
