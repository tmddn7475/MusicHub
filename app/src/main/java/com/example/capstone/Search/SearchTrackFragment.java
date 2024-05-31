package com.example.capstone.Search;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.capstone.Adapter.MusicListAdapter;
import com.example.capstone.Data.MusicListAdapterData;
import com.example.capstone.Fragment2.EtcFragment;
import com.example.capstone.Interface.MusicListListener;
import com.example.capstone.Interface.MusicListener;
import com.example.capstone.MainActivity;
import com.example.capstone.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class SearchTrackFragment extends Fragment implements MusicListListener {

    ListView search_track_list;
    TextView none;
    MusicListAdapter musicListAdapter;
    String query;
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
        View v = inflater.inflate(R.layout.fragment_search_track, container, false);

        query = getArguments().getString("search").toLowerCase();
        none = v.findViewById(R.id.none);
        search_track_list = v.findViewById(R.id.search_track_list);
        musicListAdapter = new MusicListAdapter(getActivity(), this);

        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference("Songs");
        mReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                musicListAdapter.resetList();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    MusicListAdapterData mld = ds.getValue(MusicListAdapterData.class);
                    assert mld != null;
                    if(mld.getSongName().toLowerCase().contains(query) || mld.getSongArtist().toLowerCase().contains(query)){
                        musicListAdapter.addItemToList(mld);
                    }
                }
                search_track_list.setAdapter(musicListAdapter);
                musicListAdapter.notifyDataSetChanged();
                if(musicListAdapter.getCount() == 0){
                    none.setVisibility(View.VISIBLE);
                } else {
                    none.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        search_track_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MusicListAdapterData data = (MusicListAdapterData) search_track_list.getItemAtPosition(position);
                String url = data.getSongUrl();
                musicListener.sendMessage(url);
            }
        });

        return v;
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