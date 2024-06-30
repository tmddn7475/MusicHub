package com.example.capstone.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestOptions;
import com.example.capstone.Data.MusicListAdapterData;
import com.example.capstone.Interface.MusicListener;
import com.example.capstone.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class FeedListAdapter extends BaseAdapter {
    private final int ONE_DAY = 24 * 60 * 60 * 1000;
    public ArrayList<MusicListAdapterData> list = new ArrayList<MusicListAdapterData>();
    Context context;
    MusicListener musicListener;
    public FeedListAdapter(Context context, MusicListener musicListener){
        this.context = context;
        this.musicListener = musicListener;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public String getUrl(int position){
        return list.get(position).getSongUrl();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final Context context = parent.getContext();
        // exercise_listview를 inflate 해서 view를 참조한다
        if(view == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.feed_list_layout, parent, false);
        }

        TextView feed_post, feed_song_name, feed_song_duration;
        ImageView feed_song_image, feed_play_btn, feed_blur_image;

        feed_blur_image = view.findViewById(R.id.feed_blur_image);
        feed_song_image = view.findViewById(R.id.feed_song_image);
        feed_play_btn = view.findViewById(R.id.feed_play_btn);
        feed_post = view.findViewById(R.id.feed_post);
        feed_song_name = view.findViewById(R.id.feed_song_name);
        feed_song_duration = view.findViewById(R.id.feed_song_duration);

        MusicListAdapterData listData = list.get(position);

        feed_post.setText(get_dDay(listData.getTime(), listData.getSongArtist()));
        feed_song_name.setText(listData.getSongName());
        feed_song_duration.setText(listData.getSongDuration());
        Glide.with(view).load(listData.getImageUrl()).into(feed_song_image);
        Glide.with(view).load(listData.getImageUrl()) // 이미지 블러처리
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(25, 3))).into(feed_blur_image);

        feed_play_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicListener.sendMessage(listData.getSongUrl());
            }
        });

        feed_post.setSingleLine(true);    // 한줄로 표시하기
        feed_post.setEllipsize(TextUtils.TruncateAt.MARQUEE); // 흐르게 만들기
        feed_post.setSelected(true);
        feed_song_name.setSingleLine(true);    // 한줄로 표시하기
        feed_song_name.setEllipsize(TextUtils.TruncateAt.MARQUEE); // 흐르게 만들기
        feed_song_name.setSelected(true);      // 선택하기

        return view;
    }

    public void resetList(){
        list.clear();
    }

    public void addItemToList(MusicListAdapterData listData){
        list.add(listData);
    }

    public void sort(){
        Comparator<MusicListAdapterData> comparator = (prod1, prod2) -> prod1.getTime().compareTo(prod2.getTime());
        list.sort(comparator.reversed());
    }
    private String get_dDay(String time, String artist){
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
            goalDate = artist + " posted a track " + result + " day ago";
        } else if (result <= 30) {
            goalDate = artist + " posted a track " + result + " days ago";
        } else if (result <= 365) {
            result = result / 30;
            if(result <= 1){
                goalDate = artist + " posted a track " + result + " month ago";
            } else {
                goalDate = artist + " posted a track " + result + " months ago";
            }
        } else {
            result = result / 365;
            if(result <= 1){
                goalDate = artist + " posted a track " + result + " year ago";
            } else {
                goalDate = artist + " posted a track " + result + " years ago";
            }
        }

        return goalDate;
    }
}
