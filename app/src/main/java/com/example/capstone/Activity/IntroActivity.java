package com.example.capstone.Activity;

import static com.example.capstone.Static_command.Get_Internet;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.capstone.R;

public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        int internet = Get_Internet(this);
        if(internet == 0){
            //인터넷 연결 안 될 시 종료시키기
            AlertDialog.Builder alert_ex = new AlertDialog.Builder(this);
            alert_ex.setMessage("네트워크 연결을 해주시길 바랍니다");

            alert_ex.setNegativeButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finishAffinity();
                }
            }).setCancelable(false);//영역 밖을 클릭해도 창이 사라지지 않게 한다.

            AlertDialog alert = alert_ex.create();
            alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.DKGRAY));
            alert.show();
        } else {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    finishAffinity();
                }
            },1500); //1.5초 후 로그인 액티비티로 넘어감
        }
    }
}