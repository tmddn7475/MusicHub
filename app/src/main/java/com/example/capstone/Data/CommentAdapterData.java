package com.example.capstone.Data;

public class CommentAdapterData {
    String email, nickname, imageUrl, comment, time, songUrl;

    public CommentAdapterData() {
        this.email = "";
        this.nickname = "";
        this.imageUrl = "";
        this.comment = "";
        this.time = "";
        this.songUrl = "";
    }

    public CommentAdapterData(String email, String nickname, String imageUrl, String comment, String time, String songUrl) {
        this.email = email;
        this.imageUrl = imageUrl;
        this.nickname = nickname;
        this.comment = comment;
        this.time = time;
        this.songUrl = songUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSongUrl() {
        return songUrl;
    }

    public void setSongUrl(String songUrl) {
        this.songUrl = songUrl;
    }
}

