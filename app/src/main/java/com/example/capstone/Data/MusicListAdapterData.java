package com.example.capstone.Data;

public class MusicListAdapterData {
    String songName, songUrl, imageUrl, songArtist, email, songInfo, songDuration, songCategory, time;

    public MusicListAdapterData() {
        this.songName = "";
        this.songUrl = "";
        this.imageUrl = "";
        this.email = "";
        this.songInfo = "";
        this.songArtist = "";
        this.songDuration = "";
        this.songCategory = "";
        this.time = "";
    }

    public MusicListAdapterData(String songName, String songUrl, String imageUrl, String songArtist,
                                String email, String songInfo, String songDuration, String songCategory, String time) {
        this.songName = songName;
        this.songUrl = songUrl;
        this.imageUrl = imageUrl;
        this.email = email;
        this.songInfo = songInfo;
        this.songArtist = songArtist;
        this.songDuration = songDuration;
        this.songCategory = songCategory;
        this.time = time;
    }


    public String getSongArtist() {
        return songArtist;
    }

    public void setSongArtist(String songArtist) {
        this.songArtist = songArtist;
    }

    public String getSongDuration() {
        return songDuration;
    }

    public void setSongDuration(String songDuration) {
        this.songDuration = songDuration;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSongInfo() {
        return songInfo;
    }

    public void setSongInfo(String songInfo) {
        this.songInfo = songInfo;
    }

    public String getSongUrl() {
        return songUrl;
    }

    public void setSongUrl(String songUrl) {
        this.songUrl = songUrl;
    }

    public String getSongCategory() {
        return songCategory;
    }

    public void setSongCategory(String songCategory) {
        this.songCategory = songCategory;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
