package com.veselin.zevae.models;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.List;

public class User {

    private String id;
    private String username;
    private String imageURL;
    private String description;
    private List<String> interactions;
    private boolean isOnline;
    private String birthday;
    private String country;
    private String gender;
    private String timezone;
    private List<String> friendsIDs;
    private int friendsCount;
    private int interactionsCount;
    private String email;
    private Double latitude;
    private Double longitude;
    private String lastLocationTime;
    private Boolean hasStory;
    private String lastOnline;
    public User(String id, String username, String imageURL, String description, String birthday, String country, String gender, int friendsCount, int interactionsCount, String email, Double latitude, Double longitude, String lastLocationTime, String lastOnline) {
        this.id = id;
        this.username = username;
        this.imageURL = imageURL;
        this.description = description;
        this.birthday = birthday;
        this.country = country;
        this.gender = gender;
        this.friendsCount = friendsCount;
        this.interactionsCount = interactionsCount;
        this.email = email;
        this.latitude = latitude;
        this.longitude = longitude;
        this.lastLocationTime = lastLocationTime;
        this.lastOnline = lastOnline;
    }

    public User(String id, String imageURL, String username){
        this.id = id;
        this.imageURL = imageURL;
        this.username = username;
    }
    public User(){

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getInteractions() {
        return interactions;
    }

    public void setInteractions(List<String> interactions) {
        this.interactions = interactions;
    }

    public boolean getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(boolean online) {
        isOnline = online;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public List<String> getFriendsIDs() {
        return friendsIDs;
    }

    public void setFriendsIDs(List<String> friendsIDs) {
        this.friendsIDs = friendsIDs;
    }

    public int getFriendsCount() {
        return friendsCount;
    }

    public void setFriendsCount(int friendsCount) {
        this.friendsCount = friendsCount;
    }

    public int getInteractionsCount() {
        return interactionsCount;
    }

    public void setInteractionsCount(int interactionsCount) {
        this.interactionsCount = interactionsCount;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void updateInteractionsConvos(){
        FirebaseDatabase.getInstance().getReference("Users")
                .child(getId()).child("FriendRequest").child("Accepted").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                setFriendsCount((int) snapshot.getChildrenCount());
                FirebaseDatabase.getInstance().getReference("Users")
                        .child(getId()).child("friendsCount").setValue(getFriendsCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        FirebaseDatabase.getInstance().getReference("Users")
                .child(getId()).child("Convos").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                setInteractionsCount((int) snapshot.getChildrenCount());
                FirebaseDatabase.getInstance().getReference("Users")
                        .child(getId()).child("interactionsCount").setValue(getInteractionsCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    public LatLng getLatLon(){
        return new LatLng(this.latitude, this.longitude);
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getLastLocationTime() {
        return lastLocationTime;
    }

    public void setLastLocationTime(String lastLocationTime) {
        this.lastLocationTime = lastLocationTime;
    }

    public Boolean getHasStory() {
        return hasStory;
    }

    public void setHasStory(Boolean hasStory) {
        this.hasStory = hasStory;
    }

    public String getLastOnline() {
        return lastOnline;
    }

    public void setLastOnline(String lastOnline) {
        this.lastOnline = lastOnline;
    }
}
