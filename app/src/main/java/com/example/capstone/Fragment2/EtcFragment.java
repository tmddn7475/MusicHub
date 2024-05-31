package com.example.capstone.Fragment2;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.capstone.Static_command;
import com.example.capstone.MainActivity;
import com.example.capstone.Data.MusicListAdapterData;
import com.example.capstone.Data.PlayListDB;
import com.example.capstone.R;
import com.example.capstone.Account.AccountFragment;
import com.example.capstone.Fragment1.SongInfoFragment;
import com.example.capstone.Activity.SongEditActivity;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class EtcFragment extends BottomSheetDialogFragment {

    BottomSheetBehavior bottomSheetBehavior;
    Button etc_song_info, etc_artist_info;
    ImageView etc_song_thumnail, etc_like_img;
    TextView etc_song_name, etc_song_artist, etc_add_my_list, etc_add_playlist, etc_comment, etc_like, etc_edit;
    String email, like_key;
    FragmentManager fragmentManager;
    MainActivity mainActivity;
    boolean like_check;
    PlayListDB playListDB;
    SQLiteDatabase database;
    MusicListAdapterData data;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_etc, container, false);

        etc_song_thumnail = v.findViewById(R.id.etc_song_thumnail);
        etc_song_name = v.findViewById(R.id.etc_song_name);
        etc_song_artist = v.findViewById(R.id.etc_song_artist);
        etc_song_info = v.findViewById(R.id.etc_song_info);
        etc_artist_info = v.findViewById(R.id.etc_artist_info);
        etc_add_my_list = v.findViewById(R.id.etc_add_my_list);
        etc_like = v.findViewById(R.id.etc_like);
        etc_like_img = v.findViewById(R.id.etc_like_img);
        etc_comment = v.findViewById(R.id.etc_comment);
        etc_edit = v.findViewById(R.id.etc_edit);
        etc_add_playlist = v.findViewById(R.id.etc_add_playlist);

        mainActivity = (MainActivity) getContext();
        assert mainActivity != null;
        fragmentManager = mainActivity.getSupportFragmentManager();

        playListDB = new PlayListDB(getActivity());
        database = playListDB.getWritableDatabase();

        String getUrl = getArguments().getString("url");
        setUp(getUrl);

        // 곡정보
        etc_song_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SongInfoFragment songInfoFragment = new SongInfoFragment();
                Bundle bundle = new Bundle();
                bundle.putString("url", getUrl);
                songInfoFragment.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.container, songInfoFragment).addToBackStack(null).commit();

                if(MainActivity.mediaFragment.isAdded()){
                    MainActivity.mediaFragment.dismiss();
                }
                if(MainActivity.playListFragment.isAdded()){
                    MainActivity.playListFragment.dismiss();
                }
                dismiss();
            }
        });

        // 아티스트 정보
        etc_artist_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AccountFragment accountFragment = new AccountFragment();
                Bundle bundle = new Bundle();
                bundle.putString("email", email);
                accountFragment.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.container, accountFragment).addToBackStack(null).commit();

                if(MainActivity.mediaFragment.isAdded()){
                    MainActivity.mediaFragment.dismiss();
                }

                if(MainActivity.playListFragment.isAdded()){
                    MainActivity.playListFragment.dismiss();
                }
                dismiss();
            }
        });

        // 내 리스트에 담기
        etc_add_my_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SongToAlbumFragment songToAlbumFragment = new SongToAlbumFragment();
                Bundle bundle = new Bundle();
                bundle.putString("url", getUrl);
                songToAlbumFragment.setArguments(bundle);
                songToAlbumFragment.show(fragmentManager, songToAlbumFragment.getTag());
            }
        });

        // 재생목록에 담기
        etc_add_playlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sql = "select * from playlist where songUrl = '" + data.getSongUrl() + "'";
                Cursor cursor = database.rawQuery(sql, null);
                if(cursor.getCount() < 1){
                    playListDB.addPlaylist_song(data);
                }
                Toast.makeText(getContext(), "재생목록에 추가되었습니다\n중복이 있을 경우 추가되지 않습니다", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });

        // 좋아요
        etc_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (like_check){
                    Static_command.checkUnlike(like_key);
                    etc_like_img.setImageResource(R.drawable.baseline_favorite_border_24);
                    like_check = false;
                    Toast.makeText(getContext(), "해당 곡이 좋아요에 삭제되었습니다", Toast.LENGTH_SHORT).show();
                } else {
                    Static_command.checkLike(getUrl);
                    etc_like_img.setImageResource(R.drawable.baseline_favorite_24);
                    like_check = true;
                    Toast.makeText(getContext(), "해당 곡이 좋아요에 추가되었습니다", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 댓글 보기
        etc_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommentsFragment commentsFragment = new CommentsFragment();
                Bundle bundle = new Bundle();
                bundle.putString("url", getUrl);
                commentsFragment.setArguments(bundle);
                commentsFragment.show(fragmentManager, commentsFragment.getTag());
            }
        });

        // 곡 정보 수정
        etc_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), SongEditActivity.class);
                intent.putExtra("url", data.getSongUrl());
                startActivity(intent);
            }
        });

        return v;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bottomSheetBehavior = BottomSheetBehavior.from((View) (view.getParent()));
        bottomSheetBehavior.setMaxWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        view.findViewById(R.id.etc_dismiss_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    public void setUp(String url){
        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference("Songs");
        Query query = mReference.orderByChild("songUrl").equalTo(url).limitToFirst(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    data = ds.getValue(MusicListAdapterData.class);
                    assert data != null;

                    Glide.with(requireActivity()).load(data.getImageUrl()).into(etc_song_thumnail);
                    etc_song_name.setText(data.getSongName());
                    etc_song_artist.setText(data.getSongArtist());
                    email = data.getEmail();

                    if(data.getEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())){
                        etc_edit.setVisibility(View.VISIBLE);
                    } else {
                        etc_edit.setVisibility(View.GONE);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        String get_email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        DatabaseReference mReference2 = FirebaseDatabase.getInstance().getReference("Like");
        Query query2 = mReference2.orderByChild("email_songUrl").equalTo(get_email+"_"+url).limitToFirst(1);
        query2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getChildren() != null && snapshot.getChildren().iterator().hasNext()){
                    //exist
                    for(DataSnapshot ds: snapshot.getChildren()){
                        like_key = ds.getKey();
                        etc_like_img.setImageResource(R.drawable.baseline_favorite_24);
                        like_check = true;
                    }
                } else {
                    //not exist
                    etc_like_img.setImageResource(R.drawable.baseline_favorite_border_24);
                    like_check = false;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}