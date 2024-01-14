package com.example.ecopulse.Information;

public class RequestListItem {
    private String dayOfweek;
    private String time;
    private String address;
    private String contact;
    private String note;
    private String status;
    private String id;

    public RequestListItem(String dayOfWeek, String time, String address, String contact, String note, String status, String id) {
        this.dayOfweek = dayOfWeek;
        this.time = time;
        this.address = address;
        this.contact = contact;
        this.id = id;
        this.note = note;
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public String getNote() {
        return note;
    }

    public String getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public String getContact() {
        return contact;
    }

    public String getDayOfweek() {
        return dayOfweek;
    }

    public String getTime() {
        return time;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setDayOfweek(String dayOfweek) {
        this.dayOfweek = dayOfweek;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
