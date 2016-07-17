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
    public static final String IMAGE_NOTIFICATION = "image";

    //Service type constants
    public static final String PSI_SERVICE = "psi";
    public static final String TEMPERATURE_SERVICE = "temperature";
    public static final String WEATHER_SERVICE = "weather";

    private String type;
    private String content;
    private Double latitude;
    private Double longitude;
    private String sender;
    private Map<String, Boolean> receiver;
    private long timestamp;
    private String imageName;

    public Notification() {}

    public String getType() { return type; }
    public String getContent() { return content; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public String getSender() { return sender; }
    public Map<String, Boolean> getReceiver() { return receiver; }
    public long getTimestamp() { return timestamp; }
    public String getImageName() { return imageName; }

    public void setType(String type) { this.type = type; }
    public void setContent(String content) { this.content = content; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public void setSender(String sender) { this.sender = sender; }
    public void setReceiver(Map<String, Boolean> receiver) { this.receiver = receiver; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setImageName(String imgName) { this.imageName = imgName; }


    /**
     * Create notification in Firebase and send to recipient if receiver list is provided
     *
     * @param  type         Type of notification
     * @param  content      Content of notification
     * @param  latitude     latitude of user's coordinates where this notice was created
     * @param  longitude    longitude of user's coordinates where this notice was created
     * @param  sender       Sender user ID or provider name
     * @param  receiver     List of recipients
     * @param  imgName      Image sent along with notification
     */
    public void sendNotification(String type, String content, Double latitude, Double longitude, String sender, Map<String, Boolean> receiver, String imgName) {
        this.type = type;
        this.content = content;
        this.latitude = latitude;
        this.longitude = longitude;
        this.sender = sender;
        this.receiver = receiver;
        this.timestamp = new Date().getTime();
        this.imageName = imgName;

        //Get unique notification ID and store the notice into Firebase
        String notificationID = FirebaseDatabase.getInstance().getReference().child("notification").push().getKey();
        FirebaseDatabase.getInstance().getReference().child("notification").child(notificationID).setValue(this);

        //Record notification has been sent by this user
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
