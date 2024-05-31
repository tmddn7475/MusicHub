package com.example.capstone.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.example.capstone.Data.MyAlbumData;
import com.example.capstone.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class MyAlbumAdapter extends BaseAdapter {
    private final ArrayList<MyAlbumData> list = new ArrayList<MyAlbumData>();
    private final ArrayList<String> key_list = new ArrayList<String>();

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
    public View getView(int position, View view, ViewGroup parent) {
        final Context context = parent.getContext();

        // food_listview를 inflate 해서 view를 참조한다
        if(view == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.album_layout, parent, false);
        }

        ImageView my_list_lock, my_list_delete, my_list_thumnail;
        TextView my_list_name;

        my_list_lock = view.findViewById(R.id.my_list_lock);
        my_list_thumnail = view.findViewById(R.id.my_list_thumnail);
        my_list_delete = view.findViewById(R.id.my_list_delete);
        my_list_name = view.findViewById(R.id.my_list_name);

        MyAlbumData myListData = list.get(position);

        my_list_name.setText(myListData.getListName());
        Glide.with(view).load(myListData.getImageUrl()).into(my_list_thumnail);

        if (myListData.getList_mode().equals("private")){
            my_list_lock.setVisibility(View.VISIBLE);
        } else {
            my_list_lock.setVisibility(View.GONE);
        }

        // 리스트 제거
        my_list_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert_ex = new AlertDialog.Builder(context);
                alert_ex.setMessage("해당 리스트를 삭제하시겠습니까?");
                alert_ex.setNegativeButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteList(list.get(position).getImageUrl(), key_list.get(position));
                    }
                });
                alert_ex.setPositiveButton("아니요", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { } });

                AlertDialog alert = alert_ex.create();
                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.DKGRAY));
                alert.show();
            }
        });

        return view;
    }

    public void resetList(){
        list.clear();
        key_list.clear();
    }

    public String getKey(int position){
        return key_list.get(position);
    }

    public void addItemToList(MyAlbumData listData, String key){
        list.add(listData);
        key_list.add(key);
    }

    private void deleteList(String image, String key){
        FirebaseStorage.getInstance().getReferenceFromUrl(image).delete();
        FirebaseDatabase.getInstance().getReference("PlayLists").child(key).removeValue();

        FirebaseDatabase.getInstance().getReference("PlayLists_song").orderByChild("key")
                .equalTo(key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String key = ds.getKey();
                            assert key != null;
                            FirebaseDatabase.getInstance().getReference("PlayLists_song").child(key).removeValue();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }
}
