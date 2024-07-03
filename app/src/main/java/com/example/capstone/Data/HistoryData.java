package com.example.capstone.Data;

public class HistoryData {
    String email, songUrl, time;

    public HistoryData() {
        this.songUrl = "";
        this.email = "";
        this.time = "";
    }

    public HistoryData(String songUrl, String email, String time) {
        this.songUrl = songUrl;
        this.email = email;
        this.time = time;
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
