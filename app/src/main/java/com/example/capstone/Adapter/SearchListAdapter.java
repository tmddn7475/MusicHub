package com.example.capstone.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.capstone.Data.PlayListDB;
import com.example.capstone.R;

import java.util.ArrayList;

public class SearchListAdapter extends BaseAdapter {
    public ArrayList<String> list = new ArrayList<String>();
    PlayListDB playListDB;

    public SearchListAdapter(PlayListDB playListDB){
        this.playListDB = playListDB;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    public String getStr(int position){
        return list.get(position);
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
            view = inflater.inflate(R.layout.search_recent_layout, parent, false);
        }

        TextView recent_text = view.findViewById(R.id.recent_text);
        ImageView recent_delete = view.findViewById(R.id.recent_delete);

        recent_text.setText(list.get(position));

        recent_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playListDB.deleteSearch(list.get(position));
                list.remove(position);
                notifyDataSetChanged();
            }
        });

        return view;
    }

    public void resetList(){
        list.clear();
    }

    public void addItemToList(String search){
        list.add(0, search);
    }
}