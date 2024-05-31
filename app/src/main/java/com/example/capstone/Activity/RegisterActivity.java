package com.example.capstone.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.capstone.Data.AccountData;
import com.example.capstone.R;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterActivity extends AppCompatActivity {

    Uri image;
    byte[] bytes;
    ImageView register_back_btn;
    CircleImageView register_circle_image;
    Button register_btn, register_profile_btn;
    EditText register_email_edit, register_password_edit, register_password_correct, register_nickname_edit;
    StorageReference storageReference;
    Dialog progress_dialog;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        storageReference = FirebaseStorage.getInstance().getReference();

        register_circle_image = findViewById(R.id.account_edit_image);
        register_email_edit = findViewById(R.id.song_edit_name);
        register_password_edit = findViewById(R.id.upload_artist_name);
        register_password_correct = findViewById(R.id.song_edit_category);
        register_nickname_edit = findViewById(R.id.register_nickname_edit);
        register_btn = findViewById(R.id.register_btn);
        register_back_btn = findViewById(R.id.register_back_btn);
        register_profile_btn = findViewById(R.id.account_edit_image_btn);

        progress_dialog = new Dialog(RegisterActivity.this);
        progress_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 다이어로그에 타이틀 안 나오게 하기
        progress_dialog.setContentView(R.layout.progress_layout2);
        progress_dialog.setCancelable(false);

        register_profile_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.with(RegisterActivity.this).crop(1f, 1f)
                        // 이미지 크기 지정
                        .compress(1024)
                        // 최대 가로세로 크기 지정
                        .maxResultSize(500, 500)
                        .start();
            }
        });

        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = register_email_edit.getText().toString().trim();
                String password = register_password_edit.getText().toString().trim();
                String password_correct = register_password_correct.getText().toString().trim();
                String nickname = register_nickname_edit.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty() || password_correct.isEmpty() || nickname.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "전부 입력해주세요", Toast.LENGTH_SHORT).show();
                } else if (password.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "비밀번호를 6자리 이상 입력해주세요", Toast.LENGTH_SHORT).show();
                } else if (!password.equals(password_correct)){
                    Toast.makeText(RegisterActivity.this, "비밀번호가 다릅니다", Toast.LENGTH_SHORT).show();
                } else {
                    progress_dialog.show();
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                uploadImageToServer(bytes, email, email, password, nickname);
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(RegisterActivity.this, "회원가입 실패", Toast.LENGTH_SHORT).show();
                                progress_dialog.dismiss();
                            }
                        }
                    });
                }
            }
        });

        register_back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            try {
                image = data.getData();
                Bitmap bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), image));
                register_circle_image.setImageBitmap(bitmap);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
                bytes = byteArrayOutputStream.toByteArray();
            }
            catch (IOException e) {
                bytes = null;
                e.printStackTrace();
            }
        } else {
            image = null;
            bytes = null;
        }
    }

    public void uploadImageToServer(byte[] image, String fileName, String email, String password, String name) {
        if(image == null){
            addAcount(email, password, name, "");
        } else {
            UploadTask uploadTask = storageReference.child("Accounts_Thumbnails").child(fileName).putBytes(image);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> task = taskSnapshot.getStorage().getDownloadUrl();
                    while (!task.isComplete());
                    Uri urlsong = task.getResult();

                    addAcount(email, password, name, urlsong.toString());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), "오류가 발생했습니다! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                    progress_dialog.dismiss();
                }
            });
        }
    }

    public void addAcount(String email, String password, String name, String imageUrl){
        AccountData accountData = new AccountData(email, password, name, imageUrl, "");
        FirebaseDatabase.getInstance().getReference("accounts")
                .push().setValue(accountData).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(), "회원가입이 완료되었습니다", Toast.LENGTH_SHORT).show();
                        progress_dialog.dismiss();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "이미 가입한 이메일입니다", Toast.LENGTH_SHORT).show();
                        progress_dialog.dismiss();
                    }
                });
    }
}