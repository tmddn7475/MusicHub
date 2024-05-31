package com.example.capstone.Fragment1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.capstone.Account.AccountFragment;
import com.example.capstone.Data.MusicListAdapterData;
import com.example.capstone.MainActivity;
import com.example.capstone.R;
import com.example.capstone.Activity.SongEditActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class SongInfoFragment extends Fragment {

    private final int ONE_DAY = 24 * 60 * 60 * 1000;
    ImageView info_song_thumnail, info_back_btn, info_edit_btn;
    TextView info_song_name, info_song_artist, info_song_duration, info_song, info_song_play, info_song_like, info_song_time;
    String email, url;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_song_info, container, false);

        info_song_thumnail = v.findViewById(R.id.info_song_thumnail);
        info_song_name = v.findViewById(R.id.info_song_name);
        info_song_artist = v.findViewById(R.id.info_song_artist);
        info_song_duration = v.findViewById(R.id.info_song_duration);
        info_song = v.findViewById(R.id.info_song);
        info_song_play = v.findViewById(R.id.info_song_play);
        info_song_like = v.findViewById(R.id.info_song_like);
        info_song_time = v.findViewById(R.id.info_song_time);
        info_back_btn = v.findViewById(R.id.info_back_btn);
        info_edit_btn = v.findViewById(R.id.info_edit_btn);

        info_song_name.setSingleLine(true);    // 한줄로 표시하기
        info_song_name.setEllipsize(TextUtils.TruncateAt.MARQUEE); // 흐르게 만들기
        info_song_name.setSelected(true);      // 선택하기

        assert getArguments() != null;
        getData(getArguments().getString("url"));

        // 뒤로 가기
        info_back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });

        // 아티스트 창
        info_song_artist.setPaintFlags(info_song_artist.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG); // text 밑줄 추가
        info_song_artist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) getContext();
                assert mainActivity != null;
                FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
                AccountFragment accountFragment = new AccountFragment();

                Bundle bundle = new Bundle();
                bundle.putString("email", email);
                accountFragment.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.container, accountFragment).addToBackStack(null).commit();
            }
        });

        // 곡 정보 수정
        info_edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), SongEditActivity.class);
                intent.putExtra("url", url);
                startActivity(intent);
            }
        });

        return v;
    }

    private void getData(String song){
        // 조회수
        DatabaseReference mReference2 = FirebaseDatabase.getInstance().getReference("History");
        Query query2 = mReference2.orderByChild("songUrl").equalTo(song);
        query2.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint({"DefaultLocale", "SetTextI18n"})
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double num = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    num = num + 1;
                }

                if(num < 1){
                    info_song_play.setText("0");
                } else {
                    String a = String.format("%.0f", num);
                    info_song_play.setText(a);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // 좋아요
        DatabaseReference mReference3 = FirebaseDatabase.getInstance().getReference("Like");
        Query query3 = mReference3.orderByChild("songUrl").equalTo(song);
        query3.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint({"DefaultLocale", "SetTextI18n"})
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double num = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    num = num + 1;
                }

                if(num < 1){
                    info_song_like.setText("0");
                } else {
                    String a = String.format("%.0f", num);
                    info_song_like.setText(a);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // 노래 정보
        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference("Songs");
        Query query = mReference.orderByChild("songUrl").equalTo(song).limitToFirst(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    MusicListAdapterData data = ds.getValue(MusicListAdapterData.class);
                    assert data != null;

                    email = data.getEmail();
                    url = data.getSongUrl();

                    info_song_name.setText(data.getSongName());
                    info_song_artist.setText(data.getSongArtist());
                    info_song_duration.setText(" · " + data.getSongDuration());
                    info_song.setText(data.getSongInfo());
                    Glide.with(getActivity()).load(data.getImageUrl()).into(info_song_thumnail);
                    get_dDay(data.getTime(), data.getSongCategory());

                    if(data.getEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())){
                        info_edit_btn.setVisibility(View.VISIBLE);
                    } else {
                        info_edit_btn.setVisibility(View.GONE);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void get_dDay(String time, String category){
        String[] a = time.split("/");

        final Calendar dDayCalendar = Calendar.getInstance();
        // 입력 받은 날짜로 설정한다
        dDayCalendar.set(Integer.parseInt(a[0]), Integer.parseInt(a[1]) - 1, Integer.parseInt(a[2]));

        // millisecond 으로 환산한 뒤 입력한 날짜에서 현재 날짜의 차를 구한다
        final long dDay = dDayCalendar.getTimeInMillis() / ONE_DAY;
        final long today = Calendar.getInstance().getTimeInMillis() / ONE_DAY;
        long result = today - dDay;
        final String goalDate;

        if(result <= 1){
            goalDate = category + " · " + result + " day ago";
            info_song_time.setText(goalDate);
        } else if (result <= 30) {
            goalDate = category + " · " + result + " days ago";
            info_song_time.setText(goalDate);
        } else if (result <= 365) {
            result = result / 30;
            if(result <= 1){
                goalDate = category + " · " + result + " month ago";
                info_song_time.setText(goalDate);
            } else {
                goalDate = category + " · " + result + " months ago";
                info_song_time.setText(goalDate);
            }
        } else {
            result = result / 365;
            if(result <= 1){
                goalDate = category + " · " + result + " year ago";
                info_song_time.setText(goalDate);
            } else {
                goalDate = category + " · " + result + " years ago";
                info_song_time.setText(goalDate);
            }
        }
    }
    private void back(){
        MainActivity mainActivity = (MainActivity) getContext();
        assert mainActivity != null;
        FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
        fragmentManager.beginTransaction().remove(this).commit();
        fragmentManager.popBackStack();
    }
}