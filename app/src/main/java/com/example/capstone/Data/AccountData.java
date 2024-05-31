package com.example.capstone.Data;

public class AccountData {
    String email, password, nickname, imageUrl, info;

    public AccountData() {
        this.email = "";
        this.password = "";
        this.nickname = "";
        this.imageUrl = "";
        this.info = "";
    }

    public AccountData(String email, String password, String nickname, String imageUrl, String info) {
        this.email = email;
        this.password = password;
        this.imageUrl = imageUrl;
        this.nickname = nickname;
        this.info = info;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
