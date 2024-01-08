package com.example.ecopulse;

import com.google.firebase.firestore.Exclude;

public class UploadEvent {
    private String eventName;
    private String eventDesc;
    private String eventVenue;
    private String eventDate;
    private String eventStartTime;
    private String eventEndTime;
    private String imageUrl;
    private String timestamp;
    private String userID;

    public UploadEvent() {}
    public UploadEvent(String eventName, String eventDesc, String eventVenue, String eventDate, String eventStartTime, String eventEndTime, String imageUrl, String timestamp, String userID) {
        this.eventName = eventName;
        this.eventDesc = eventDesc;
        this.eventVenue = eventVenue;
        this.eventDate = eventDate;
        this.eventStartTime = eventStartTime;
        this.eventEndTime = eventEndTime;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
        this.userID = userID;
    }

    public UploadEvent(String eventName, String eventDesc, String eventVenue, String eventDate, String eventStartTime, String eventEndTime) {
        this.eventName = eventName;
        this.eventDesc = eventDesc;
        this.eventVenue = eventVenue;
        this.eventDate = eventDate;
        this.eventStartTime = eventStartTime;
        this.eventEndTime = eventEndTime;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventDesc() {
        return eventDesc;
    }

    public void setEventDesc(String eventDesc) {
        this.eventDesc = eventDesc;
    }

    public String getEventVenue() {
        return eventVenue;
    }

    public void setEventVenue(String eventVenue) {
        this.eventVenue = eventVenue;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getEventStartTime() {
        return eventStartTime;
    }

    public void setEventStartTime(String eventStartTime) {
        this.eventStartTime = eventStartTime;
    }

    public String getEventEndTime() {
        return eventEndTime;
    }

    public void setEventEndTime(String eventEndTime) {
        this.eventEndTime = eventEndTime;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
