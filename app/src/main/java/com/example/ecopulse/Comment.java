package com.example.ecopulse;

public class Comment {
    private String content;
    //private String userID;

    public Comment() {}
    public Comment(String content) {    //add userID
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    //userID getter and setter
}
