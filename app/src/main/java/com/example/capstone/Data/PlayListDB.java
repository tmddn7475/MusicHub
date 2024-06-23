package com.example.capstone.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PlayListDB extends SQLiteOpenHelper {

    public PlayListDB(Context context) {
        super(context, "playlist.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table playlist (songName text not null, songUrl text not null primary key);";
        db.execSQL(sql);

        String sql2 = "create table search (searchText text not null);";
        db.execSQL(sql2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS playlist;");
        onCreate(db);
    }

    public void addPlaylist_song(MusicListAdapterData data){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("songName", data.getSongName());
        contentValues.put("songUrl", data.getSongUrl());
        sqLiteDatabase.insert("playlist", null, contentValues);
    }
    public void addSearch(String str){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("searchText", str);
        sqLiteDatabase.insert("search", null, contentValues);
    }

    public void deleteSearch(String str){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from search where searchText = '" + str + "';");
    }
    public void deletePlayList_song(String url){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from playlist where songUrl = '" + url + "';");
    }
    public void deleteAll(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from playlist");
        db.execSQL("delete from search");
    }
}
