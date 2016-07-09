package com.itpteam11.visualisemandai;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This custom RecyclerView adapter will create and hold multiple notification
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private List<NotificationItem> notificationList;

    public NotificationAdapter(List<NotificationItem> notificationList) {
        this.notificationList = notificationList;
    }

    @Override
    public NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item;

        //Create respective notification item based on notification type
        switch (viewType) {
            case 1:
                item = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_list_escape, parent, false);
                return new EscapeNotificationViewHolder(item);
            default:
                item = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_list, parent, false);
                return new NormalNotificationViewHolder(item);
        }
    }

    @Override
    public void onBindViewHolder(NotificationViewHolder holder, int position) {
        NotificationItem notification = notificationList.get(position);

        //Set respective notification item's widgets with content
        switch (holder.getItemViewType()) {
            case 0:
                NormalNotificationViewHolder normalNotificationViewHolder = (NormalNotificationViewHolder) holder;
                normalNotificationViewHolder.message.setText(notification.getContent());
                normalNotificationViewHolder.sender.setText(notification.getSender());
                normalNotificationViewHolder.timestamp.setText(new SimpleDateFormat("dd MMM yyyy h:mm a").format(new Date(notification.getTimestamp())));
                break;
            case 1:
                EscapeNotificationViewHolder escapeNotificationViewHolder = (EscapeNotificationViewHolder) holder;
                escapeNotificationViewHolder.message.setText(notification.getContent());
                escapeNotificationViewHolder.sender.setText(notification.getSender());
                escapeNotificationViewHolder.timestamp.setText(new SimpleDateFormat("dd MMM yyyy h:mm a").format(new Date(notification.getTimestamp())));
                escapeNotificationViewHolder.setResolveButtonListener(notification.getNotificationID(), notification.getContent());
                break;
        }
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    //Method for onCreateViewHolder to identify viewType parameter
    @Override
    public int getItemViewType(int position) {
        String type = notificationList.get(position).getType();

        //Set type is normal when no type stated
        if (notificationList.get(position).getType() == null) {
            type = Notification.NORMAL_NOTIFICATION;
        }

        //Return numeric type
        switch (type) {
            case Notification.ESCAPE_NOTIFICATION:
                return 1;
            default:
                return 0;
        }
    }

    //Super CardView
    public class NotificationViewHolder extends RecyclerView.ViewHolder {
        public NotificationViewHolder(View v) {
            super(v);
        }
    }

    //Normal notification item
    private class NormalNotificationViewHolder extends NotificationViewHolder {
        public TextView message, sender, timestamp;

        public NormalNotificationViewHolder(View view) {
            super(view);
            message = (TextView) view.findViewById(R.id.notification_list_message);
            sender = (TextView) view.findViewById(R.id.notification_list_sender);
            timestamp = (TextView) view.findViewById(R.id.notification_list_timestamp);
        }
    }

    //Animal escape notification item
    private class EscapeNotificationViewHolder extends NotificationViewHolder {
        private TextView message, sender, timestamp;
        private Button resolve;
        private View view;

        public EscapeNotificationViewHolder(View view) {
            super(view);
            this.view = view;
            message = (TextView) view.findViewById(R.id.notification_list_escape_message);
            sender = (TextView) view.findViewById(R.id.notification_list_escape_sender);
            timestamp = (TextView) view.findViewById(R.id.notification_list_escape_timestamp);
            resolve = (Button) view.findViewById(R.id.notification_list_escape_resolve_button);
        }

        //Set listener for Resolve button
        public void setResolveButtonListener(final String notificationID, final String content) {
            resolve.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    //Set notification type as normal and notice has been resolved
                    FirebaseDatabase.getInstance().getReference().child("notification").child(notificationID).child("type").setValue(Notification.NORMAL_NOTIFICATION);

                    //Get the list of current staff who is not on off
                    FirebaseDatabase.getInstance().getReference().child("user").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Map<String, Boolean> receiver = new HashMap<String, Boolean>();

                            //Populate the list with staff who is not on off
                            for (DataSnapshot child : dataSnapshot.getChildren()) {
                                if (!child.child("status").getValue(String.class).equals("off")) {
                                    receiver.put(child.getKey(), false);
                                }
                            }

                            //Get user's coordinates to indicate where the animal has captured
                            String coordinates = null;
                            if (StaffLocationService.isLocationPermissionGranted()) {
                                coordinates = StaffLocationService.getLatitude() + "-" + StaffLocationService.getLongitude();
                            }

                            //Create a notification with necessary information to notify staff who is not on off
                            Notification notification = new Notification();
                            notification.sendNotification(Notification.NORMAL_NOTIFICATION, content.split(" has")[0] + " has been captured!", coordinates, FirebaseAuth.getInstance().getCurrentUser().getUid(), receiver);
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Failed to get working staff list
                            System.out.println("Failed to get working staff list: " + error.toException());
                        }
                    });
                }
            });
        }
    }
}