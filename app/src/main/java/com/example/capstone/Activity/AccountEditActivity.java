package com.example.capstone.Activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.capstone.Data.AccountData;
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
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class AccountEditActivity extends AppCompatActivity {

    Uri image;
    byte[] bytes;
    ImageView account_edit_image, account_edit_back_btn;
    Button account_edit_image_btn, account_edit_save_btn, password_edit_btn;
    EditText account_nickname_edit, account_info_edit;
    TextView account_info_length;
    String key, email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    DatabaseReference mReference = FirebaseDatabase.getInstance().getReference("accounts");
    Dialog dialog;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_edit);

        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 다이어로그에 타이틀 안 나오게 하기
        dialog.setContentView(R.layout.progress_layout2);

        account_info_length = findViewById(R.id.account_info_length);
        account_edit_image = findViewById(R.id.account_edit_image);
        account_edit_image_btn = findViewById(R.id.account_edit_image_btn);
        account_edit_save_btn = findViewById(R.id.account_edit_save_btn);
        account_nickname_edit = findViewById(R.id.account_nickname_edit);
        account_info_edit = findViewById(R.id.account_info_edit);
        account_edit_back_btn = findViewById(R.id.song_edit_back_btn);
        password_edit_btn = findViewById(R.id.password_edit_btn);

        setAccountEdit(email);

        account_edit_image_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.with(AccountEditActivity.this)
                        .crop(1f, 1f)
                        // 이미지 크기 지정
                        .compress(1024)
                        // 최대 가로세로 크기 지정
                        .maxResultSize(500, 500)
                        .start();
            }
        });

        // 저장
        account_edit_save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (account_nickname_edit.getText().toString().isEmpty()){
                    Toast.makeText(AccountEditActivity.this, "닉네임을 적어주세요", Toast.LENGTH_SHORT).show();
                } else if (image != null){
                    dialog.show();
                    dialog.setCancelable(false);

                    uploadImageToServer(bytes, email);
                } else if (bytes == null){
                    dialog.show();
                    dialog.setCancelable(false);

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("email", email);
                    hashMap.put("nickname", account_nickname_edit.getText().toString());
                    hashMap.put("info", account_info_edit.getText().toString());

                    mReference.child(key).updateChildren(hashMap);
                    editNickName(email, account_nickname_edit.getText().toString(), "");
                }
            }
        });

        // 비밀번호 변경
        password_edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert_ex = new AlertDialog.Builder(AccountEditActivity.this);
                alert_ex.setMessage("비밀번호를 변경하시겠습니까?\n비밀번호를 재설정하는 메일을 보냅니다");
                alert_ex.setNegativeButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(AccountEditActivity.this, "해당 이메일로 비밀번호를 재설정하는 메일을 보냈습니다", Toast.LENGTH_SHORT).show();
                        FirebaseAuth.getInstance().sendPasswordResetEmail(email);
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

        // 터치시 스크롤 되도록 설정
        account_info_edit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (account_info_edit.hasFocus()) {
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

        account_info_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String length = account_info_edit.getText().toString();
                account_info_length.setText(length.length() + " / 2000");
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        account_edit_back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            image = data.getData();
            Bitmap bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), image));
            account_edit_image.setImageBitmap(bitmap);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
            bytes = byteArrayOutputStream.toByteArray();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void uploadImageToServer(byte[] image, String email) {
        UploadTask uploadTask = FirebaseStorage.getInstance().getReference()
                .child("Accounts_Thumbnails").child(email).putBytes(image);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> task = taskSnapshot.getStorage().getDownloadUrl();
                while (!task.isComplete());
                Uri urlsong = task.getResult();

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("email", email);
                hashMap.put("nickname", account_nickname_edit.getText().toString());
                hashMap.put("info", account_info_edit.getText().toString());
                hashMap.put("imageUrl", urlsong.toString());

                mReference.child(key).updateChildren(hashMap);
                editNickName(email, account_nickname_edit.getText().toString(), urlsong.toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "오류가 발생했습니다! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    private void setAccountEdit(String email){
        Query query = mReference.orderByChild("email").equalTo(email).limitToFirst(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    key = ds.getKey();
                    AccountData data = ds.getValue(AccountData.class);
                    assert data != null;

                    if(data.getImageUrl().equals("")){
                        account_edit_image.setImageResource(R.drawable.baseline_account_circle_24);
                    } else {
                        Glide.with(getApplicationContext()).load(data.getImageUrl()).into(account_edit_image);
                    }
                    account_nickname_edit.setText(data.getNickname());
                    account_info_edit.setText(data.getInfo());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "오류가 발생했습니다! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    private void editNickName(String email, String name, String url){
        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference("Songs");
        Query query = mReference.orderByChild("email").equalTo(email);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String song_key = ds.getKey();

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("songArtist", name);

                    assert song_key != null;
                    mReference.child(song_key).updateChildren(hashMap);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "오류가 발생했습니다! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        DatabaseReference mReference2 = FirebaseDatabase.getInstance().getReference("Comments");
        Query query2 = mReference2.orderByChild("email").equalTo(email);
        query2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String comments_key = ds.getKey();

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("nickname", name);
                    if(!url.isEmpty()){
                        hashMap.put("imageUrl", url);
                    }
                    assert comments_key != null;
                    mReference2.child(comments_key).updateChildren(hashMap);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "오류가 발생했습니다! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        DatabaseReference mReference3 = FirebaseDatabase.getInstance().getReference("PlayLists");
        Query query3 = mReference3.orderByChild("email").equalTo(email);
        query3.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String key = ds.getKey();

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("nickname", name);

                    assert key != null;
                    mReference3.child(key).updateChildren(hashMap);
                }
                dialog.dismiss();
                Toast.makeText(AccountEditActivity.this, "정보가 수정되었습니다", Toast.LENGTH_SHORT).show();
                finish();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "오류가 발생했습니다! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }
}