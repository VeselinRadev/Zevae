package com.veselin.zevae.models;

import java.util.HashMap;

public class Message {
    private String senderId;
    private String receiverId;
    private String message;
    private String timestamp;

    public Message(String messages, String receiverId, String senderId, String timestamp) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = messages;
        this.timestamp = timestamp;
    }
    public Message(){

    }
    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public HashMap<String, String> buildMessageHashMap(){
        HashMap<String, String> message = new HashMap<>();
        message.put("messages", getMessage());
        message.put("senderId", getSenderId());
        message.put("receiverId", getReceiverId());
        message.put("timestamp", getTimestamp());
        message.put("status", "delivered");
        return message;
    }

}
