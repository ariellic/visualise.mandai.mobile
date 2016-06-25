package com.itpteam11.visualisemandai;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 *  This fragment contain RecyclerView which contain user's notification list
 */

public class NotificationFragment extends Fragment {
    private String userID;

    private List<NotificationItem> notificationList = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager recyclerLayoutManager;
    private NotificationAdapter notificationAdapter;

    public NotificationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Retrieve value pass from activity
        Bundle bundle = getArguments();
        userID = bundle.getString("userID");

        updateList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.notification_fragment_recyclerview);

        notificationAdapter = new NotificationAdapter(notificationList);
        recyclerLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(recyclerLayoutManager);

        //To have a divider underneath each list item
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(notificationAdapter);

        return view;
    }

    /**
     * This method set an event listener for an addition to the respective notification node
     * for weather/temp/PSI when an alert has been added in.
     * @param type
     */
    public void updateEnvData(String type, final String sender) {
        DatabaseReference notiRef = FirebaseDatabase.getInstance().getReference().child("service").child(type).child("notifications");
        notiRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                NotificationItem notificationItem = new NotificationItem(dataSnapshot.child("title").getValue().toString(), sender, Long.parseLong(dataSnapshot.getKey()));
                Log.d("NOTITITLE getMessage", notificationItem.getContent());
                Log.d("NOTITITLE getSender", notificationItem.getSender());
                Log.d("NOTITITLE getTimestamp", notificationItem.getTimestamp() + "");
                notificationList.add(notificationItem);
                notificationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    //This method set an event listener to observe for next addition for notification
    //on the user's receive notification node. After which notification will be build
    //to notify the user
    public void updateList() {
        FirebaseDatabase.getInstance().getReference().child("notification-lookup").child(userID).child("receive").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                String notificationID = dataSnapshot.getKey();
                boolean notified = dataSnapshot.getValue(Boolean.class);

                //Show notification alert when record is a new notification. Else past notifications will not be alert
                if (!notified) {
                    //Retrieve the actual notification by ID
                    FirebaseDatabase.getInstance().getReference().child("notification").child(notificationID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //Store notification details into Notification object
                            Notification notification = dataSnapshot.getValue(Notification.class);

                            //Create a NotificationItem to be added into the notification list
                            NotificationItem notificationItem = new NotificationItem(notification.getContent(), notification.getSender(), notification.getTimestamp());
                            notificationList.add(notificationItem);

                            //Build system notification to notify the user
                            NotificationCompat.Builder notify = new NotificationCompat.Builder(getContext())
                                    .setContentTitle(notification.getSender())
                                    .setContentText(notification.getContent())
                                    .setSmallIcon(R.drawable.notification)
                                    .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });

                            NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender();

                            String desc = "";
                            switch (dataSnapshot.child("sender").getValue().toString()) {
                                case "NEA - Weather (Rain)":
                                    wearableExtender = getWearableDesign(R.drawable.rain, R.drawable.rainy);
                                    desc = "It's going to rain soon, do advise the visitors to stay sheltered and do the same for yourself too!";
                                    break;
                                case "NEA - Weather (Sun)":
                                    wearableExtender = getWearableDesign(R.drawable.bottle, R.drawable.sunny);
                                    desc = "Do drink more water as the weather is getting warmer.";
                                    break;
                                case "NEA - PSI":
                                    wearableExtender = getWearableDesign(R.drawable.haze, R.drawable.hazy);
                                    desc = "Do wear a mask wherever you are outdoors and do alert the visitors to wear one too.";
                                    break;
                                case "OpenWeather":
                                    wearableExtender = getWearableDesign(R.drawable.bottle, R.drawable.sunny);
                                    desc = "Do drink more water as the weather is getting warmer.";
                                    break;
                            }

                            notify.setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText(desc));
                            notify.extend(wearableExtender);
                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());
                            notificationManager.notify(0, notify.build());

                            //Update notification list
                            notificationAdapter.notifyDataSetChanged();

                            //Set notification has received(set true)
                            FirebaseDatabase.getInstance().getReference().child("notification-lookup").child(userID).child("receive").child(dataSnapshot.getKey()).setValue(true);
                            FirebaseDatabase.getInstance().getReference().child("notification").child(dataSnapshot.getKey()).child("receiver").child(userID).setValue(true);
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                        }
                    });
                } else {
                    Log.d("ELSE", "Notification ID: " + notificationID);
                    //Retrieve the actual notification by ID
                    FirebaseDatabase.getInstance().getReference().child("notification").child(notificationID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //Store notification details into Notification object
                            Notification notification = dataSnapshot.getValue(Notification.class);

                            Log.d("ELSE", "Notification content: " + notification.getContent());
                            Log.d("ELSE", "Notification sender: " + notification.getSender());
                            Log.d("ELSE", "Notification timestamp: " + notification.getTimestamp());
                            //Create a NotificationItem to be added into the notification list
                            NotificationItem notificationItem = new NotificationItem(notification.getContent(), notification.getSender(), notification.getTimestamp());
                            notificationList.add(notificationItem);

                            //Update notification list
                            notificationAdapter.notifyDataSetChanged();

                            //Set notification has received(set true)
                            FirebaseDatabase.getInstance().getReference().child("notification-lookup").child(userID).child("receive").child(dataSnapshot.getKey()).setValue(true);
                            FirebaseDatabase.getInstance().getReference().child("notification").child(dataSnapshot.getKey()).child("receiver").child(userID).setValue(true);
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                        }
                    });
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Failed listen for notification
                System.out.println("NotificationFragment - Failed listen for notification: " + databaseError.toException());
            }
        });
    }

    private NotificationCompat.WearableExtender getWearableDesign(int icon, int bg) {
        NotificationCompat.WearableExtender extender = new NotificationCompat.WearableExtender()
                .setHintHideIcon(true)
                .setContentIcon(icon)
                .setBackground(BitmapFactory.decodeResource(getContext().getResources(), bg));

        return extender;

    }
}
