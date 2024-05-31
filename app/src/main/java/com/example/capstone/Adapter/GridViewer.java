package com.example.capstone.Adapter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.capstone.Data.MyAlbumData;
import com.example.capstone.R;

public class GridViewer extends LinearLayout {

    TextView grid_name, grid_artist;
    ImageView grid_image;

    public GridViewer(Context context) {
        super(context);
        init(context);
    }

    public GridViewer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.grid_item,this,true);

        grid_name = (TextView)findViewById(R.id.grid_name);
        grid_artist = (TextView)findViewById(R.id.grid_artist);
        grid_image = (ImageView) findViewById(R.id.grid_image);
    }

    public void setItem(MyAlbumData data){
        Glide.with(getContext()).load(data.getImageUrl()).into(grid_image);
        grid_name.setText(data.getListName());
        grid_artist.setText(data.getNickname());
    }
}
