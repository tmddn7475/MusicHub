package com.example.capstone.Library;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.capstone.Adapter.MusicListAdapter;
import com.example.capstone.Data.LikeData;
import com.example.capstone.Data.MusicListAdapterData;
import com.example.capstone.Fragment2.EtcFragment;
import com.example.capstone.Interface.MusicListListener;
import com.example.capstone.Interface.MusicListener;
import com.example.capstone.MainActivity;
import com.example.capstone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class LikeFragment extends Fragment implements MusicListListener {

    public static LikeFragment newInstance() {
        return new LikeFragment();
    }

    ListView like_list;
    TextView like_text;
    SearchView like_search;
    MusicListAdapter musicListAdapter;
    MusicListener musicListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            musicListener = (MusicListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_like, container, false);
        like_list = v.findViewById(R.id.like_list);
        like_text = v.findViewById(R.id.like_text);
        // 검색
        like_search = v.findViewById(R.id.like_search);
        like_search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                ((MusicListAdapter)like_list.getAdapter()).getFilter().filter(newText);
                return false;
            }
        });

        like_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MusicListAdapterData data = (MusicListAdapterData) like_list.getItemAtPosition(position);
                String url = data.getSongUrl();
                musicListener.sendMessage(url);
            }
        });

        // 좋아요
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        musicListAdapter = new MusicListAdapter(getContext(), this);
        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference("Like");
        Query query = mReference.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getChildren() != null && snapshot.getChildren().iterator().hasNext()){
                    //exist
                    musicListAdapter.resetList();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        LikeData data = ds.getValue(LikeData.class);
                        assert data != null;
                        get_music(data.getSongUrl());
                    }
                    like_text.setVisibility(View.GONE);
                } else {
                    //not exist
                    like_text.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        return v;
    }

    public void get_music(String url){
        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference("Songs");
        Query query = mReference.orderByChild("songUrl").equalTo(url);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    MusicListAdapterData mld = ds.getValue(MusicListAdapterData.class);
                    assert mld != null;
                    musicListAdapter.addItemToList2(mld);
                }
                like_list.setAdapter(musicListAdapter);
                musicListAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

    }

    @Override
    public void sendEtc(String message) {
        MainActivity mainActivity = (MainActivity) getContext();
        assert mainActivity != null;
        FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
        EtcFragment etcFragment = MainActivity.etcFragment;

        if(!etcFragment.isAdded()){
            Bundle bundle = new Bundle();
            bundle.putString("url", message);
            etcFragment.setArguments(bundle);

            etcFragment.show(fragmentManager, etcFragment.getTag());
        }
    }
}