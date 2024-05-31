package com.example.capstone.Activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.capstone.Static_command;
import com.example.capstone.Data.AccountData;
import com.example.capstone.Data.MusicListAdapterData;
import com.example.capstone.R;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class UploadActivity extends AppCompatActivity {

    Uri uriSong, image;
    String fileName, songUrl, imageUrl, songLength;
    StorageReference storageReference;
    byte[] bytes;
    ImageView thumnail, upload_back_btn, file;
    TextView upload_edit_length;
    Button upload_btn;
    EditText song_name, songCategory, song_description;
    Dialog progress_dialog;
    String nickname, email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

    final String[] category_arr = new String[] {"None", "Ambient", "Classical", "Dance & EDM",
            "Disco", "Hip hop", "Jazz", "R&B", "Reggae", "Rock"};


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        storageReference = FirebaseStorage.getInstance().getReference();

        upload_edit_length = findViewById(R.id.song_edit_length);
        upload_back_btn = findViewById(R.id.upload_back_btn);
        file = findViewById(R.id.selectSongButton);
        thumnail = findViewById(R.id.song_edit_image);
        upload_btn = findViewById(R.id.edit_save_btn);
        song_name = findViewById(R.id.song_edit_name);
        songCategory = findViewById(R.id.song_edit_category);
        song_description = findViewById(R.id.song_edit_description);

        progress_dialog = new Dialog(UploadActivity.this);
        progress_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 다이어로그에 타이틀 안 나오게 하기
        progress_dialog.setContentView(R.layout.progress_layout);
        progress_dialog.setCancelable(false);

        getAccount(email);

        // 터치시 스크롤 되도록 설정
        song_description.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (song_description.hasFocus()) {
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

        // 카테고리
        songCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(UploadActivity.this).setItems(category_arr, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        songCategory.setText(category_arr[which]);
                    }
                }).show();
            }
        });

        // 파일
        file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("audio/*");
                song_launcher.launch(intent);
            }
        });

        song_description.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String length = song_description.getText().toString();
                upload_edit_length.setText(length.length() + " / 2000");
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        thumnail.setClipToOutline(true);
        thumnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.with(UploadActivity.this)
                        // .crop(16f, 9f)와 같이 이미지 자르는 사각형의 크기 지정 가능.
                        // ()안에 값이 없으면 유저가 직접 크기 선택
                        .crop(1f, 1f)
                        // 이미지 크기 지정
                        .compress(1024)
                        // 최대 가로세로 크기 지정
                        .maxResultSize(600, 600)
                        .start();
            }
        });

        // 업로드
        upload_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uriSong == null){
                    Toast.makeText(UploadActivity.this, "곡 파일을 가져와주세요", Toast.LENGTH_SHORT).show();
                }
                else if (song_name.getText().toString().isEmpty()){
                    Toast.makeText(UploadActivity.this, "곡 제목을 적어주세요", Toast.LENGTH_SHORT).show();
                }
                else if(songCategory.getText().toString().isEmpty()){
                    Toast.makeText(UploadActivity.this, "카테고리를 선택해주세요", Toast.LENGTH_SHORT).show();
                }
                else if (image == null){
                    Toast.makeText(UploadActivity.this, "이미지를 가져와주세요", Toast.LENGTH_SHORT).show();
                }
                else {
                    progress_dialog.show();
                    fileName = song_name.getText().toString();
                    String description = song_description.getText().toString();
                    String category = songCategory.getText().toString();

                    uploadImageToServer(bytes,fileName);
                    uploadFileToServer(uriSong,fileName,description,songLength,category);
                }
            }
        });

        upload_back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    // 이미지 파일 서버에 전송
    public void uploadImageToServer(byte[] image, String fileName) {
        UploadTask uploadTask = storageReference.child("Song_Thumbnails").child(email +"/"+ fileName).putBytes(image);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> task = taskSnapshot.getStorage().getDownloadUrl();
                while (!task.isComplete());
                Uri urlsong = task.getResult();
                imageUrl = urlsong.toString();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "오류가 발생했습니다! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                progress_dialog.dismiss();
            }
        });
    }

    // 음악 파일 서버에 전송
    public void uploadFileToServer(Uri uri, final String songName, final String description, final String duration, final String category){
        StorageReference filePath = storageReference.child("Audios").child(email +"/"+ songName);
        filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isComplete());
                Uri urlSong = uriTask.getResult();
                songUrl = urlSong.toString();
                uploadDetailsToDatabase(fileName,songUrl,imageUrl,description,duration,category);
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                TextView progress_percent = progress_dialog.findViewById(R.id.progress_percent);
                double progress = (100.0*snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                int currentProgress = (int) progress;
                progress_percent.setText(currentProgress + "%");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "오류가 발생했습니다! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                progress_dialog.dismiss();
            }
        });
    }

    // 파이어베이스에 데이터 저장
    public void uploadDetailsToDatabase(String songName, String songUrl, String imageUrl, String description, String songDuration, String songCategory) {
        MusicListAdapterData song = new MusicListAdapterData(songName, songUrl, imageUrl, nickname,
                email, description, songDuration, songCategory, Static_command.getTime());

        FirebaseDatabase.getInstance().getReference("Songs")
                .push().setValue(song).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(), "음악이 업로드되었습니다.", Toast.LENGTH_SHORT).show();
                        progress_dialog.dismiss();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "오류가 발생했습니다! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                        progress_dialog.dismiss();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            try {
                image = data.getData();
                // Uri를 활용하여 ImageView에 가져온 이미지 표시
                Bitmap bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), image));
                thumnail.setImageBitmap(bitmap);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
                bytes = byteArrayOutputStream.toByteArray();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            image = null;
            bytes = null;
        }
    }

    ActivityResultLauncher<Intent> song_launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>()
            {
                @Override
                public void onActivityResult(ActivityResult result)
                {
                    if (result.getResultCode() == RESULT_OK)
                    {
                        Intent intent = result.getData();
                        uriSong = intent.getData();
                        fileName = getFileName(uriSong);
                        song_name.setText(fileName);
                        songLength = getSongDuration(uriSong);
                    } else {
                        uriSong = null;
                    }
                }
            });

    // 계정 닉네임 가져오기
    void getAccount(String email){
        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference("accounts");
        Query query = mReference.orderByChild("email").equalTo(email);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    AccountData data = ds.getValue(AccountData.class);
                    assert data != null;
                    nickname = data.getNickname();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "오류가 발생했습니다! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                progress_dialog.dismiss();}
        });
    }

    @SuppressLint("Range")
    public String getFileName(Uri uri) {
        String result = null;
        if (Objects.equals(uri.getScheme(), "content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            assert result != null;
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public String getSongDuration(Uri song){
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(getApplicationContext(),song);
        String durationString = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long time = Long.parseLong(durationString);
        int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(time);
        int totalSeconds = (int) TimeUnit.MILLISECONDS.toSeconds(time);
        int seconds = totalSeconds-(minutes*60);
        if (String.valueOf(seconds).length() == 1){
            return minutes + ":0" + seconds;
        }else {
            return minutes + ":" + seconds;
        }
    }
}