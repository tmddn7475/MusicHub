package com.example.capstone.Library;

import android.content.Intent;
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

import com.example.capstone.MainActivity;
import com.example.capstone.Adapter.MyAlbumAdapter;
import com.example.capstone.Activity.AddAlbumActivity;
import com.example.capstone.Data.MyAlbumData;
import com.example.capstone.Fragment1.AlbumFragment;
import com.example.capstone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MyAlbumFragment extends Fragment {

    public static MyAlbumFragment newInstance() {
        return new MyAlbumFragment();
    }

    TextView add_my_list, my_list_text;
    ListView my_list_view;
    MyAlbumAdapter myAlbumAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_my_album, container, false);

        my_list_view = v.findViewById(R.id.my_list_view);
        my_list_text = v.findViewById(R.id.my_list_text);
        add_my_list = v.findViewById(R.id.add_my_list);

        myAlbumAdapter = new MyAlbumAdapter();
        getList();

        // 내 리스트 추가
        add_my_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), AddAlbumActivity.class);
                startActivity(intent);
            }
        });

        my_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MainActivity mainActivity = (MainActivity) getContext();
                assert mainActivity != null;
                FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
                AlbumFragment albumFragment = new AlbumFragment();

                Bundle bundle = new Bundle();
                bundle.putString("key", myAlbumAdapter.getKey(position));

                albumFragment.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.container, albumFragment).addToBackStack(null).commit();
            }
        });

        return v;
    }

    private void getList(){
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference("PlayLists");
        Query query = mReference.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myAlbumAdapter.resetList();
                if(snapshot.getChildren() != null && snapshot.getChildren().iterator().hasNext()){
                    //exist
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        MyAlbumData mld = ds.getValue(MyAlbumData.class);
                        assert mld != null;
                        myAlbumAdapter.addItemToList(mld, ds.getKey());
                    }
                    my_list_view.setAdapter(myAlbumAdapter);
                    my_list_text.setVisibility(View.GONE);
                    myAlbumAdapter.notifyDataSetChanged();
                } else {
                    //not exist
                    my_list_text.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}