package com.example.ecopulse;

public class AdminApprovalListItem {
    private String name;
    private String address;
    private String contact;
    private String email;
    private String status;

    private String role;
    private String id;

    private String opening;

    private String type;

    public AdminApprovalListItem(String name, String address, String contact, String email, String status, String role, String id) {
        this.name = name;
        this.address = address;
        this.contact = contact;
        this.email = email;
        this.status = status;
        this.role = role;
        this.id = id;
    }

    public AdminApprovalListItem(String name, String address, String contact, String email, String status, String role, String id, String opening, String type) {
        this.name = name;
        this.address = address;
        this.contact = contact;
        this.email = email;
        this.status = status;
        this.role = role;
        this.id = id;
        this.opening = opening;
        this.type = type;
    }

    public String getOpening() {
        return opening;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getRole() {
        return role;
    }

    public String getAddress() {
        return address;
    }

    public String getContact() {
        return contact;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }
}
