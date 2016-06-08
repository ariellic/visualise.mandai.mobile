package com.itpteam11.visualisemandai;

import android.content.Intent;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private int[] tabIcons = {
            R.drawable.info,
            R.drawable.notification};

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        final String uID = intent.getStringExtra("uID");

        System.out.println("uID mainActivity: " + uID);
        System.out.println("Firebase UID mainActivity: " + FirebaseAuth.getInstance().getCurrentUser().getUid());

        //Get user's detail
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        database.child("user").child(uID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);


                toolbar = (Toolbar) findViewById(R.id.main_activity_toolbar);
                setSupportActionBar(toolbar);

                getSupportActionBar().setDisplayHomeAsUpEnabled(false);

                viewPager = (ViewPager) findViewById(R.id.main_activity_viewpager);
                setupViewPager(viewPager, user.getType());

                tabLayout = (TabLayout) findViewById(R.id.main_activity_tabs);
                tabLayout.setupWithViewPager(viewPager);

                setupTabIcons();

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
    public User getUser(){ return user; }

    private void setupViewPager(ViewPager viewPager, String userType)
    {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        if(userType.equals("manager")) {
            adapter.addFragment(new ManagerInfoFragment(), "Info");
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
