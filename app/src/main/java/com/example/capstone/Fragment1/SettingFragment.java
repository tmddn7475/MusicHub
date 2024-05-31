package com.example.capstone.Fragment1;

import static androidx.core.app.ActivityCompat.finishAffinity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.capstone.Activity.AccountEditActivity;
import com.example.capstone.Activity.LoginActivity;
import com.example.capstone.Activity.UploadActivity;
import com.example.capstone.Data.PlayListDB;
import com.example.capstone.MainActivity;
import com.example.capstone.R;
import com.example.capstone.Service.MusicService;
import com.google.firebase.auth.FirebaseAuth;

public class SettingFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);
    }

    @Override
    public boolean onPreferenceTreeClick(@NonNull Preference preference) {
        String key = preference.getKey();

        if(key.equals("account_edit")){
            Intent intent = new Intent(getContext(), AccountEditActivity.class);
            startActivity(intent);
            return true;
        } else if(key.equals("account_logout")){
            logOut();
            return true;
        } else if(key.equals("upload")){
            Intent intent = new Intent(getContext(), UploadActivity.class);
            startActivity(intent);
            return true;
        }

        return false;
    }

    private void logOut(){
        AlertDialog.Builder alert_ex = new AlertDialog.Builder(getActivity());
        alert_ex.setMessage("로그아웃하시겠습니까?");
        alert_ex.setNegativeButton("네", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PlayListDB playListDB = new PlayListDB(getContext());
                playListDB.deleteAll();

                // preference 초기화
                SharedPreferences preferences = getContext().getSharedPreferences("pref", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("url", "");
                editor.apply(); // 저장

                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                finishAffinity(getActivity());
                MainActivity.mediaController.stop();
            }
        });
        alert_ex.setPositiveButton("아니요", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        AlertDialog alert = alert_ex.create();
        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.DKGRAY));
        alert.show();
    }
}