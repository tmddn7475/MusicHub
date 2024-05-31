package com.example.capstone.Account;

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

public class AccountTrackFragment extends Fragment implements MusicListListener {

    String email;
    TextView track_none;
    ListView track_list;
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
        View v = inflater.inflate(R.layout.fragment_account_track, container, false);

        email = getArguments().getString("email");

        track_list = v.findViewById(R.id.track_list);
        track_none = v.findViewById(R.id.track_none);
        musicListAdapter = new MusicListAdapter(getActivity(), this);

        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference("Songs");
        Query query = mReference.orderByChild("email").equalTo(email);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                musicListAdapter.resetList();
                if(snapshot.getChildren() != null && snapshot.getChildren().iterator().hasNext()){
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        MusicListAdapterData mld = ds.getValue(MusicListAdapterData.class);
                        assert mld != null;
                        musicListAdapter.addItemToList(mld);
                    }
                    track_list.setAdapter(musicListAdapter);
                    musicListAdapter.notifyDataSetChanged();
                    track_none.setVisibility(View.GONE);
                } else {
                    track_none.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        track_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MusicListAdapterData data = (MusicListAdapterData) track_list.getItemAtPosition(position);
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