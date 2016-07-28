package com.itpteam11.visualisemandai;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    private User user;
    private String userID, userGroup;
    private String[] userGroupList;

    private String fNotiStatus = null;
    private String eNotiStatus = null;
    private String rNotiStatus = null;
    private String sNotiStatus = null;

    private String psi = null;
    private String temp = null;
    private String weather = null;

    private final String TAG = "MInfo";

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
        user = bundle.getParcelable("user");
        userGroupList = (String[]) bundle.getCharSequenceArray("userGroup");

        //Create and add neccessary cards
        cardDataSet = new HashMap<>();

        //Todo: Get value from Firebase
        cardDataSet.put(CardType.TRAM_STATION, "NA-NA-NA-NA");
        cardDataSet.put(CardType.SHOWTIME, "OK-OK-OK-OK");
        cardDataSet.put(CardType.WEATHER, "#-#-#");

        //Get the climate information
        ValueEventListener weatherServiceListener = FirebaseDatabase.getInstance().getReference().child("service").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Log.d("POSTSNAPSHOT", postSnapshot.toString());
                    if (postSnapshot.getKey().equals("psi")) {
                        psi = postSnapshot.child("value").getValue().toString();
                        if (psi.equals("")){
                            psi = "NA";
                        }
                    } else if (postSnapshot.getKey().equals("temperature")) {
                        temp = postSnapshot.child("value").getValue().toString();
                        if (temp.equals("")){
                            temp = "NA";
                        }
                    } else if (postSnapshot.getKey().equals("weather")) {
                        weather = postSnapshot.child("valueLong").getValue().toString();
                        if (weather.equals("")){
                            weather = "NA";
                        }
                    }
                }

                Log.d("TEMPSIWEATHER", temp + psi + weather);
                cardDataSet.put(CardType.WEATHER, weather + "-" + temp + "°C" + "-" + psi);
                customCardAdapter = new CustomCardAdapter(cardDataSet, userID);
                recyclerView.setAdapter(customCardAdapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println("ManagerInfoFragment - Failed on climate retrieval value: " + error.toException());
            }
        });

        //Add created listener into list
        MainActivity.valueEventListenerList.put(FirebaseDatabase.getInstance().getReference().child("service"), weatherServiceListener);

        //Get the group users' status
        ValueEventListener staffStatusListener = FirebaseDatabase.getInstance().getReference().child("group").child(userGroup).child("staff").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                workingList.clear();
                breakList.clear();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    if (postSnapshot.getValue(String.class).equals("working")) {
                        workingList.add(userID);
                    } else if (!postSnapshot.getValue(String.class).equals("off")) {
                        breakList.add(userID);
                    }
                }

                cardDataSet.put(CardType.STAFF_COUNT, workingList.size() + "-" + breakList.size());

                customCardAdapter = new CustomCardAdapter(cardDataSet, userID);
                recyclerView.setAdapter(customCardAdapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                //Failed on staff count value
                System.out.println("ManagerInfoFragment - Failed on staff count value: " + error.toException());
            }
        });

        //Add created listener into list
        MainActivity.valueEventListenerList.put(FirebaseDatabase.getInstance().getReference().child("group").child(userGroup).child("staff"), staffStatusListener);

        ValueEventListener showtimeStatusListener = FirebaseDatabase.getInstance().getReference().child("notification").addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
                Date current = new Date();
                String currentDate =   (new SimpleDateFormat("dd MMM yyyy").format(current));
                String currentDateTime = (new SimpleDateFormat("dd MMM yyyy HH:mm:ss").format(current));

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String notiDate = new SimpleDateFormat("dd MMM yyyy").format(new Date(postSnapshot.child("timestamp").getValue(Long.class)));
                    String notiDateTime = new SimpleDateFormat("dd MMM yyyy HH:mm:ss").format(new Date(postSnapshot.child("timestamp").getValue(Long.class)));

                    if (notiDate.equals(currentDate)) {
                            //11 am show
                        if (postSnapshot.child("content").getValue(String.class).contains("Friends")) {

                            try {
                                Date current1 = sdf.parse(currentDateTime);
                                Date notiTime = sdf.parse(notiDateTime);
                                Date cThreshold = sdf.parse(currentDate + " 11:30:00");
                                Date nThreshold = sdf.parse(currentDate + " 11:20:00");
                                Date nThreshold1 = sdf.parse(currentDate + " 16:30:00");
                                if(current1.compareTo(cThreshold)<0 ) {
                                    if(notiTime.compareTo(nThreshold)<0) {
                                        fNotiStatus = postSnapshot.child("content").getValue(String.class);
                                    }

                                }
                                else{ //4pm show
                                    if(notiTime.compareTo(nThreshold1)<0 && notiTime.compareTo(nThreshold)>0 ) {
                                        fNotiStatus = postSnapshot.child("content").getValue(String.class);
                                    }
                                    else{
                                        fNotiStatus = "OK";
                                    }
                                }
                            } catch(ParseException e){
                                e.printStackTrace();
                            }


                        } else if (postSnapshot.child("content").getValue(String.class).contains("Elephant")) {
                            try {
                                Date current1 = sdf.parse(currentDateTime);
                                Date notiTime = sdf.parse(notiDateTime);
                                Date cThreshold = sdf.parse(currentDate + " 12:00:00");
                                Date nThreshold = sdf.parse(currentDate + " 11:50:00");
                                Date nThreshold1 = sdf.parse(currentDate + " 15:50:00");

                                System.out.print("cthreshold : " + current1.compareTo(cThreshold));
                                System.out.print("nthreshold : " + notiTime.compareTo(nThreshold1));
                                //11:30am
                                if(current1.compareTo(cThreshold)<0) {
                                    if(notiTime.compareTo(nThreshold)<0) {
                                        eNotiStatus = postSnapshot.child("content").getValue(String.class);
                                    }
                                }
                                else{ //3.30pm
                                    if(notiTime.compareTo(nThreshold1)<0 && notiTime.compareTo(nThreshold)>0) {
                                        eNotiStatus = postSnapshot.child("content").getValue(String.class);
                                    }
                                    else{
                                        eNotiStatus = "OK";
                                    }
                                }
                            } catch(ParseException e){
                                e.printStackTrace();
                            }

                        } else if (postSnapshot.child("content").getValue(String.class).contains("Rainforest Fights")) {

                            try {
                                Date current1 = sdf.parse(currentDateTime);
                                Date notiTime = sdf.parse(notiDateTime);
                                Date cThreshold = sdf.parse(currentDate + " 13:00:00");
                                Date nThreshold = sdf.parse(currentDate + " 12:50:00");
                                Date nThreshold1 = sdf.parse(currentDate + " 14:50:00");
                                //12:30pm
                                if(current1.compareTo(cThreshold)< 0) {
                                    if(notiTime.before(nThreshold)) {
                                        rNotiStatus = postSnapshot.child("content").getValue(String.class);
                                    }
                                }
                                else{ //2.30pm
                                    if(notiTime.compareTo(nThreshold1)<0 && notiTime.compareTo(nThreshold)>0) {
                                        rNotiStatus = postSnapshot.child("content").getValue(String.class);
                                    }
                                    else{
                                        rNotiStatus = "OK";
                                    }
                                }
                            }
                            catch(ParseException e){
                                e.printStackTrace();
                            }


                        } else if (postSnapshot.child("content").getValue(String.class).contains("Splash")) {
                            try {
                                Date current1 = sdf.parse(currentDateTime);
                                Date notiTime = sdf.parse(notiDateTime);
                                Date cThreshold = sdf.parse(currentDate + " 11:00:00");
                                Date nThreshold = sdf.parse(currentDate + " 10:50:00");
                                Date nThreshold1 = sdf.parse(currentDate + " 17:20:00");
                                //10:30pm
                                if(current.compareTo(cThreshold)<0) {
                                    if(notiTime.compareTo(nThreshold)<0) {
                                        sNotiStatus = postSnapshot.child("content").getValue(String.class);
                                    }
                                }
                                else{ //5pm
                                    if(notiTime.compareTo(nThreshold1)<0 && notiTime.compareTo(nThreshold)>0) {
                                        sNotiStatus = postSnapshot.child("content").getValue(String.class);
                                    }
                                    else{
                                        sNotiStatus = "OK";
                                    }
                                }
                            }
                            catch(ParseException e){
                                e.printStackTrace();
                            }

                        }

                    }
                }
                //Spash/Animal/Ele/Rain
                String previousStatus = cardDataSet.get(CardType.SHOWTIME);
                if(previousStatus != null){
                    String[] parts = previousStatus.split("-");
                    if (fNotiStatus != null) {
                        Log.v(TAG, fNotiStatus);
                        if (fNotiStatus.contains("cancel")) {
                            parts[1] = "CL";
                        } else if (fNotiStatus.contains("delay")) {
                            parts[1] = "DY";
                        }else if(fNotiStatus.equals("OK")){
                            parts[1]= "OK";
                        }
                        else {
                            parts[1] = "FL";
                        }
                    }
                    if (sNotiStatus != null) {
                        if (sNotiStatus.contains("cancel")) {
                            parts[0] = "CL";
                        } else if (sNotiStatus.contains("delay")) {
                            parts[0] = "DY";
                        }else if(sNotiStatus.equals("OK")){
                            parts[0]= "OK";
                        }else {
                            parts[0] = "FL";
                        }
                    }
                    if (eNotiStatus != null) {
                        if (eNotiStatus.contains("cancel")) {
                            parts[2] = "CL";
                        } else if (eNotiStatus.contains("delay")) {
                            parts[2] = "DY";
                        }else if(eNotiStatus.equals("OK")){
                            parts[2]= "OK";
                        } else {
                            parts[2] = "FL";
                        }
                    }
                    if (rNotiStatus != null) {
                        if (rNotiStatus.contains("cancel")) {
                            parts[3] = "CL";
                        } else if (rNotiStatus.contains("delay")) {
                            parts[3] = "DY";
                        }else if(rNotiStatus.equals("OK")){
                            parts[3]= "OK";
                        } else {
                            parts[3] = "FL";
                        }
                    }
                    cardDataSet.put(CardType.SHOWTIME, parts[0] + "-" +  parts[1] + "-" + parts[2] + "-" + parts[3]);
                    customCardAdapter = new CustomCardAdapter(cardDataSet, userID);
                    recyclerView.setAdapter(customCardAdapter);
                }
                else{
                    cardDataSet.put(CardType.SHOWTIME,"OK-OK-OK-OK");
                    customCardAdapter = new CustomCardAdapter(cardDataSet, userID);
                    recyclerView.setAdapter(customCardAdapter);

                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                //Failed on staff count value
                System.out.println("ManagerInfoFragment - Failed to get show notification: " + error.toException());
            }
        });

        //Add created listener into list
        MainActivity.valueEventListenerList.put(FirebaseDatabase.getInstance().getReference().child("notification"), showtimeStatusListener);

        ValueEventListener tramStationStatusListener = FirebaseDatabase.getInstance().getReference().child("service").child("tram").addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> stationStatus = new ArrayList<String>();

                for(DataSnapshot station : dataSnapshot.getChildren()) {
                    stationStatus.add(station.getValue(String.class));
                }

                cardDataSet.put(CardType.TRAM_STATION, stationStatus.get(0)+ "-" + stationStatus.get(1) + "-" + stationStatus.get(2) + "-" + stationStatus.get(3));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                //Failed to get tram station status
                System.out.println("ManagerInfoFragment - Failed to get tram station status: " + error.toException());
            }
        });

        //Add created listener into list
        MainActivity.valueEventListenerList.put(FirebaseDatabase.getInstance().getReference().child("service").child("tram"), tramStationStatusListener);
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
