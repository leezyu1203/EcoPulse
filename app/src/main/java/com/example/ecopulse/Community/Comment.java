package com.example.ecopulse.Community;

public class Comment {
    private String content;
    private String userID;

    public Comment() {}
    public Comment(String content, String userID) {
        this.content = content;
        this.userID = userID;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
