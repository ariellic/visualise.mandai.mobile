package com.itpteam11.visualisemandai;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * This main activity which consist of necessary fragments for
 * the user to get their information and interact with the application
 */

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private int[] tabIcons = {
            R.drawable.info,
            R.drawable.notification};

    private User user;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get authenticated user ID from sign in activity
        Intent intent = getIntent();
        userID = intent.getStringExtra("userID");

        System.out.println("MainActivity - User ID from Intent mainActivity: " + userID);
        System.out.println("MainActivity - Firebase user ID: " + FirebaseAuth.getInstance().getCurrentUser().getUid());

        //Retrieve user's detail from database with authenticated user ID
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        database.child("user").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Store user's details into User object
                user = dataSnapshot.getValue(User.class);

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

    private void setupViewPager(ViewPager viewPager, String userType)
    {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        if(userType.equals("manager")) {
            //Create Bundle to pass value from MainActivity to fragment
            Bundle bundle = new Bundle();
            bundle.putString("userID", userID);

            ManagerInfoFragment managerInfoFragment = new ManagerInfoFragment();
            managerInfoFragment.setArguments(bundle);

            adapter.addFragment(managerInfoFragment, "Info");
        }
        else {
            adapter.addFragment(new InfoFragment(), "Info");
        }

        adapter.addFragment(new NotificationFragment(), "Notification");
        viewPager.setAdapter(adapter);
    }

    private void setupTabIcons()
    {
        for(int i=0; i<tabIcons.length; i++)
            tabLayout.getTabAt(i).setIcon(tabIcons[i]);
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
