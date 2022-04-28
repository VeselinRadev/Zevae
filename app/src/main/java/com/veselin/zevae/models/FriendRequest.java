package com.veselin.zevae.models;

import java.util.HashMap;

public class FriendRequest {
    private String status;
    private String senderID;
    private String receiverID;

    public FriendRequest() {
    }

    public FriendRequest(String status, String senderID, String receiverID) {
        this.status = status;
        this.senderID = senderID;
        this.receiverID = receiverID;
    }

    public String getStatus() {
        return status;//sent, received, accepted
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getReceiverID() {
        return receiverID;
    }

    public void setReceiverID(String receiverID) {
        this.receiverID = receiverID;
    }

    public HashMap<String, String> getHashMap(){
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("status", status);
        hashMap.put("senderID", senderID);
        hashMap.put("receiverID", receiverID);
        return hashMap;
    }
}
