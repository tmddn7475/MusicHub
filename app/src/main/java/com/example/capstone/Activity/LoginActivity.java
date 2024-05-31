package com.example.capstone.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.capstone.MainActivity;
import com.example.capstone.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    Button login_btn, login_re_btn;
    EditText login_email_edit, login_password_edit;
    CheckBox login_chk;
    Dialog progress_dialog;

    private boolean saveLoginData;
    private SharedPreferences appData;
    private String id;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && appData.getBoolean("SAVE_LOGIN_DATA", false)) {
            complete();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progress_dialog = new Dialog(LoginActivity.this);
        progress_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 다이어로그에 타이틀 안 나오게 하기
        progress_dialog.setContentView(R.layout.progress_layout2);
        progress_dialog.setCancelable(false);

        appData = getSharedPreferences("appData", MODE_PRIVATE);
        load();

        login_btn = findViewById(R.id.edit_save_btn);
        login_re_btn = findViewById(R.id.login_re_btn);
        login_email_edit = findViewById(R.id.song_edit_name);
        login_password_edit = findViewById(R.id.upload_artist_name);
        login_chk = findViewById(R.id.login_chk);

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = login_email_edit.getText().toString().trim();
                String password = login_password_edit.getText().toString().trim();
                progress_dialog.show();

                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {//성공했을때
                            progress_dialog.dismiss();
                            complete();
                        } else {//실패했을때
                            progress_dialog.dismiss();
                            Toast.makeText(LoginActivity.this, "다시 시도해주세요", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                save();
            }
        });

        if(saveLoginData) {
            login_email_edit.setText(id);        // 아이디가 입력된 상태로 유지
            login_chk.setChecked(saveLoginData); // 체크된 상태로 유지
        }

        login_re_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // 뒤로 가기 누를 때
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    private void complete() {
        Intent login_intent = new Intent(this, MainActivity.class);
        startActivity(login_intent);
        finishAffinity();
    }

    private void save() {
        SharedPreferences.Editor editor = appData.edit();
        editor.putBoolean("SAVE_LOGIN_DATA", login_chk.isChecked());
        editor.putString("ID", login_email_edit.getText().toString().trim());
        editor.apply();
    }
    // 설정값을 불러오는 함수
    private void load() {
        saveLoginData = appData.getBoolean("SAVE_LOGIN_DATA", false);
        id = appData.getString("ID", null);
    }

    // 뒤로 가기 누를 때
    private long backpressedTime = 0;
    private final OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            if (System.currentTimeMillis() > backpressedTime + 2000) {
                backpressedTime = System.currentTimeMillis();
                Snackbar.make(findViewById(R.id.login_activity) ,"뒤로 버튼을 한번 더 누르면 종료됩니다.", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(Color.DKGRAY).setTextColor(Color.WHITE).show();
            } else if (System.currentTimeMillis() <= backpressedTime + 2000) {
                finishAffinity();
            }
        }
    };
}