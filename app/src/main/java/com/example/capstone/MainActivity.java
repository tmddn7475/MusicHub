package com.example.capstone;

import android.app.Activity;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.capstone.Data.HistoryData;
import com.example.capstone.Data.PlayListDB;
import com.example.capstone.Service.MusicService;
import com.example.capstone.Fragment2.EtcFragment;
import com.example.capstone.Fragment2.PlayListFragment;
import com.example.capstone.Fragment1.FeedFragment;
import com.example.capstone.Library.LibraryFragment;
import com.example.capstone.Fragment1.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.snackbar.Snackbar;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.session.MediaController;
import androidx.media3.session.SessionToken;

import com.bumptech.glide.Glide;
import com.example.capstone.Interface.MusicListener;
import com.example.capstone.Data.MusicListAdapterData;
import com.example.capstone.Fragment1.HomeFragment;
import com.example.capstone.Fragment2.MediaFragment;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements MusicListener {

    ImageView bar_thumnail, bar_playlist_btn;
    TextView bar_song, bar_artist;
    BottomNavigationView bottomNavigationView;
    HomeFragment homeFragment;
    FeedFragment feedFragment;
    SearchFragment searchFragment;
    LibraryFragment libraryFragment;

    public static LinearProgressIndicator bar_progress;
    public static ImageView bar_play_btn;
    public static MediaFragment mediaFragment;
    public static PlayListFragment playListFragment;
    public static EtcFragment etcFragment;
    public static String current_url = "";

    private Handler handler;
    public static MediaController mediaController;
    ListenableFuture<MediaController> controllerFuture;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    PlayListDB playListDB;
    SQLiteDatabase database;

    @Override
    protected void onStart() {
        super.onStart();
        initializeMediaController();
        if(!Objects.equals(preferences.getString("url", null), "")){
            getMusic(preferences.getString("url", null));
            current_url = preferences.getString("url", null);
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void initializeMediaController() {
        SessionToken sessionToken = new SessionToken(this, new ComponentName(this, MusicService.class));
        controllerFuture = new MediaController.Builder(this, sessionToken).buildAsync();
        controllerFuture.addListener(() -> {
            try {
                mediaController = controllerFuture.get();
                if(!mediaController.isPlaying() && !Objects.equals(preferences.getString("url", null), "")){
                    ready_music(preferences.getString("url", null));
                }
                updateProgressIndicator();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, MoreExecutors.directExecutor());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler(Looper.getMainLooper());

        // Fragment
        homeFragment = new HomeFragment();
        mediaFragment = new MediaFragment();
        feedFragment = new FeedFragment();
        searchFragment = new SearchFragment();
        libraryFragment = new LibraryFragment();
        playListFragment = new PlayListFragment();
        etcFragment = new EtcFragment();

        // bottomNavigationView
        getSupportFragmentManager().beginTransaction().replace(R.id.container, homeFragment).commit();
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.bottom_home) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, homeFragment).commit();
                } else if (itemId == R.id.bottom_feed) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, feedFragment).commit();
                } else if (itemId == R.id.bottom_search) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, searchFragment).commit();
                } else if (itemId == R.id.bottom_library) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, libraryFragment).commit();
                }
                return true;
            }
        });

        bar_thumnail = findViewById(R.id.bar_song_thumnail);
        bar_song = findViewById(R.id.bar_song_name);
        bar_artist = findViewById(R.id.bar_song_artist);
        bar_progress = findViewById(R.id.media_bar_progress);
        bar_play_btn = findViewById(R.id.bar_play_pause_btn);
        bar_playlist_btn = findViewById(R.id.bar_playlist_btn);

        // 플레이리스트
        bar_playlist_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bar_song.getText().equals("")) {
                    playListFragment.setMediaController(mediaController);
                    playListFragment.show(getSupportFragmentManager(), playListFragment.getTag());
                }
            }
        });

        bar_play_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaController != null && !bar_song.getText().equals("")) {
                    if (mediaController.isPlaying()) {
                        mediaController.pause();
                        bar_play_btn.setImageResource(R.drawable.play_arrow);
                    } else {
                        mediaController.play();
                        bar_play_btn.setImageResource(R.drawable.pause);
                        updateProgressIndicator();
                    }
                }
            }
        });

        bar_song.setSingleLine(true);    // 한줄로 표시하기
        bar_song.setEllipsize(TextUtils.TruncateAt.MARQUEE); // 흐르게 만들기
        bar_song.setSelected(true);

        // 뮤직 바 클릭 시 프래그먼트 나오게 함
        FrameLayout frameLayout = findViewById(R.id.media_player_bar_bg);
        frameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bar_song.getText().equals("")) {
                    mediaFragment.setMediaController(mediaController);
                    mediaFragment.show(getSupportFragmentManager(), mediaFragment.getTag());
                }
            }
        });

        // 듣고 있던 곡 저장
        preferences = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        editor = preferences.edit();
        // preferences 초기값
        String current = preferences.getString("url", "");
        editor.putString("url", current);
        editor.apply(); // 저장

        playListDB = new PlayListDB(MainActivity.this);
        database = playListDB.getWritableDatabase();

        // 뒤로 가기 누를 때
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    @Override
    public void sendMessage(String message) {
        bar_progress.setProgress(0);
        getMusic(message);
        ready_music(message);
        putHistory(message);
        if(mediaController != null){
            mediaController.play();
        }
    }

    @Override
    public void nextMessage() {
        nextMusic();
    }

    @Override
    public void previousMessage() {
        previousMusic();
    }

    public void getMusic(String url){
        String sql = "select * from playlist where songUrl = '" + url + "'";
        Cursor cursor = database.rawQuery(sql, null);

        editor.putString("url", url);
        editor.apply();

        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference("Songs");
        Query query = mReference.orderByChild("songUrl").equalTo(url).limitToFirst(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    MusicListAdapterData mld = ds.getValue(MusicListAdapterData.class);
                    assert mld != null;
                    if(!MainActivity.this.isFinishing()){
                        Glide.with(MainActivity.this).load(mld.getImageUrl()).into(bar_thumnail);
                    }
                    bar_song.setText(mld.getSongName());
                    bar_artist.setText(mld.getSongArtist());

                    if(cursor.getCount() < 1){
                        playListDB.addPlaylist_song(mld, Static_command.getTime2());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public void ready_music(String url){
        if(mediaController != null) {
            FirebaseDatabase.getInstance().getReference("Songs").orderByChild("songUrl")
                    .equalTo(url).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                MusicListAdapterData mld = ds.getValue(MusicListAdapterData.class);
                                assert mld != null;

                                MediaItem item =
                                        new MediaItem.Builder()
                                                .setMediaId("media-1")
                                                .setUri(mld.getSongUrl())
                                                .setMediaMetadata(
                                                        new MediaMetadata.Builder()
                                                                .setArtist(mld.getSongArtist())
                                                                .setTitle(mld.getSongName())
                                                                .setArtworkUri(Uri.parse(mld.getImageUrl()))
                                                                .build())
                                                .build();
                                mediaController.setMediaItem(item);
                                mediaController.prepare();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
        }
    }

    private void check_play_state(){
        if(mediaController != null && mediaController.isPlaying()){
            bar_play_btn.setImageResource(R.drawable.pause);
        } else if (mediaController != null && !mediaController.isPlaying()) {
            bar_play_btn.setImageResource(R.drawable.play_arrow);
        }
    }

    // progressbar 진행
    private void updateProgressIndicator() {
        if (mediaController != null && mediaController.isPlaying()) {
            bar_progress.setMax((int) mediaController.getDuration());
            bar_progress.setProgress((int) mediaController.getCurrentPosition());

            if(!current_url.equals(preferences.getString("url", null))){
                getMusic(preferences.getString("url", null));
                current_url = preferences.getString("url", null);
                if(mediaFragment.isAdded()){
                    mediaFragment.setUpUi(current_url);
                }
                if(playListFragment.isAdded()){
                    playListFragment.setUpCurrent(current_url);
                }
            }
            if(mediaFragment.isAdded()){
                MediaFragment.media_seekbar.setMax((int) mediaController.getDuration());
                MediaFragment.media_seekbar.setProgress((int) mediaController.getCurrentPosition());
                updatePlayingTime();
            }
            if(playListFragment.isAdded()){
                PlayListFragment.playlist_progress.setMax((int) mediaController.getDuration());
                PlayListFragment.playlist_progress.setProgress((int) mediaController.getCurrentPosition());
            }
        }
        check_play_state();
        handler.postDelayed(this::updateProgressIndicator, 330);
    }

    // 현재 노래 시간 확인
    private void updatePlayingTime() {
        if (mediaController != null) {
            int millis = (int) mediaController.getCurrentPosition();
            long total_secs = TimeUnit.SECONDS.convert(millis, TimeUnit.MILLISECONDS);
            long mins = TimeUnit.MINUTES.convert(total_secs, TimeUnit.SECONDS);
            long secs = total_secs - (mins * 60);

            if (secs < 10) {
                MediaFragment.media_song_current.setText(mins + ":0" + secs);
            } else {
                MediaFragment.media_song_current.setText(mins + ":" + secs);
            }
        }
    }

    // 다음 곡
    private void nextMusic(){
        ArrayList<String> arr = new ArrayList<>();
        String sql = "select * from playlist";

        Cursor cursor = database.rawQuery(sql, null);
        while(cursor.moveToNext()){
            arr.add(0, cursor.getString(1));
        }
        int num = arr.indexOf(preferences.getString("url", null));

        if(cursor.getCount() > 0){
            if(num < arr.size() - 1){
                ready_music(arr.get(num + 1));
                mediaController.play();
                editor.putString("url", arr.get(num + 1));
                editor.apply();
            } else {
                ready_music(arr.get(0));
                mediaController.play();
                editor.putString("url", arr.get(0));
                editor.apply();
            }
        } else {
            ready_music(preferences.getString("url", null));
            mediaController.play();
            editor.putString("url", preferences.getString("url", null));
            editor.apply();
        }
    }

    // 전 곡
    private void previousMusic(){
        ArrayList<String> arr = new ArrayList<>();
        String sql = "select * from playlist";

        Cursor cursor = database.rawQuery(sql, null);
        while(cursor.moveToNext()){
            arr.add(0, cursor.getString(1));
        }
        int num = arr.indexOf(preferences.getString("url", null));

        if(cursor.getCount() > 0){
            if(num > 0){
                ready_music(arr.get(num - 1));
                mediaController.play();
                editor.putString("url", arr.get(num - 1));
                editor.apply();
            } else {
                ready_music(arr.get(arr.size() - 1));
                mediaController.play();
                editor.putString("url", arr.get(arr.size() - 1));
                editor.apply();
            }
        } else {
            ready_music(preferences.getString("url", null));
            mediaController.play();
        }
    }

    // 음악 기록에 저장
    private void putHistory(String url){
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        HistoryData historyData = new HistoryData(url, email);
        FirebaseDatabase.getInstance().getReference("History").push().setValue(historyData);
    }

    // 뒤로 가기 누를 때
    private long backpressedTime = 0;
    private final OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
            if(fragment instanceof HomeFragment){
                if (System.currentTimeMillis() > backpressedTime + 2000) {
                    backpressedTime = System.currentTimeMillis();
                    Snackbar.make(findViewById(R.id.main),"뒤로 버튼을 한번 더 누르면 종료됩니다.", Snackbar.LENGTH_SHORT)
                            .setBackgroundTint(Color.DKGRAY).setTextColor(Color.WHITE).setAnchorView(findViewById(R.id.include)).show();
                } else if (System.currentTimeMillis() <= backpressedTime + 2000) {
                    finish();
                }
            } else if (fragment instanceof FeedFragment || fragment instanceof SearchFragment || fragment instanceof LibraryFragment) {
                getSupportFragmentManager().beginTransaction().replace(R.id.container, homeFragment).commit();
                bottomNavigationView.setSelectedItemId(R.id.bottom_home);
            } else {
                assert fragment != null;
                getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                getSupportFragmentManager().popBackStack();
            }
        }
    };
}


