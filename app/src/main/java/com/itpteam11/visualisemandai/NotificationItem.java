package com.itpteam11.visualisemandai;

import java.util.Comparator;

/**
 * This class represents the data model for displaying the notification list
 */
public class NotificationItem implements Comparator<NotificationItem> {
    private String content;
    private String sender;
    private long timestamp;

    public NotificationItem() {}

    public NotificationItem(String content, String sender, long timestamp) {
        this.content = content;
        this.sender = sender;
        this.timestamp = timestamp;
    }

    public String getContent() { return content; }
    public String getSender() { return sender; }
    public long getTimestamp() { return timestamp; }

    //This method does the sorting of latest notification on top
    @Override
    public int compare(NotificationItem item1, NotificationItem item2) {
        System.out.println("NotificationItem :" +  (item2.timestamp - item1.timestamp));
        return ((int) item2.timestamp) - ((int) item1.timestamp);
    }
}
