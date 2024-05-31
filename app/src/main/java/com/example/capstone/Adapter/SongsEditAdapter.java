package com.example.capstone.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.capstone.Data.MusicListAdapterData;
import com.example.capstone.R;

import java.util.ArrayList;

public class SongsEditAdapter extends BaseAdapter {
    public ArrayList<MusicListAdapterData> list = new ArrayList<MusicListAdapterData>();

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

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        final Context context = viewGroup.getContext();

        // exercise_listview를 inflate 해서 view를 참조한다
        if(view == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.songs_edit_list_layout, viewGroup, false);
        }
        TextView playlist_songName = view.findViewById(R.id.playlist_songName);
        TextView playlist_artistName = view.findViewById(R.id.playlist_artistName);
        ImageView playlist_songThumbnail = view.findViewById(R.id.playlist_songThumbnail);

        MusicListAdapterData listData = list.get(position);

        playlist_songName.setText(listData.getSongName());
        playlist_artistName.setText(listData.getSongArtist());
        Glide.with(view).load(listData.getImageUrl()).into(playlist_songThumbnail);

        playlist_songName.setSingleLine(true);    // 한줄로 표시하기
        playlist_songName.setEllipsize(TextUtils.TruncateAt.MARQUEE); // 흐르게 만들기
        playlist_songName.setSelected(true);      // 선택하기

        return view;
    }

    public void addItemToList(MusicListAdapterData data){
        list.add(data);
    }

    public String existList(){
        String url = "";
        for(int i = 0; i < list.size(); i++){
            MusicListAdapterData data = list.get(i);
            url += data.getSongUrl();
        }

        return url;
    }
}
