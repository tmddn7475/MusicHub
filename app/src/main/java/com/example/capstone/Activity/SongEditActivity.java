package com.example.capstone.Activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.capstone.Data.MusicListAdapterData;
import com.example.capstone.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;

public class SongEditActivity extends AppCompatActivity {

    ImageView song_edit_back_btn;
    EditText song_edit_name, song_edit_category, song_edit_description;
    TextView song_edit_length, edit_save_btn, song_delete;
    String song_key, song_url, image_url;
    Dialog progress_dialog;

    final String[] category_arr = new String[] {"None", "Ambient", "Classical", "Dance & EDM",
            "Disco", "Hip hop", "Jazz", "R&B", "Reggae", "Rock"};

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_edit);

        progress_dialog = new Dialog(this);
        progress_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 다이어로그에 타이틀 안 나오게 하기
        progress_dialog.setContentView(R.layout.progress_layout2);

        song_edit_back_btn = findViewById(R.id.song_edit_back_btn);
        song_edit_length = findViewById(R.id.song_edit_length);
        song_edit_name = findViewById(R.id.song_edit_name);
        song_edit_category = findViewById(R.id.song_edit_category);
        song_edit_description = findViewById(R.id.song_edit_description);
        edit_save_btn = findViewById(R.id.edit_save_btn);
        song_delete = findViewById(R.id.song_delete);

        // 터치시 스크롤 되도록 설정
        song_edit_description.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (song_edit_description.hasFocus()) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction() & MotionEvent.ACTION_MASK){
                        case MotionEvent.ACTION_SCROLL:
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            return true;
                    }
                }
                return false;
            }
        });

        // 글자 수 제한
        song_edit_description.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String length = song_edit_description.getText().toString();
                song_edit_length.setText(length.length() + " / 2000");
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 카테고리
        song_edit_category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(SongEditActivity.this).setItems(category_arr, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        song_edit_category.setText(category_arr[which]);
                    }
                }).show();
            }
        });

        song_url = getIntent().getStringExtra("url");
        FirebaseDatabase.getInstance().getReference("Songs").orderByChild("songUrl").equalTo(song_url).limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            MusicListAdapterData data = ds.getValue(MusicListAdapterData.class);
                            song_key = ds.getKey();
                            image_url = data.getImageUrl();

                            assert data != null;
                            song_edit_name.setText(data.getSongName());
                            song_edit_category.setText(data.getSongCategory());
                            song_edit_description.setText(data.getSongInfo());
                            song_edit_length.setText(data.getSongInfo().length() + " / 2000");
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        song_edit_back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 저장
        edit_save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert_ex = new AlertDialog.Builder(SongEditActivity.this);
                alert_ex.setMessage("저장하시겠습니까?");
                alert_ex.setNegativeButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editSong();
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

        // 곡 삭제
        song_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert_ex = new AlertDialog.Builder(SongEditActivity.this);
                alert_ex.setMessage("곡을 삭제하시겠습니까?");
                alert_ex.setNegativeButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        progress_dialog.show();
                        progress_dialog.setCancelable(false);

                        FirebaseDatabase.getInstance().getReference("Songs").child(song_key).removeValue();
                        deleteData(song_url);
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

    private void editSong(){
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("songName", song_edit_name.getText().toString());
        hashMap.put("songInfo", song_edit_description.getText().toString());
        hashMap.put("songCategory", song_edit_category.getText().toString());

        FirebaseDatabase.getInstance().getReference("Songs").child(song_key).updateChildren(hashMap);
        Toast.makeText(SongEditActivity.this, "정보가 수정되었습니다", Toast.LENGTH_SHORT).show();
        finish();
    }

    // 관련된 데이터 삭제
    private void deleteData(String song_url){
        FirebaseStorage.getInstance().getReferenceFromUrl(song_url).delete();
        FirebaseStorage.getInstance().getReferenceFromUrl(image_url).delete();

        FirebaseDatabase.getInstance().getReference("History").orderByChild("songUrl").equalTo(song_url)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String key = ds.getKey();
                            assert key != null;

                            FirebaseDatabase.getInstance().getReference("History").child(key).removeValue();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getApplicationContext(), "오류가 발생했습니다! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                        progress_dialog.dismiss();
                    }
                });

        FirebaseDatabase.getInstance().getReference("Like").orderByChild("songUrl").equalTo(song_url)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String key = ds.getKey();
                            assert key != null;

                            FirebaseDatabase.getInstance().getReference("Like").child(key).removeValue();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getApplicationContext(), "오류가 발생했습니다! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                        progress_dialog.dismiss();
                    }
                });

        FirebaseDatabase.getInstance().getReference("PlayLists_song").orderByChild("songUrl").equalTo(song_url)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String key = ds.getKey();
                            assert key != null;

                            FirebaseDatabase.getInstance().getReference("PlayLists_song").child(key).removeValue();
                        }
                        Toast.makeText(SongEditActivity.this, "음악이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                        progress_dialog.dismiss();
                        finish();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getApplicationContext(), "오류가 발생했습니다! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                        progress_dialog.dismiss();
                    }
                });
    }
}