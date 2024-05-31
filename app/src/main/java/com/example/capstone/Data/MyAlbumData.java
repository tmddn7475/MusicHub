package com.example.capstone.Data;

public class MyAlbumData {
    String email, listName, list_mode, nickname, description, imageUrl;

    public MyAlbumData() {
        this.listName = "";
        this.email = "";
        this.list_mode = "";
        this.nickname = "";
        this.description = "";
        this.imageUrl = "";
    }

    public MyAlbumData(String email, String listName, String list_mode, String nickname, String description, String imageUrl) {
        this.email = email;
        this.listName = listName;
        this.list_mode = list_mode;
        this.nickname = nickname;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getList_mode() {
        return list_mode;
    }

    public void setList_mode(String list_mode) {
        this.list_mode = list_mode;
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}

