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
    private String proxi;

    public NotificationItem() {}

    public NotificationItem(String notificationID, String type, String content, String sender, long timestamp, String proxi) {
        this.notificationID = notificationID;
        this.type = type;
        this.content = content;
        this.sender = sender;
        this.timestamp = timestamp;
        this.proxi = proxi;
    }

    public String getNotificationID() { return notificationID; }
    public String getType() { return type; }
    public String getContent() { return content; }
    public String getSender() { return sender; }
    public long getTimestamp() { return timestamp; }
    public String getProxi(){return proxi;}

    //This method does the sorting of latest notification on top
    @Override
    public int compare(NotificationItem item1, NotificationItem item2) {
        //System.out.println("NotificationItem :" +  (item2.timestamp - item1.timestamp));
        return ((int) item2.timestamp) - ((int) item1.timestamp);
    }
}
