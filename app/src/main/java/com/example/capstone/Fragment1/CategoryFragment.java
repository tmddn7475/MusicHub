package com.example.capstone.Fragment1;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CategoryFragment extends Fragment implements MusicListListener {

    ImageView category_image, category_back_btn;
    TextView category_name, category_text;
    ListView category_list;
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
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_category, container, false);

        category_text = v.findViewById(R.id.category_text);
        category_image = v.findViewById(R.id.category_image);
        category_back_btn = v.findViewById(R.id.category_back_btn);
        category_name = v.findViewById(R.id.category_name);
        category_list = v.findViewById(R.id.category_list);

        musicListAdapter = new MusicListAdapter(getActivity(), this);

        assert getArguments() != null;
        String str = getArguments().getString("category");

        category_name.setText(str);
        getImage(str);
        getSongs(str);

        category_back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });

        category_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MusicListAdapterData data = (MusicListAdapterData) category_list.getItemAtPosition(position);
                String url = data.getSongUrl();
                musicListener.sendMessage(url);
            }
        });

        return v;
    }

    private void getSongs(String str){
        FirebaseDatabase.getInstance().getReference("Songs").orderByChild("songCategory").equalTo(str)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        musicListAdapter.resetList();
                        if(snapshot.getChildren() != null && snapshot.getChildren().iterator().hasNext()){
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                MusicListAdapterData mld = ds.getValue(MusicListAdapterData.class);
                                assert mld != null;
                                musicListAdapter.addItemToList(mld);
                            }
                            category_list.setAdapter(musicListAdapter);
                            musicListAdapter.notifyDataSetChanged();
                            category_text.setVisibility(View.GONE);
                            category_list.setVisibility(View.VISIBLE);
                        } else {
                            category_text.setVisibility(View.VISIBLE);
                            category_list.setVisibility(View.GONE);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void getImage(String str){
        switch (str) {
            case "Ambient":
                category_image.setImageResource(R.drawable.ambient);
                break;
            case "Classical":
                category_image.setImageResource(R.drawable.classical);
                break;
            case "Disco":
                category_image.setImageResource(R.drawable.disco);
                break;
            case "Dance & EDM":
                category_image.setImageResource(R.drawable.edm);
                break;
            case "Hip hop":
                category_image.setImageResource(R.drawable.hiphop);
                break;
            case "Jazz":
                category_image.setImageResource(R.drawable.jazz);
                break;
            case "R&B":
                category_image.setImageResource(R.drawable.rnb);
                break;
            case "Reggae":
                category_image.setImageResource(R.drawable.reggae);
                break;
            case "Rock":
                category_image.setImageResource(R.drawable.rock);
                break;
        }
    }

    // 전 프래그먼트로 돌아가기
    private void back(){
        MainActivity mainActivity = (MainActivity) getContext();
        assert mainActivity != null;
        FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
        fragmentManager.beginTransaction().remove(this).commit();
        fragmentManager.popBackStack();
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