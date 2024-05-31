package com.example.capstone.Library;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.capstone.Adapter.ViewPagerAdapter;
import com.example.capstone.R;
import com.example.capstone.Activity.SettingActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class LibraryFragment extends Fragment {

    TabLayout tabLayout;
    ViewPager2 viewPager;
    ViewPagerAdapter viewPagerAdapter;
    ImageView setting;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_library, container, false);

        tabLayout = v.findViewById(R.id.tabLayout);
        viewPager = v.findViewById(R.id.viewPager);
        setting = v.findViewById(R.id.setting);

        viewPagerAdapter = new ViewPagerAdapter(getActivity());
        viewPagerAdapter.addFragment(MyAlbumFragment.newInstance(), "내 앨범");
        viewPagerAdapter.addFragment(LikeFragment.newInstance(), "좋아요");
        viewPagerAdapter.addFragment(HistoryFragment.newInstance(), "음악 기록");

        viewPager.setAdapter(viewPagerAdapter);

        TabLayoutMediator tm = new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(viewPagerAdapter.getTitle(position)) ;
        });
        tm.attach();

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SettingActivity.class);
                startActivity(intent);
            }
        });

        return v;
    }
}