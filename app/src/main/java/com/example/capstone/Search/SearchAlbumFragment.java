package com.example.capstone.Search;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.example.capstone.Account.AccountAlbumFragment;
import com.example.capstone.Adapter.GridViewer;
import com.example.capstone.Data.MyAlbumData;
import com.example.capstone.Fragment1.AlbumFragment;
import com.example.capstone.MainActivity;
import com.example.capstone.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class SearchAlbumFragment extends Fragment {

    GridView search_gridview;
    TextView none;
    GridViewAdapter gridAdapter;
    String query;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search_album, container, false);

        search_gridview = v.findViewById(R.id.search_gridview);
        none = v.findViewById(R.id.none);

        assert getArguments() != null;
        query = getArguments().getString("search").toLowerCase();
        gridAdapter = new GridViewAdapter();

        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference("PlayLists");
        mReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                gridAdapter.resetList();
                if(snapshot.getChildren() != null && snapshot.getChildren().iterator().hasNext()){
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        MyAlbumData data = ds.getValue(MyAlbumData.class);
                        assert data != null;
                        if(data.getList_mode().equals("public") && data.getListName().toLowerCase().contains(query)){
                            gridAdapter.addItem(data, ds.getKey());
                        }
                    }
                    search_gridview.setAdapter(gridAdapter);
                    gridAdapter.notifyDataSetChanged();
                    if(gridAdapter.getCount() == 0){
                        none.setVisibility(View.VISIBLE);
                    } else {
                        none.setVisibility(View.GONE);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        search_gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MainActivity mainActivity = (MainActivity) getContext();
                assert mainActivity != null;
                FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
                AlbumFragment albumFragment = new AlbumFragment();

                Bundle bundle = new Bundle();
                bundle.putString("key", gridAdapter.getKey(position));

                albumFragment.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.container, albumFragment).addToBackStack(null).commit();
            }
        });

        return v;
    }

    class GridViewAdapter extends BaseAdapter {
        ArrayList<MyAlbumData> items = new ArrayList<MyAlbumData>();
        ArrayList<String> key_list = new ArrayList<String>();

        @Override
        public int getCount() {
            return items.size();
        }

        public void resetList(){
            items.clear();
            key_list.clear();
        }

        public String getKey(int pos){
            return key_list.get(pos);
        }

        public void addItem(MyAlbumData singerItem, String key) {
            items.add(singerItem);
            key_list.add(key);
        }

        @Override
        public MyAlbumData getItem(int i) {
            return items.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            GridViewer gridViewer = new GridViewer(getActivity().getApplicationContext());
            gridViewer.setItem(items.get(i));
            return gridViewer;
        }
    }
}