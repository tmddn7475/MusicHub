package com.example.capstone.Search;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.capstone.Adapter.ViewPagerAdapter;
import com.example.capstone.MainActivity;
import com.example.capstone.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class SearchResultFragment extends Fragment {

    String query;
    TabLayout tabLayout;
    ImageView search_result_back_btn;
    ViewPager2 viewPager;
    ViewPagerAdapter viewPagerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search_result, container, false);
        assert getArguments() != null;
        query = getArguments().getString("search");

        tabLayout = v.findViewById(R.id.tabLayout);
        viewPager = v.findViewById(R.id.viewPager);
        search_result_back_btn = v.findViewById(R.id.search_result_back_btn);

        SearchTrackFragment searchTrackFragment = new SearchTrackFragment();
        SearchAlbumFragment searchAlbumFragment = new SearchAlbumFragment();
        SearchAccountFragment searchAccountFragment = new SearchAccountFragment();

        Bundle bundle = new Bundle();
        bundle.putString("search", query);
        searchTrackFragment.setArguments(bundle);
        searchAlbumFragment.setArguments(bundle);
        searchAccountFragment.setArguments(bundle);

        viewPagerAdapter = new ViewPagerAdapter(getActivity());
        viewPagerAdapter.addFragment(searchTrackFragment, "곡");
        viewPagerAdapter.addFragment(searchAccountFragment, "계정");
        viewPagerAdapter.addFragment(searchAlbumFragment, "앨범");

        viewPager.setAdapter(viewPagerAdapter);
        TabLayoutMediator tm = new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(viewPagerAdapter.getTitle(position)) ;
        });
        tm.attach();

        search_result_back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });

        return v;
    }

    private void back(){
        MainActivity mainActivity = (MainActivity) getContext();
        assert mainActivity != null;
        FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
        fragmentManager.beginTransaction().remove(this).commit();
        fragmentManager.popBackStack();
    }
}