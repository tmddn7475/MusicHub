package com.example.capstone.Data;

public class AlbumToSongData {
    String key, songUrl, key_songUrl;

    public AlbumToSongData() {
        this.songUrl = "";
        this.key = "";
        this.key_songUrl = "";
    }

    public AlbumToSongData(String key, String songUrl) {
        this.key = key;
        this.songUrl = songUrl;
        this.key_songUrl = key + "_" + songUrl;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSongUrl() {
        return songUrl;
    }

    public void setSongUrl(String songUrl) {
        this.songUrl = songUrl;
    }

    public String getKey_songUrl() {
        return key_songUrl;
    }

    public void setKey_songUrl(String key_songUrl) {
        this.key_songUrl = key_songUrl;
    }
}
