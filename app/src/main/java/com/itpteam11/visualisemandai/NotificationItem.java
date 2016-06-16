package com.itpteam11.visualisemandai;

/**
 * This class represents the data model for displaying the notification list
 */
public class NotificationItem {
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
}
