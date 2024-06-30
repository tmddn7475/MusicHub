package com.example.capstone;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.capstone.Data.AlbumToSongData;
import com.example.capstone.Data.FollowData;
import com.example.capstone.Data.LikeData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Static_command {
    // 좋아요
    public static void checkLike(String url) {
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        LikeData likeData = new LikeData(email, url);
        FirebaseDatabase.getInstance().getReference("Like").push().setValue(likeData);
    }

    public static void checkUnlike(String key){
        FirebaseDatabase.getInstance().getReference("Like").child(key).removeValue();
    }
    
    // 팔로우
    public static void checkFollow(String follow){
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        FollowData followData = new FollowData(email, follow);
        FirebaseDatabase.getInstance().getReference("Follow").push().setValue(followData);
    }

    public static void checkUnfollow(String key){
        FirebaseDatabase.getInstance().getReference("Follow").child(key).removeValue();
    }

    // 리스트에 곡 추가
    public static void putTrack(String key, String url){
        FirebaseDatabase.getInstance().getReference("PlayLists_song").orderByChild("key_songUrl")
                .equalTo(key + "_" + url).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getChildren() != null && snapshot.getChildren().iterator().hasNext()){
                            // exist
                            Log.i("album_exist", "already exist");
                        } else {
                            // not exist
                            AlbumToSongData albumToSongData = new AlbumToSongData(key, url, getTime2());
                            FirebaseDatabase.getInstance().getReference("PlayLists_song").push().setValue(albumToSongData);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    // 오늘 날짜 가져오기
    public static String getTime(){
        long mNow;
        Date mDate;
        SimpleDateFormat mFormat = new SimpleDateFormat("yyyy/MM/dd");

        mNow = System.currentTimeMillis();
        mDate = new Date(mNow);
        return mFormat.format(mDate);
    }

    public static String getTime2(){
        long mNow;
        Date mDate;
        SimpleDateFormat mFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        mNow = System.currentTimeMillis();
        mDate = new Date(mNow);
        return mFormat.format(mDate);
    }

    // 인터넷 연결 확인
    public static int Get_Internet(Context context)
    {
        int state = 0;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = cm.getActiveNetwork();
        NetworkCapabilities activeNetwork = cm.getNetworkCapabilities(network);
        if(activeNetwork == null){
            return 0;
        }
        if(activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)){
            state = 1;
        }
        return state;
    }
}
