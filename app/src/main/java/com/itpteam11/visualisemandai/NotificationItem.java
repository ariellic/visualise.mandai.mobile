package com.itpteam11.visualisemandai;

import java.util.Comparator;

/**
 * This class represents the data model for displaying the notification list
 */
public class NotificationItem implements Comparator<NotificationItem> {
    private String notificationID;
    private String type;
    private String content;
    private String sender;
    private long timestamp;
    private Double proxi;
    private String imageName;

    public NotificationItem() {}

    public NotificationItem(String notificationID, String type, String content, String sender, long timestamp, Double proxi, String img) {
        this.notificationID = notificationID;
        this.type = type;
        this.content = content;
        this.sender = sender;
        this.timestamp = timestamp;
        this.proxi = proxi;
        this.imageName = img;
    }

    public String getNotificationID() { return notificationID; }
    public String getType() { return type; }
    public String getContent() { return content; }
    public String getSender() { return sender; }
    public long getTimestamp() { return timestamp; }
    public Double getProxi(){return proxi;}
    public String getImageName(){return imageName;}

    //This method does the sorting of latest notification on top
    @Override
    public int compare(NotificationItem item1, NotificationItem item2) {
        return ((int) item2.timestamp) - ((int) item1.timestamp);
    }
}
