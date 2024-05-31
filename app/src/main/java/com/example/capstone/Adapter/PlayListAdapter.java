package com.example.capstone.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.capstone.Data.MusicListAdapterData;
import com.example.capstone.Interface.ItemTouchHelperListener;
import com.example.capstone.Interface.PlayListListener;
import com.example.capstone.R;
import com.example.capstone.Fragment2.PlayListFragment;

import java.util.ArrayList;

public class PlayListAdapter extends RecyclerView.Adapter<PlayListAdapter.ViewHolder> implements ItemTouchHelperListener {

    private final ArrayList<MusicListAdapterData> mList = new ArrayList<MusicListAdapterData>();
    private final ArrayList<String> list = new ArrayList<String>();
    PlayListListener playListListener;
    Context context;
    public int selectedItem = -1;

    public PlayListAdapter(Context context, PlayListListener playListListener){
        this.context = context;
        this.playListListener = playListListener;
    }

    public void addItem(int position, MusicListAdapterData item){
        mList.add(position, item);
        list.add(position, item.getSongUrl());
    }

    public void removeItem(int position){
        PlayListFragment.playListDB.deletePlayList_song(list.get(position));
        mList.remove(position);
        list.remove(position);
    }

    public void resetList(){
        mList.clear();
        list.clear();
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public int getNumber(String url){
        return list.indexOf(url);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.playlist_layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MusicListAdapterData item = mList.get(position);
        int pos = position;

        if(pos == selectedItem){
            holder.songName.setTextColor(Color.parseColor("#00B3EF"));
        } else {
            holder.songName.setTextColor(Color.WHITE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View v) {
                selectedItem = pos;
                playListListener.sendMessage(item.getSongUrl());
                notifyDataSetChanged();
            }
        });

        holder.playlist_etc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playListListener.sendEtc(item.getSongUrl());
            }
        });

        holder.setItem(item);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onItemSwipe(int position) {
        removeItem(position);
        notifyItemRemoved(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView songName;
        ImageView songThumbnail, playlist_etc;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            songName = itemView.findViewById(R.id.playlist_songName);
            songThumbnail = itemView.findViewById(R.id.playlist_songThumbnail);
            playlist_etc = itemView.findViewById(R.id.playlist_etc);

            songName.setSingleLine(true);    // 한줄로 표시하기
            songName.setEllipsize(TextUtils.TruncateAt.MARQUEE); // 흐르게 만들기
            songName.setSelected(true);      // 선택하기
        }

        public void setItem(MusicListAdapterData item){
            Glide.with(itemView).load(item.getImageUrl()).into(songThumbnail);
            songName.setText(item.getSongName());
        }
    }

}
