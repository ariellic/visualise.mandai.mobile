package com.itpteam11.visualisemandai;

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

        updateEnvData("weather", "NEA forecast");
        updateEnvData("psi", "NEA forecast");
        updateEnvData("temperature", "OpenWeather forecast");
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
                Log.d("NOTITITLE getTimestamp", notificationItem.getTimestamp()+"");
                notificationList.add(notificationItem);
                notificationAdapter.notifyDataSetChanged();
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onCancelled(DatabaseError databaseError) {}
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
                if(!notified) {
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
                                    .setContentTitle("Notification from " + notification.getSender())
                                    .setContentText(notification.getContent())
                                    .setSmallIcon(R.drawable.notification);

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
                }
                else {
                    //Retrieve the actual notification by ID
                    FirebaseDatabase.getInstance().getReference().child("notification").child(notificationID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //Store notification details into Notification object
                            Notification notification = dataSnapshot.getValue(Notification.class);

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
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {}
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {}
            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Failed listen for notification
                System.out.println("NotificationFragment - Failed listen for notification: " + databaseError.toException());
            }
        });
    }
}
