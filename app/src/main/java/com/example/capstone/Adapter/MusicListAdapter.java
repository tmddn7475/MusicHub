package com.example.capstone.Adapter;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.example.capstone.Data.MusicListAdapterData;
import com.example.capstone.Fragment1.SongInfoFragment;
import com.example.capstone.Interface.MusicListListener;
import com.example.capstone.MainActivity;
import com.example.capstone.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MusicListAdapter extends BaseAdapter implements Filterable {

    public ArrayList<MusicListAdapterData> list = new ArrayList<MusicListAdapterData>();
    public ArrayList<MusicListAdapterData> filterList = list;
    Filter listFilter;
    MusicListListener musicListListener;
    Context context;

    public MusicListAdapter(Context context, MusicListListener musicListListener){
        this.context = context;
        this.musicListListener = musicListListener;
    }

    @Override
    public int getCount() {
        return filterList.size();
    }

    @Override
    public Object getItem(int position) {
        return filterList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final Context context = parent.getContext();
        // exercise_listview를 inflate 해서 view를 참조한다
        if(view == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.songs_list_layout, parent, false);
        }

        TextView songDuration, artistName, songName;
        ImageView songThumbnail, song_etc;

        songName = view.findViewById(R.id.playlist_songName);
        artistName = view.findViewById(R.id.playlist_artistName);
        songDuration = view.findViewById(R.id.playlist_songDuration);
        songThumbnail = view.findViewById(R.id.playlist_songThumbnail);
        song_etc = view.findViewById(R.id.song_etc);

        MusicListAdapterData listData = filterList.get(position);

        songName.setText(listData.getSongName());
        artistName.setText(listData.getSongArtist());
        songDuration.setText(listData.getSongDuration());
        Glide.with(view).load(listData.getImageUrl()).into(songThumbnail);

        songThumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) context;
                assert mainActivity != null;
                FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
                SongInfoFragment songInfoFragment = new SongInfoFragment();

                Bundle bundle = new Bundle();
                bundle.putString("url", listData.getSongUrl());
                songInfoFragment.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.container, songInfoFragment).addToBackStack(null).commit();
            }
        });

        song_etc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicListListener.sendEtc(listData.getSongUrl());
            }
        });

        songName.setSingleLine(true);    // 한줄로 표시하기
        songName.setEllipsize(TextUtils.TruncateAt.MARQUEE); // 흐르게 만들기
        songName.setSelected(true);      // 선택하기

        return view;
    }

    public void resetList(){
        list.clear();
    }

    public void addItemToList(MusicListAdapterData listData){
        list.add(listData);
    }

    public void addItemToList2(MusicListAdapterData listData){
        list.add(0, listData);
    }

    public void addItemToList3(MusicListAdapterData listData, String time){
        listData.setTime(time);
        list.add(listData);
    }

    public void sort(){
        Comparator<MusicListAdapterData> comparator = (prod1, prod2) -> prod1.getTime().compareTo(prod2.getTime());
        list.sort(comparator.reversed());
    }

    @Override
    public Filter getFilter() {
        if(listFilter == null) {
            listFilter = new ListFilter();
        }
        return listFilter;
    }

    private class ListFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint == null || constraint.length() == 0) {
                results.values = list;
                results.count = list.size();
            } else {
                ArrayList<MusicListAdapterData> itemList = new ArrayList<MusicListAdapterData>() ;
                for (MusicListAdapterData item : list) {
                    if (item.getSongName().toLowerCase().contains(constraint.toString().toLowerCase()))
                    {
                        // 검색창에 입력된 이름과 같은 음식 이름만 리스트에 나오게 한다.
                        itemList.add(item);
                    }
                }
                results.values = itemList;
                results.count = itemList.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filterList = (ArrayList<MusicListAdapterData>) results.values;

            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}
