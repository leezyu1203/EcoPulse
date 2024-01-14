package com.example.ecopulse.Information;

public class searchItem {

    private String title;
    private String address;

    public searchItem(String title, String address) {
        this.title = title;
        this.address = address;
    }

    public String getTitle() {
        return title;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return getTitle() + " " + getAddress();
    }
}
