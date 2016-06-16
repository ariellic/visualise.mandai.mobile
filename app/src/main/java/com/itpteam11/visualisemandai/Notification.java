package com.itpteam11.visualisemandai;

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
}
