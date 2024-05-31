package com.example.capstone.Data;

public class LikeData {
    String email, songUrl, email_songUrl;

    public LikeData() {
        this.songUrl = "";
        this.email = "";
        this.email_songUrl = "";
    }

    public LikeData(String email, String songUrl) {
        this.email = email;
        this.songUrl = songUrl;
        this.email_songUrl = email + "_" + songUrl;
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

    public String getEmail_songUrl() {
        return email_songUrl;
    }

    public void setEmail_songUrl(String email_songUrl) {
        this.email_songUrl = email_songUrl;
    }
}
