package com.example.capstone.Activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.capstone.Data.AccountData;
import com.example.capstone.Data.MyAlbumData;
import com.example.capstone.R;
import com.github.dhaval2404.imagepicker.ImagePicker;
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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AddAlbumActivity extends AppCompatActivity {

    String nickname, fileName, set = "private", imageUrl, email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    ImageView list_back_btn, list_selectImage;
    EditText list_name, list_description;
    TextView list_description_length;
    Switch my_list_set;
    Button list_upload_btn;
    byte[] bytes;
    Uri image;
    StorageReference storageReference;
    Dialog progress_dialog;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_list);

        list_back_btn = findViewById(R.id.list_back_btn);
        list_selectImage = findViewById(R.id.list_selectImage);
        list_name = findViewById(R.id.list_name);
        list_description = findViewById(R.id.list_description);
        list_upload_btn = findViewById(R.id.list_upload_btn);
        my_list_set = findViewById(R.id.my_list_set);
        list_description_length = findViewById(R.id.list_description_length);
        storageReference = FirebaseStorage.getInstance().getReference();
        list_selectImage.setClipToOutline(true);

        progress_dialog = new Dialog(AddAlbumActivity.this);
        progress_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 다이어로그에 타이틀 안 나오게 하기
        progress_dialog.setContentView(R.layout.progress_layout2);
        progress_dialog.setCancelable(false);

        getAccount(email);

        // 공개 설정
        my_list_set.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    set = "public";
                } else {
                    set = "private";
                }
            }
        });
        // 나가기
        list_back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // 이미지 선택
        list_selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.with(AddAlbumActivity.this)
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
        // 설명 스크롤
        list_description.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (list_description.hasFocus()) {
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

        list_description.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String length = list_description.getText().toString();
                list_description_length.setText(length.length() + " / 2000");
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 업로드
        list_upload_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(list_name.getText().toString().isEmpty()){

                }
                else if (image == null){

                }
                else {
                    progress_dialog.show();
                    fileName = list_name.getText().toString();
                    String description = list_description.getText().toString();
                    uploadImageToServer(bytes, fileName, description);
                }
            }
        });
    }

    private void uploadImageToServer(byte[] image, String fileName, String description) {
        UploadTask uploadTask = storageReference.child("PlayLists_Thumbnails").child(email +"/"+ fileName).putBytes(image);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> task = taskSnapshot.getStorage().getDownloadUrl();
                while (!task.isComplete());
                Uri urlsong = task.getResult();
                imageUrl = urlsong.toString();
                createPlayList(fileName, set, description);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "오류가 발생했습니다! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createPlayList(String name, String mode, String description){
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        MyAlbumData myListData = new MyAlbumData(email, name, mode, nickname, description, imageUrl);
        FirebaseDatabase.getInstance().getReference("PlayLists").push().setValue(myListData);
        progress_dialog.dismiss();
        finish();
    }

    // 닉네임 가져오기
    void getAccount(String email){
        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference("accounts");
        Query query = mReference.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    AccountData data = ds.getValue(AccountData.class);
                    assert data != null;
                    nickname = data.getNickname();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // 이미지 가져오기
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            try {
                image = data.getData();
                // Uri를 활용하여 ImageView에 가져온 이미지 표시
                Bitmap bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), image));
                list_selectImage.setImageBitmap(bitmap);
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
}