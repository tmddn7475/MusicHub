package com.example.capstone.Library;

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
import com.example.capstone.Data.HistoryData;
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

import java.util.ArrayList;

public class HistoryFragment extends Fragment implements MusicListListener {

    ArrayList<String> arr = new ArrayList<String>();
    ArrayList<String> arr2 = new ArrayList<String>();
    ListView history_list;
    TextView history_text;
    MusicListAdapter musicListAdapter;
    MusicListener musicListener;

    public static HistoryFragment newInstance() {
        return new HistoryFragment();
    }

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
        View v = inflater.inflate(R.layout.fragment_history, container, false);

        history_list = v.findViewById(R.id.history_list);
        history_text = v.findViewById(R.id.history_text);

        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        musicListAdapter = new MusicListAdapter(getContext(), this);
        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference("History");
        Query query = mReference.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arr.clear();
                arr2.clear();
                musicListAdapter.resetList();
                if(snapshot.getChildren() != null && snapshot.getChildren().iterator().hasNext()){
                    //exist
                    history_text.setVisibility(View.GONE);
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        HistoryData data = ds.getValue(HistoryData.class);
                        assert data != null;
                        arr.add(data.getSongUrl());
                    }
                    for(String strValue : arr) {
                        if(!arr2.contains(strValue)) {
                            arr2.add(strValue);
                            getHistory_music(strValue);
                        }
                    }
                } else {
                    //not exist
                    history_text.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        history_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MusicListAdapterData data = (MusicListAdapterData) history_list.getItemAtPosition(position);
                String url = data.getSongUrl();
                musicListener.sendMessage(url);
            }
        });

        return v;
    }

    public void getHistory_music(String url){
        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference("Songs");
        Query query = mReference.orderByChild("songUrl").equalTo(url);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    MusicListAdapterData mld = ds.getValue(MusicListAdapterData.class);
                    assert mld != null;
                    musicListAdapter.addItemToList(mld);
                }
                history_list.setAdapter(musicListAdapter);
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