package com.example.capstone.Data;

public class HistoryData {
    String email, songUrl;

    public HistoryData() {
        this.songUrl = "";
        this.email = "";
    }

    public HistoryData(String songUrl, String email) {
        this.songUrl = songUrl;
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSongUrl() {
        return songUrl;
    }

    public void setSongUrl(String songUrl) {
        this.songUrl = songUrl;
    }

}
