package com.itpteam11.visualisemandai;

import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  This fragment contain RecyclerView which contain user's notification list
 */

public class NotificationFragment extends Fragment {
    private String userID;
    private Map<String, String> staffIDDirectory = new HashMap<String, String>();

    private int pendingNotifications = 0;
    private int id = 0;
    private ArrayList<NotificationItem> groupedNotifications = new ArrayList<>();

    private List<NotificationItem> notificationList = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager recyclerLayoutManager;
    private NotificationAdapter notificationAdapter;
    private TextView noNotification;

   // private String userlocation;

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

        noNotification = (TextView) view.findViewById(R.id.notification_fragment_no_notification);

        return view;
    }

    /**
     * This method set an event listener to observe for next addition for notification
     * on the user's receive notification node. After which notification will be build
     * to notify the user
     */
    public void updateList() {
        FirebaseDatabase.getInstance().getReference().child("user").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Retrieve a list of user ID to map with staff name
                for(DataSnapshot staff : dataSnapshot.getChildren()) {
                    staffIDDirectory.put(staff.getKey(), staff.child("name").getValue(String.class));
                }

                //Create a receive node if not exist to prevent null exception when added event listener
                FirebaseDatabase.getInstance().getReference().child("notification-lookup").child(userID).child("receive").child("notificationid").setValue(true);

                ChildEventListener userNotificationListener = FirebaseDatabase.getInstance().getReference().child("notification-lookup").child(userID).child("receive").addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                        String notificationID = dataSnapshot.getKey();
                        boolean notified = dataSnapshot.getValue(Boolean.class);

                        //Ignore notificationid record
                        if (!notificationID.equals("notificationid")) {
                            noNotification.setVisibility(View.GONE);

                            //Show notification alert when record is a new notification. Else past notifications will not be alert
                            if (!notified) {
                                //Retrieve the actual notification by ID
                                pendingNotifications++;
                                FirebaseDatabase.getInstance().getReference().child("notification").child(notificationID).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        //Store notification details into Notification object
                                        Notification notification = dataSnapshot.getValue(Notification.class);

                                        // Create a NotificationItem to be added into the notification list
                                        // When notification's and user's location exist
                                        if (notification.getLatitude() != null && notification.getLongitude() != null) {
                                            NotificationItem notificationItem = new NotificationItem(dataSnapshot.getKey(), notification.getType(), notification.getContent(), resolveSenderName(notification.getSender()), notification.getTimestamp(), calculateProxi(notification.getLatitude(), notification.getLongitude()), notification.getImageName());
                                            groupedNotifications.add(notificationItem); // For stacking notifications
                                            notificationList.add(notificationItem);
                                        }
                                        // Image node not available in Firebase
                                        else {
                                            NotificationItem notificationItem = new NotificationItem(dataSnapshot.getKey(), notification.getType(), notification.getContent(), resolveSenderName(notification.getSender()), notification.getTimestamp(), null, notification.getImageName());
                                            groupedNotifications.add(notificationItem); // For stacking notifications
                                            notificationList.add(notificationItem);
                                        }

                                        final String GROUP_NOTIFICATIONS = "group_notifications";

                                        //Build system notification to notify the user
                                        NotificationCompat.Builder notify = new NotificationCompat.Builder(getContext())
                                                .setContentTitle(resolveSenderName(notification.getSender()))
                                                .setContentText(notification.getContent())
                                                .setSmallIcon(R.drawable.notification)
                                                .setGroup(GROUP_NOTIFICATIONS)
                                                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});

                                        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender();

                                        // Customizing big text, icons and bg to appear for notifications on mobile/wearable
                                        CharSequence desc = "";
                                        switch (dataSnapshot.child("sender").getValue().toString()) {
                                            case "NEA - Weather (Rain)":
                                                wearableExtender = getWearableDesign(R.drawable.rain, R.drawable.rainy);
                                                desc = Html.fromHtml("<b>" + notification.getContent() + "</b><br /> It's going to rain soon, do advise the visitors to stay sheltered and do the same for yourself too!");
                                                break;
                                            case "NEA - Weather (Sun)":
                                                wearableExtender = getWearableDesign(R.drawable.bottle, R.drawable.sunny);
                                                desc = Html.fromHtml("<b>" + notification.getContent() + "</b><br /> Do drink more water as the weather is getting warmer.");
                                                break;
                                            case "NEA - PSI":
                                                wearableExtender = getWearableDesign(R.drawable.haze, R.drawable.hazy);
                                                desc = Html.fromHtml("<b>" + notification.getContent() + "</b><br /> Do wear a mask wherever you are outdoors and do alert the visitors to wear one too.");
                                                break;
                                            case "OpenWeather":
                                                wearableExtender = getWearableDesign(R.drawable.bottle, R.drawable.sunny);
                                                desc = Html.fromHtml("<b>" + notification.getContent() + "</b><br /> Do drink more water as the weather is getting warmer.");
                                                break;
                                            default:
                                                desc = Html.fromHtml("<b>Notice from " + resolveSenderName(notification.getSender()) + "</b><br />" + notification.getContent());
                                                break;
                                        }


                                        String notificationWord = "";
                                        if (pendingNotifications == 1) {
                                            notificationWord = " notification";
                                        } else {
                                            notificationWord = " notifications";
                                        }

                                        notify.setStyle(new NotificationCompat.BigTextStyle()
                                                //.setSummaryText(Integer.toString(pendingNotifications) + notificationWord + " from Visualizing Mandai")
                                                .bigText(desc));
                                /*} else if (pendingNotifications > 1) {
                                    NotificationCompat.InboxStyle inbox = new NotificationCompat.InboxStyle();
                                    {
                                        for (NotificationItem item : groupedNotifications) {
                                            String stackNotificationLine = item.getSender();
                                            if (stackNotificationLine != null) {
                                                inbox.addLine(stackNotificationLine);
                                            }
                                        }

                                        // the summary text will appear at the bottom of the expanded stack notification
                                        // we just display the same thing from above (don't forget to use string
                                        // resource formats!)
                                        inbox.setSummaryText(String.format("%d new notifications from Visualizing Mandai", groupedNotifications.size()));
                                    }
                                    notify.setStyle(inbox);
                                } */

                                        notify.extend(wearableExtender);
                                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());
                                        notificationManager.notify(id++, notify.build()); // Display multiple notifications, prevent replacing of notification

                                        //Sort latest item to be at top of notification list
                                        Collections.sort(notificationList, new NotificationItem());

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
                                //Retrieve the actual notification by ID
                                FirebaseDatabase.getInstance().getReference().child("notification").child(notificationID).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        //Store notification details into Notification object
                                        Notification notification = dataSnapshot.getValue(Notification.class);

                                        // Create a NotificationItem to be added into the notification list
                                        // When notification's and user's location exist
                                        if (notification.getLatitude() != null && notification.getLongitude() != null) {
                                            NotificationItem notificationItem = new NotificationItem(dataSnapshot.getKey(), notification.getType(), notification.getContent(), resolveSenderName(notification.getSender()), notification.getTimestamp(), calculateProxi(notification.getLatitude(), notification.getLongitude()), notification.getImageName());
                                            notificationList.add(notificationItem);
                                        }
                                        // Image node not available in Firebase
                                        else {
                                            NotificationItem notificationItem = new NotificationItem(dataSnapshot.getKey(), notification.getType(), notification.getContent(), resolveSenderName(notification.getSender()), notification.getTimestamp(), null, notification.getImageName());
                                            notificationList.add(notificationItem);
                                        }

                                        //Sort latest item to be at top of notification list
                                        Collections.sort(notificationList, new NotificationItem());

                                        //Update notification list
                                        notificationAdapter.notifyDataSetChanged();

                                        //Set notification has received(set true)
                                        FirebaseDatabase.getInstance().getReference().child("notification-lookup").child(userID).child("receive").child(dataSnapshot.getKey()).setValue(true);
                                        FirebaseDatabase.getInstance().getReference().child("notification").child(dataSnapshot.getKey()).child("receiver").child(userID).setValue(true);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError error) { }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled (DatabaseError databaseError){
                        //Failed listen for notification
                        System.out.println("NotificationFragment - Failed listen for notification: " + databaseError.toException());
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) { }
                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) { }
                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) { }
                });

                //Add created listener into list
                MainActivity.childEventListenerList.put(FirebaseDatabase.getInstance().getReference().child("notification-lookup").child(userID).child("receive"), userNotificationListener);
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    /**
     * This method sets background and icon for different types of notifications to appear on the wearable
     * @param icon
     * @param bg
     * @return
     */
    private NotificationCompat.WearableExtender getWearableDesign(int icon, int bg) {
        NotificationCompat.WearableExtender extender = new NotificationCompat.WearableExtender()
                .setHintHideIcon(true)
                .setContentIcon(icon)
                .setBackground(BitmapFactory.decodeResource(getContext().getResources(), bg));

        return extender;
    }

    /*
     * This method will resolve sender into actual user name if exist
     */
    private String resolveSenderName(String sender) {
        if(staffIDDirectory.containsKey(sender)) {
            return staffIDDirectory.get(sender);
        }

        return sender;
    }

    public Double calculateProxi(double notificationLatitude, double notificationLongtitude){
        DecimalFormat round = new DecimalFormat("#.##");
        //double distM = userLoc.distanceTo(notiLoc);
        if(StaffLocationService.isLocationPermissionGranted()) {
            double distM = distance(notificationLatitude, notificationLongtitude, StaffLocationService.getLatitude(), StaffLocationService.getLongitude());
            distM = distM * 1000;
            return Double.parseDouble(round.format(distM));
        }

        return null;
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515* 1.609344;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
}
