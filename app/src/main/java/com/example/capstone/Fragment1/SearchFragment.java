package com.example.capstone.Fragment1;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.widget.SearchView;

import com.example.capstone.Data.PlayListDB;
import com.example.capstone.MainActivity;
import com.example.capstone.R;
import com.example.capstone.Adapter.SearchListAdapter;
import com.example.capstone.Search.SearchResultFragment;

public class SearchFragment extends Fragment {

    SearchView search_view;
    TextView none;
    SearchListAdapter searchListAdapter;
    ListView search_recent;
    PlayListDB playListDB;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search, container, false);

        none = v.findViewById(R.id.none);
        search_recent = v.findViewById(R.id.search_recent);
        search_view = v.findViewById(R.id.search_view);
        search_view.setSubmitButtonEnabled(true);

        playListDB = new PlayListDB(getContext());
        SQLiteDatabase database = playListDB.getWritableDatabase();
        String sql = "select * from search";
        Cursor cursor = database.rawQuery(sql, null);

        searchListAdapter = new SearchListAdapter(playListDB);
        while(cursor.moveToNext()){
            searchListAdapter.addItemToList(cursor.getString(0));
        }
        search_recent.setAdapter(searchListAdapter);

        if(searchListAdapter.getCount() == 0){
            none.setVisibility(View.VISIBLE);
        } else {
            none.setVisibility(View.GONE);
        }

        search_view.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search_view.setQuery("", false);
                MainActivity mainActivity = (MainActivity) getContext();
                assert mainActivity != null;
                FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
                SearchResultFragment searchResultFragment = new SearchResultFragment();

                Bundle bundle = new Bundle();
                bundle.putString("search", query);
                searchResultFragment.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.container, searchResultFragment).addToBackStack(null).commit();

                playListDB.addSearch(query);
                searchListAdapter.notifyDataSetChanged();
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        search_recent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MainActivity mainActivity = (MainActivity) getContext();
                assert mainActivity != null;
                FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
                SearchResultFragment searchResultFragment = new SearchResultFragment();

                String str = searchListAdapter.getStr(position);
                Bundle bundle = new Bundle();
                bundle.putString("search", str);
                searchResultFragment.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.container, searchResultFragment).addToBackStack(null).commit();
            }
        });


        return v;
    }
}