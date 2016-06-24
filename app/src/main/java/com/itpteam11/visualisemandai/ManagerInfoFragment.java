package com.itpteam11.visualisemandai;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 *  This fragment contain RecyclerView which contain necessary cards for
 *  manager to get information and interact with the application
 */

public class ManagerInfoFragment extends Fragment {
    private RecyclerView recyclerView;
    private CustomCardAdapter customCardAdapter;
    private RecyclerView.LayoutManager recyclerLayoutManager;

    private HashMap<Integer, String> cardDataSet;
    ArrayList<String> userList = new ArrayList<>();
    ArrayList<String> workingList = new ArrayList<>();
    ArrayList<String> breakList = new ArrayList<>();
     
    private String userID, userGroup;
    private String[] userGroupList;

    public ManagerInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Retrieve value pass from activity
        Bundle bundle = getArguments();
        userID = bundle.getString("userID");
        userGroup = bundle.getString("group");
        userGroupList = (String[]) bundle.getCharSequenceArray("userGroup");

        //Create and add neccessary cards
        cardDataSet = new HashMap<>();
        //cardDataSet.put(CardType.CHECK_SHOWTIME, "");

        //Get the group users' status
        FirebaseDatabase.getInstance().getReference().child("group").child(userGroup).child("staff").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                workingList.clear();
                breakList.clear();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    if (postSnapshot.getValue(String.class).equals("working")) {
                        workingList.add(userID);
                    } else if (!postSnapshot.getValue(String.class).equals("off")){
                        breakList.add(userID);
                    }
                }

                cardDataSet.put(CardType.STAFF_STATUS, workingList.size()+"-"+breakList.size());

                customCardAdapter = new CustomCardAdapter(cardDataSet, userID);
                recyclerView.setAdapter(customCardAdapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                //Failed to staff count value
                System.out.println("ManagerInfoFragment - Failed to staff count value: " + error.toException());
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
