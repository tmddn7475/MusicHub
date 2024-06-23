package com.example.capstone.Fragment1;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.capstone.Activity.AlbumEditActivity;
import com.example.capstone.Data.AccountData;
import com.example.capstone.MainActivity;
import com.example.capstone.Adapter.MusicListAdapter;
import com.example.capstone.Data.MusicListAdapterData;
import com.example.capstone.Data.MyAlbumData;
import com.example.capstone.Data.PlayListDB;
import com.example.capstone.Fragment2.EtcFragment;
import com.example.capstone.Interface.MusicListListener;
import com.example.capstone.Interface.MusicListener;
import com.example.capstone.Data.AlbumToSongData;
import com.example.capstone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AlbumFragment extends Fragment implements MusicListListener {

    TextView list_name, list_artist, list_track_num, list_description, list_show_more;
    ImageView list_thumnail, list_play_btn, list_edit_btn, list_back_btn;
    ListView list_trackList;
    Dialog dialog;
    MusicListAdapter musicListAdapter;
    int track_num = 0;
    String key;

    PlayListDB playListDB;
    SQLiteDatabase database;
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
        View v = inflater.inflate(R.layout.fragment_album, container, false);

        dialog = new Dialog(requireActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 다이어로그에 타이틀 안 나오게 하기
        dialog.setContentView(R.layout.progress_layout2);
        dialog.show();
        dialog.setCancelable(false);

        key = getArguments().getString("key");
        musicListAdapter = new MusicListAdapter(getContext(), this);

        list_name = v.findViewById(R.id.list_name);
        list_artist = v.findViewById(R.id.list_artist);
        list_track_num = v.findViewById(R.id.list_track_num);
        list_description = v.findViewById(R.id.list_description);
        list_thumnail = v.findViewById(R.id.list_thumnail);
        list_play_btn = v.findViewById(R.id.list_play_btn);
        list_edit_btn = v.findViewById(R.id.list_edit_btn);
        list_trackList = v.findViewById(R.id.list_track);
        list_back_btn = v.findViewById(R.id.list_back_btn);
        list_show_more = v.findViewById(R.id.list_show_more);

        playListDB = new PlayListDB(getActivity());
        database = playListDB.getWritableDatabase();

        // 뒤로 가기
        list_back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });

        // 리스트 재생
        list_play_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i = musicListAdapter.filterList.size() - 1; i >= 0; i--){
                    String sql = "select * from playlist where songUrl = '" + musicListAdapter.filterList.get(i).getSongUrl() + "'";
                    Cursor cursor = database.rawQuery(sql, null);

                    if(cursor.getCount() < 1){
                        playListDB.addPlaylist_song(musicListAdapter.filterList.get(i));
                    }
                    if(i == 0){
                        musicListener.sendMessage(musicListAdapter.filterList.get(i).getSongUrl());
                    }
                }
                Toast.makeText(getContext(), "곡이 리스트에 추가되었습니다\n중복이 있을 경우 추가되지 않습니다", Toast.LENGTH_SHORT).show();
            }
        });

        // 리스트 수정
        list_edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AlbumEditActivity.class);
                intent.putExtra("key", key);
                startActivity(intent);
            }
        });
        
        // 리스트 정보 가져오기
        assert key != null;
        FirebaseDatabase.getInstance().getReference("PlayLists").orderByKey().equalTo(key).limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    MyAlbumData mld = ds.getValue(MyAlbumData.class);
                    assert mld != null;

                    list_description.setText(mld.getDescription());
                    list_name.setText(mld.getListName());
                    list_artist.setText(mld.getNickname());
                    Glide.with(getActivity()).load(mld.getImageUrl()).into(list_thumnail);

                    if(mld.getEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())){
                        list_edit_btn.setVisibility(View.VISIBLE);
                    } else {
                        list_edit_btn.setVisibility(View.GONE);
                    }
                }
                if(list_description.getLineCount() == 1){
                    list_show_more.setVisibility(View.GONE);
                } else {
                    showMore();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        // 앨범 곡 가져오기
        FirebaseDatabase.getInstance().getReference("PlayLists_song").orderByChild("key").equalTo(key)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        track_num = 0;
                        musicListAdapter.resetList();
                        if (snapshot.getChildren() != null && snapshot.getChildren().iterator().hasNext()){
                            //exist
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                AlbumToSongData mld = ds.getValue(AlbumToSongData.class);
                                assert mld != null;
                                getTracks(mld.getSongUrl());
                            }
                            list_play_btn.setVisibility(View.VISIBLE);
                            dialog.dismiss();
                        } else {
                            //not exist
                            list_track_num.setText("0곡");
                            list_play_btn.setVisibility(View.GONE);
                            dialog.dismiss();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "오류가 발생했습니다! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });

        list_show_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMore();
            }
        });

        return v;
    }

    private void showMore(){
        if (list_description.getMaxLines() == 1) {
            list_description.setMaxLines(Integer.MAX_VALUE);
            list_description.setEllipsize(null);
            list_show_more.setText("닫기");
        } else {
            list_description.setMaxLines(1);
            list_description.setEllipsize(TextUtils.TruncateAt.END);
            list_show_more.setText("더 보기");
        }
    }

    private void getTracks(String url){
        FirebaseDatabase.getInstance().getReference("Songs").orderByChild("songUrl").equalTo(url).limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            MusicListAdapterData mld = ds.getValue(MusicListAdapterData.class);
                            assert mld != null;
                            musicListAdapter.addItemToList(mld);
                            track_num++;
                        }
                        list_track_num.setText(track_num + "곡");
                        list_trackList.setAdapter(musicListAdapter);
                        musicListAdapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "오류가 발생했습니다! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
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

    private void back(){
        MainActivity mainActivity = (MainActivity) getContext();
        assert mainActivity != null;
        FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
        fragmentManager.beginTransaction().remove(this).commit();
        fragmentManager.popBackStack();
    }
}