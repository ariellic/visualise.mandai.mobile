package com.itpteam11.visualisemandai;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;


/**
 *
 */
public class StaffInfoFragment extends Fragment {
    private RecyclerView recyclerView;
    private CustomCardAdapter customCardAdapter;
    private RecyclerView.LayoutManager recyclerLayoutManager;

    private String userID;
    private User user;

    private HashMap<Integer, String> cardDataSet;

    public StaffInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Retrieve value pass from activity
        Bundle bundle = getArguments();
        userID = bundle.getString("userID");
        user = bundle.getParcelable("user");

        //Create and add neccessary cards
        cardDataSet = new HashMap<>();

        //Todo: Get value from Firebase
        cardDataSet.put(CardType.WEATHER, "#-#-#");

        //Get users' status
        FirebaseDatabase.getInstance().getReference().child("user").child(userID).child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                cardDataSet.put(CardType.STAFF_STATUS, dataSnapshot.getValue(String.class));

                customCardAdapter = new CustomCardAdapter(cardDataSet, userID);
                recyclerView.setAdapter(customCardAdapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                //Failed on staff count value
                System.out.println("StaffInfoFragment - Failed on staff status value: " + error.toException());
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_staff_info, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.staff_info_fragment_recyclerView);
        recyclerLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(recyclerLayoutManager);

        customCardAdapter = new CustomCardAdapter(cardDataSet, userID);
        recyclerView.setAdapter(customCardAdapter);

        return view;
    }
}
