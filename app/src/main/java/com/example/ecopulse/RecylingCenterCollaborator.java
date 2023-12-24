package com.example.ecopulse;

import com.google.firebase.firestore.Exclude;

public class RecylingCenterCollaborator extends User {

    private String recyclingCenterID;

    public RecylingCenterCollaborator() {
    }

    public RecylingCenterCollaborator(String name, String email, String password, String rePassword, String recyclingCenterID) {
        super(name, email, password, rePassword);
        this.recyclingCenterID = recyclingCenterID;
    }

    public String getRecyclingCenterID() {
        return recyclingCenterID;
    }

    public void setRecyclingCenterID(String recyclingCenterID) {
        this.recyclingCenterID = recyclingCenterID;
    }

    @Override
    public String toString() {
        return super.toString() + " RecyclingCenterCollaborator{" +
                "recyclingCenterSpecificField='" + recyclingCenterID + '\'' +
                '}';
    }
}
