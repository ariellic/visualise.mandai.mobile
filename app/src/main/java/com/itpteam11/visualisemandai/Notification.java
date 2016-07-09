package com.itpteam11.visualisemandai;

import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.Map;

/**
 * This class represents the Notification data model
 */
public class Notification {
    //Notification type constants
    public static final String NORMAL_NOTIFICATION = "normal";
    public static final String ESCAPE_NOTIFICATION = "escape";

    //Service type constants
    public static final String PSI_SERVICE = "psi";
    public static final String TEMPERATURE_SERVICE = "temperature";
    public static final String WEATHER_SERVICE = "weather";

    private String type;
    private String content;
    private String location;
    private String sender;
    private Map<String, Boolean> receiver;
    private long timestamp;

    public Notification() {}

    public String getType() { return type; }
    public String getContent() { return content; }
    public String getLocation() {  return location; }
    public String getSender() { return sender; }
    public Map<String, Boolean> getReceiver() { return receiver; }
    public long getTimestamp() { return timestamp; }

    public void setType(String type) { this.type = type; }
    public void setContent(String content) { this.content = content; }
    public void setLocation(String location) { this.location = location; }
    public void setSender(String sender) { this.sender = sender; }
    public void setReceiver(Map<String, Boolean> receiver) { this.receiver = receiver; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    /**
     * Create notification in Firebase and send to recipient if receiver list is provided
     *
     * @param  type     Type of notification
     * @param  content  Content of notification
     * @param  location Latitude and longitude of user's coordinates where this notice is created
     * @param  sender   Sender user ID or provider name
     * @param  receiver List of recipients
     */
    public void sendNotification(String type, String content, String location, String sender, Map<String, Boolean> receiver) {
        this.type = type;
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

    /**
     * Handle sending of notification for services
     *
     * @param  service      Type of service
     * @param  content      Content of notification
     * @param  sender       Sender user ID or provider name
     * @param  latestValue  Latest comparable value
     */
    public void sendServiceNotification(String service, String content, String sender, Object latestValue) {
        this.content = content;
        this.sender = sender;
        this.timestamp = new Date().getTime();

        //Get unique notification ID and store the notice into Firebase
        String notificationID = FirebaseDatabase.getInstance().getReference().child("notification").push().getKey();
        FirebaseDatabase.getInstance().getReference().child("notification").child(notificationID).setValue(this);

        //Change notification ID to alert listening subscriber about changes
        FirebaseDatabase.getInstance().getReference().child("service").child(service).child("notification-id").setValue(notificationID);

        //Update new value for climate type
        FirebaseDatabase.getInstance().getReference().child("service").child(service).child("value").setValue(latestValue);
    }
}
