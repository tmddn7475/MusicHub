package com.example.capstone.Activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.capstone.Data.AlbumToSongData;
import com.example.capstone.Data.MusicListAdapterData;
import com.example.capstone.Data.MyAlbumData;
import com.example.capstone.R;
import com.example.capstone.Adapter.SongsEditAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class AlbumEditActivity extends AppCompatActivity {

    TextView album_edit_save, album_edit_delete, description_length;
    ImageView back_btn;
    EditText edit_list_name, edit_list_description;
    ListView album_edit_list;
    Switch edit_set;
    SongsEditAdapter songsEditAdapter;
    String album_key;
    Dialog dialog;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_edit);

        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 다이어로그에 타이틀 안 나오게 하기
        dialog.setContentView(R.layout.progress_layout2);

        album_edit_save = findViewById(R.id.album_edit_save);
        album_edit_delete = findViewById(R.id.album_edit_delete);
        edit_list_name = findViewById(R.id.edit_list_name);
        description_length = findViewById(R.id.description_length);
        back_btn = findViewById(R.id.back_btn);
        edit_list_description = findViewById(R.id.edit_list_description);
        album_edit_list = findViewById(R.id.album_edit_list);
        edit_set = findViewById(R.id.edit_set);

        songsEditAdapter = new SongsEditAdapter();
        description_length.setText(edit_list_description.getText().toString().length() + " / 2000");

        String key = getIntent().getStringExtra("key");
        // 앨범 정보
        FirebaseDatabase.getInstance().getReference("PlayLists").orderByKey().equalTo(key).limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            album_key = ds.getKey();
                            MyAlbumData mld = ds.getValue(MyAlbumData.class);
                            assert mld != null;
                            edit_list_description.setText(mld.getDescription());
                            edit_list_name.setText(mld.getListName());

                            if(mld.getList_mode().equals("public")){
                                edit_set.setChecked(true);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
        // 앨범 수록 곡 가져오기
        FirebaseDatabase.getInstance().getReference("PlayLists_song").orderByChild("key").equalTo(key)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            AlbumToSongData mld = ds.getValue(AlbumToSongData.class);
                            assert mld != null;
                            getTracks(mld.getSongUrl(), mld.getTime());
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        edit_list_description.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String length = edit_list_description.getText().toString();
                description_length.setText(length.length() + " / 2000");
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 리스트에서 체크 된 걸 제거
        album_edit_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SparseBooleanArray checkedItems = album_edit_list.getCheckedItemPositions();
                int count = songsEditAdapter.getCount();
                for (int i = count - 1; i >= 0; i--) {
                    if (checkedItems.get(i)) {
                        songsEditAdapter.list.remove(i);
                    }
                    Log.i("check", String.valueOf(checkedItems.get(i)));
                }
                // 모든 선택 상태 초기화.
                album_edit_list.clearChoices();
                songsEditAdapter.notifyDataSetChanged();
            }
        });

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 저장
        album_edit_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert_ex = new AlertDialog.Builder(AlbumEditActivity.this);
                alert_ex.setMessage("저장하시겠습니까?");
                alert_ex.setNegativeButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editDetail();
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
    }

    // 앨범 설명 수정
    private void editDetail(){
        dialog.show();
        dialog.setCancelable(false);

        //앨범 정보 수정
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("listName", edit_list_name.getText().toString());
        hashMap.put("description", edit_list_description.getText().toString());

        if(edit_set.isChecked()){
            hashMap.put("list_mode", "public");
        } else {
            hashMap.put("list_mode", "private");
        }
        FirebaseDatabase.getInstance().getReference("PlayLists").child(album_key).updateChildren(hashMap);

        // 앨범 트랙 수정
        FirebaseDatabase.getInstance().getReference("PlayLists_song").orderByChild("key")
                .equalTo(album_key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            AlbumToSongData data = ds.getValue(AlbumToSongData.class);
                            String song_key = ds.getKey();
                            assert data != null;

                            if(!songsEditAdapter.existList().contains(data.getSongUrl())){
                                assert song_key != null;
                                //Log.i("list", song_key);
                                FirebaseDatabase.getInstance().getReference("PlayLists_song").child(song_key).removeValue();
                            }
                        }
                        dialog.dismiss();
                        Toast.makeText(AlbumEditActivity.this, "정보가 수정되었습니다", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getApplicationContext(), "오류가 발생했습니다! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
    }

    private void getTracks(String url, String time){
        FirebaseDatabase.getInstance().getReference("Songs").orderByChild("songUrl").equalTo(url).limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            MusicListAdapterData mld = ds.getValue(MusicListAdapterData.class);
                            assert mld != null;
                            songsEditAdapter.addItemToList(mld, time);
                        }
                        songsEditAdapter.sort();
                        album_edit_list.setAdapter(songsEditAdapter);
                        songsEditAdapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getApplicationContext(), "오류가 발생했습니다! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
    }
}