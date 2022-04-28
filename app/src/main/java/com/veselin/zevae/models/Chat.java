package com.veselin.zevae.models;

import java.util.HashMap;

public class Chat {
    private User userSender;
    private User userReceiver;
    private String chatID;

    public Chat(){

    }
    public Chat(User userSender, User userReceiver) {
        this.userSender = userSender;
        this.userReceiver = userReceiver;
        setChatID(userSender.getId(), userReceiver.getId());
    }

    public User getUserSender() {
        return userSender;
    }

    public void setUserSender(User userSender) {
        this.userSender = userSender;
    }

    public User getUserReceiver() {
        return userReceiver;
    }

    public void setUserReceiver(User userReceiver) {
        this.userReceiver = userReceiver;
    }

    public String getChatID(){
        return  this.chatID;
    }

    public void setChatID(String uId1, String uId2){
        this.chatID = uId1.compareTo(uId2) > 0 ? uId1 + uId2 : uId2 + uId1;
    }

//    public HashMap<String, String> buildChatHashMap(){
//        HashMap<String, String> message = new HashMap<>();
//
//
//        return message;
//    }
}
