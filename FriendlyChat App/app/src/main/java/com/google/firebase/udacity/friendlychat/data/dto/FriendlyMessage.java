package com.google.firebase.udacity.friendlychat.data.dto;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;

public class FriendlyMessage {

    private String text;
    private String name;
    private String photoUrl;
    private Timestamp timestamp;

    public FriendlyMessage() {
    }

    public FriendlyMessage(String text, String name, String photoUrl, Timestamp timestamp) {
        this.text = text;
        this.name = name;
        this.photoUrl = photoUrl;
        this.timestamp = timestamp;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @NonNull
    @Override
    public String toString() {
        return "text: " + text +
                " name: " + name +
                " photoUrl: " + photoUrl +
                " timestamp: " + timestamp;
    }
}
