package com.itpteam11.visualisemandai;

/**
 * This class represents the Message data model
 */
public class Message {
    private String message;
    private String sender;
    private long timestamp;

    public Message() {}

    public Message(String message, String sender, long timestamp) {
        this.message = message;
        this.sender = sender;
        this.timestamp = timestamp;
    }

    public String getMessage() { return message; }
    public String getSender() { return sender; }
    public long getTimestamp() { return timestamp; }
}
