package com.example.capstone.Fragment2;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.capstone.Adapter.MyAlbumAdapter;
import com.example.capstone.Static_command;
import com.example.capstone.Data.MyAlbumData;
import com.example.capstone.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class SongToAlbumFragment extends BottomSheetDialogFragment {

    BottomSheetBehavior bottomSheetBehavior;
    ListView song_to_list_view;
    MyAlbumAdapter listAdapter;
    TextView song_to_list_text;
    String url;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_song_to_list, container, false);

        song_to_list_view = v.findViewById(R.id.song_to_list_view);
        song_to_list_text = v.findViewById(R.id.song_to_list_text);

        listAdapter = new MyAlbumAdapter();
        url = getArguments().getString("url");
        getList();

        song_to_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String key = listAdapter.getKey(position);
                Static_command.putTrack(key, url);
                Toast.makeText(getContext(), "리스트에 곡이 추가되었습니다\n중복이 있을 경우 추가되지 않습니다", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });

        return v;
    }

    private void getList(){
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference("PlayLists");
        Query query = mReference.orderByChild("email").equalTo(email);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getChildren() != null && snapshot.getChildren().iterator().hasNext()){
                    listAdapter.resetList();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        MyAlbumData mld = ds.getValue(MyAlbumData.class);
                        assert mld != null;
                        listAdapter.addItemToList(mld, ds.getKey());
                    }
                    song_to_list_view.setAdapter(listAdapter);
                    listAdapter.notifyDataSetChanged();
                    song_to_list_text.setVisibility(View.GONE);
                } else {
                    song_to_list_text.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bottomSheetBehavior = BottomSheetBehavior.from((View) (view.getParent()));
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.setPeekHeight(view.getMeasuredHeight());
        bottomSheetBehavior.setDraggable(false);

        view.findViewById(R.id.song_to_list_dismiss).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }
}