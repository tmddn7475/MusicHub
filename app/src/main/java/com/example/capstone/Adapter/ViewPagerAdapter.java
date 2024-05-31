package com.example.capstone.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private final List<Fragment> fragmnets = new ArrayList<Fragment>() ;
    private final List<String> titles = new ArrayList<>() ;

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragmnets.get(position);
    }

    @Override
    public int getItemCount() {
        return fragmnets.size();
    }

    public void addFragment(@NonNull Fragment frag, String title) {
        fragmnets.add(frag) ;
        titles.add(title) ;
    }

    @NonNull
    public String getTitle(int position) {
        return titles.get(position) ;
    }
}
