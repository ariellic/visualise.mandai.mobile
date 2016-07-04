package com.itpteam11.visualisemandai;

import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.Map;

/**
 * This class represents the Notification data model
 */
public class Notification {
    private String content;
    private String location;
    private String sender;
    private Map<String, Boolean> receiver;
    private long timestamp;

    public Notification() {}

    public String getContent() { return content; }
    public String getLocation() {  return location; }
    public String getSender() { return sender; }
    public Map<String, Boolean> getReceiver() { return receiver; }
    public long getTimestamp() { return timestamp; }

    public void setContent(String content) { this.content = content; }
    public void setLocation(String location) { this.location = location; }
    public void setSender(String sender) { this.sender = sender; }
    public void setReceiver(Map<String, Boolean> receiver) { this.receiver = receiver; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    /**
     * Create notification in Firebase and send to recipient if receiver list is provided
     *
     * @param  content  Content of notification
     * @param  location Latitude and longitude of user's coordinates where this notice is created
     * @param  sender   Sender user ID or provider name
     * @param  receiver List of recipients
     */
    public void sendNotification(String content, String location, String sender, Map<String, Boolean> receiver) {
        this.content = content;
        this.location = location;
        this.sender = sender;
        this.receiver = receiver;
        this.timestamp = new Date().getTime();

        //Get unique notification ID and store the notice into Firebase
        String notificationID = FirebaseDatabase.getInstance().getReference().child("notification").push().getKey();
        FirebaseDatabase.getInstance().getReference().child("notification").child(notificationID).setValue(this);

        //Record notification has been by this user
        FirebaseDatabase.getInstance().getReference().child("notification-lookup").child(sender).child("send").child(notificationID).setValue(true);

        //Notify all recipients
        if(receiver != null) {
            for(String recipient : receiver.keySet()) {
                FirebaseDatabase.getInstance().getReference().child("notification-lookup").child(recipient).child("receive").child(notificationID).setValue(false);
            }
        }
    }
}
