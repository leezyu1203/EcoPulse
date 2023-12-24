package com.example.ecopulse;

import com.google.firebase.firestore.Exclude;

public class EventCollaborator extends User {

    private String eventSpecificField;

    public EventCollaborator() {
        super();
        // Default constructor required for Firestore
    }

    public EventCollaborator(String name, String email, String password, String rePassword, String eventSpecificField) {
        super(name, email, password, rePassword);
        this.eventSpecificField = eventSpecificField;
    }

    public String getEventSpecificField() {
        return eventSpecificField;
    }

    public void setEventSpecificField(String eventSpecificField) {
        this.eventSpecificField = eventSpecificField;
    }

    @Override
    public String toString() {
        return super.toString() + " EventCollaborator{" +
                "eventSpecificField='" + eventSpecificField + '\'' +
                '}';
    }
}
