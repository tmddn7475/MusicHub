package com.example.capstone.Service;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;

import com.example.capstone.Data.MusicListAdapterData;
import com.example.capstone.Data.PlayListDB;
import com.example.capstone.MainActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class MusicService extends MediaSessionService {
    private MediaSession mediaSession = null;
    private ExoPlayer player;
    PlayListDB playListDB;
    SQLiteDatabase database;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    DatabaseReference databaseReference;

    @Override
    public void onCreate() {
        super.onCreate();

        playListDB = new PlayListDB(this);
        database = playListDB.getReadableDatabase();
        preferences = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        editor = preferences.edit();
        databaseReference = FirebaseDatabase.getInstance().getReference("Songs");

        player = new ExoPlayer.Builder(this)
                .setAudioAttributes(AudioAttributes.DEFAULT, true)
                .setHandleAudioBecomingNoisy(true)
                .build();
        mediaSession = new MediaSession.Builder(this, player)
                .setSessionActivity(openMainActivityPendingIntent()).build();

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_ENDED) {
                    nextSong();
                }
                Player.Listener.super.onPlaybackStateChanged(playbackState);
            }
        });
    }

    @Nullable
    @Override
    public ComponentName startForegroundService(Intent service) {
        return super.startForegroundService(service);
    }

    @Override
    public void onUpdateNotification(@NonNull MediaSession session, boolean startInForegroundRequired) {
        super.onUpdateNotification(session, true);
    }

    @Override
    public MediaSession onGetSession(@NonNull MediaSession.ControllerInfo controllerInfo) {
        return mediaSession;
    }

    @Override
    public void onTaskRemoved(@Nullable Intent rootIntent) {
        Player player = mediaSession.getPlayer();
        if (!player.getPlayWhenReady()
                || player.getPlaybackState() == Player.STATE_ENDED) {
            stopSelf();
        }
    }

    private PendingIntent openMainActivityPendingIntent() {
        Intent notifyIntent = new Intent(this, MainActivity.class);
        // set the Activity to start in a new, empty task
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // create the PendingIntent
        return PendingIntent.getActivity(
                this, 0, notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    @Override
    public void onDestroy() {
        mediaSession.getPlayer().release();
        mediaSession.release();
        mediaSession = null;
        super.onDestroy();
    }

    private void nextSong() {
        ArrayList<String> arr = new ArrayList<>();
        String sql = "select * from playlist";

        Cursor cursor = database.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            arr.add(0, cursor.getString(1));
        }
        int num = arr.indexOf(preferences.getString("url", null));

        if(cursor.getCount() > 0){
            if (num < arr.size() - 1) {
                playMusic(arr.get(num + 1));
                editor.putString("url", arr.get(num + 1));
                editor.apply();
            } else {
                playMusic(arr.get(0));
                editor.putString("url", arr.get(0));
                editor.apply();
            }
        } else {
            playMusic(preferences.getString("url", null));
            editor.putString("url", preferences.getString("url", null));
            editor.apply();
        }
    }

    private void playMusic(String url) {
        databaseReference.orderByChild("songUrl").equalTo(url).limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            MusicListAdapterData mld = ds.getValue(MusicListAdapterData.class);
                            assert mld != null;
                            MediaItem item =
                                    new MediaItem.Builder().setMediaId("MusicHub").setUri(mld.getSongUrl())
                                            .setMediaMetadata(new MediaMetadata.Builder()
                                                    .setArtist(mld.getSongArtist())
                                                    .setTitle(mld.getSongName())
                                                    .setArtworkUri(Uri.parse(mld.getImageUrl()))
                                                    .build())
                                            .build();
                            player.setMediaItem(item);
                            player.prepare();
                            player.play();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }
}