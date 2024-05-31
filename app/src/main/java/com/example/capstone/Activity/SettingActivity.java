package com.example.capstone.Activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.example.capstone.R;
import com.example.capstone.Fragment1.SettingFragment;

public class SettingActivity extends AppCompatActivity {

    ImageView setting_back_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        setting_back_button = findViewById(R.id.setting_back_button);
        getSupportFragmentManager().beginTransaction().replace(R.id.setting_layout, new SettingFragment()).commit();

        SharedPreferences preferences;
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                Log.d("TAG", "onSharedPreferenceChanged: " + key);

            }
        });

        setting_back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}