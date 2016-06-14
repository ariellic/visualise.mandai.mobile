package com.itpteam11.visualisemandai;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;


import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 */

public class ManagerInfoFragment extends Fragment {
    private RecyclerView recyclerView;
    private CustomCardAdapter customCardAdapter;
    private RecyclerView.LayoutManager recyclerLayoutManager;

    private HashMap<Integer, String> cardDataSet;
    ArrayList<String> userList = new ArrayList<String>();
    ArrayList<String> workingList = new ArrayList<String>();
    ArrayList<String> breakList = new ArrayList<String>();
     
    private String userID;
    private String userGroup;


    public ManagerInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        userID = bundle.getString("userID");
        userGroup = bundle.getString("group");

        cardDataSet = new HashMap<Integer, String>();
        cardDataSet.put(CardType.CHECK_SHOWTIME, "");

        //Get list of users in the group under Manager
        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("group").child(userGroup).child("user");
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot sp : dataSnapshot.getChildren()) {
                    userList.add(sp.getKey());
                }
            }


            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                System.out.println(error.toException());
            }
        });
    //Get the group users' status
        DatabaseReference db1 = FirebaseDatabase.getInstance().getReference();
        db1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                workingList.clear();
                breakList.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.child("user").getChildren()) {
                    String userID = postSnapshot.getKey();
                    if (userList.contains(userID)) {
                        String status = postSnapshot.child("status").getValue(String.class);
                        Log.e("Status", status);
                        if (status.equals("working")) {
                            if (!workingList.contains(userID)) {
                                workingList.add(userID);
                            }
                        } else {
                            breakList.add(userID);
                        }
                    }
                }

                String grpNum = String.valueOf(workingList.size());
                String breakNum = String.valueOf(breakList.size());
                cardDataSet.put(CardType.STAFF_WORKING, grpNum);
                cardDataSet.put(CardType.STAFF_BREAK,breakNum);

                customCardAdapter = new CustomCardAdapter(cardDataSet, userID);
                recyclerView.setAdapter(customCardAdapter);
            }


            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                System.out.println(error.toException());
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_manager_info, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.manager_info_fragment_recyclerView);
        recyclerLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(recyclerLayoutManager);

        customCardAdapter = new CustomCardAdapter(cardDataSet, userID);
        recyclerView.setAdapter(customCardAdapter);

        return view;
    }
     @Override
    public void onStart(){
        super.onStart();



    }
}
