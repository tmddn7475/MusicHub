package com.example.capstone.Fragment2;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.media3.session.MediaController;

import android.os.IBinder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.capstone.Interface.MusicListener;
import com.example.capstone.MainActivity;
import com.example.capstone.Data.MusicListAdapterData;
import com.example.capstone.R;
import com.example.capstone.Static_command;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MediaFragment extends BottomSheetDialogFragment {

    BottomSheetBehavior bottomSheetBehavior;
    ImageView media_thumbnail, media_etc_btn, skip_previous_btn, skip_next_btn, media_playlist_btn,
            media_comment, media_follow, media_like_btn;
    TextView media_song_name, media_song_artist, media_song_duration;
    public static TextView media_song_current;
    public static SeekBar media_seekbar;
    public static ImageView media_play_btn;

    MediaController mediaController;
    MusicListener musicListener;
    boolean like_check;
    boolean follow_check = false;
    String like_key, follow_key, song_email;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            musicListener = (MusicListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString());
        }
    }

    public void setMediaController(MediaController mediaController) {
        this.mediaController = mediaController;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_media, container, false);

        media_playlist_btn = v.findViewById(R.id.media_playlist_btn);
        media_thumbnail = v.findViewById(R.id.media_song_thumnail);
        media_song_name = v.findViewById(R.id.media_song_name);
        media_song_artist = v.findViewById(R.id.media_song_artist);
        media_song_duration = v.findViewById(R.id.media_song_duration);
        media_song_current = v.findViewById(R.id.media_song_current);
        media_seekbar = v.findViewById(R.id.media_seekbar);
        media_play_btn = v.findViewById(R.id.playlist_media_play_btn);
        skip_next_btn = v.findViewById(R.id.playlist_skip_next_btn);
        skip_previous_btn = v.findViewById(R.id.playlist_skip_previous_btn);
        media_comment = v.findViewById(R.id.media_comment);
        media_etc_btn = v.findViewById(R.id.media_etc_btn);
        media_follow = v.findViewById(R.id.media_follow);
        media_like_btn = v.findViewById(R.id.media_like_btn);

        media_song_name.setSingleLine(true);    // 한줄로 표시하기
        media_song_name.setEllipsize(TextUtils.TruncateAt.MARQUEE); // 흐르게 만들기
        media_song_name.setSelected(true);

        setUpUi(MainActivity.current_url);

        if (mediaController != null) {
            if(mediaController.isPlaying()){
                media_play_btn.setImageResource(R.drawable.pause);
            } else {
                media_play_btn.setImageResource(R.drawable.play_arrow);
            }
        }

        // 재생 바 
        media_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(mediaController != null){
                    mediaController.seekTo(seekBar.getProgress());
                }
            }
        });

        media_play_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaController != null) {
                    if (mediaController.isPlaying()) {
                        mediaController.pause();
                        media_play_btn.setImageResource(R.drawable.play_arrow);
                    } else {
                        mediaController.play();
                        media_play_btn.setImageResource(R.drawable.pause);
                    }
                }
            }
        });

        // 전 곡
        skip_previous_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicListener.previousMessage();
                media_seekbar.setProgress(0);
            }
        });

        // 다음 곡
        skip_next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicListener.nextMessage();
                media_seekbar.setProgress(0);
            }
        });

        // 댓글
        media_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) getContext();
                assert mainActivity != null;
                FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
                CommentsFragment commentsFragment = new CommentsFragment();

                Bundle bundle = new Bundle();
                bundle.putString("url", MainActivity.current_url);
                commentsFragment.setArguments(bundle);

                commentsFragment.show(fragmentManager, commentsFragment.getTag());
            }
        });

        // 좋아요 체크
        media_like_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (like_check){
                    Static_command.checkUnlike(like_key);
                    media_like_btn.setImageResource(R.drawable.baseline_favorite_border_24);
                    like_check = false;
                    Toast.makeText(getContext(), "해당 곡이 좋아요에 삭제되었습니다", Toast.LENGTH_SHORT).show();
                } else {
                    Static_command.checkLike(MainActivity.current_url);
                    media_like_btn.setImageResource(R.drawable.baseline_favorite_24);
                    like_check = true;
                    Toast.makeText(getContext(), "해당 곡이 좋아요에 추가되었습니다", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 팔로우
        media_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (follow_check){
                    Static_command.checkUnfollow(follow_key);
                    media_follow.setImageResource(R.drawable.baseline_person_add_24);
                    follow_check = false;
                    Toast.makeText(getContext(), "해당 계정을 언팔로우했습니다", Toast.LENGTH_SHORT).show();
                } else {
                    Static_command.checkFollow(song_email);
                    media_follow.setImageResource(R.drawable.baseline_person_add_disabled_24);
                    follow_check = true;
                    Toast.makeText(getContext(), "해당 계정을 팔로우했습니다", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 기타 이동
        media_etc_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) getContext();
                assert mainActivity != null;
                FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
                EtcFragment etcFragment = MainActivity.etcFragment;

                if(!etcFragment.isAdded()){
                    Bundle bundle = new Bundle();
                    bundle.putString("url", MainActivity.current_url);
                    etcFragment.setArguments(bundle);

                    etcFragment.show(fragmentManager, etcFragment.getTag());
                }
            }
        });

        // 재생목록으로 이동
        media_playlist_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) getContext();
                assert mainActivity != null;
                FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();

                PlayListFragment playListFragment = MainActivity.playListFragment;

                playListFragment.setMediaController(mediaController);
                playListFragment.show(fragmentManager, playListFragment.getTag());
                dismiss();
            }
        });

        return v;
    }

    public void setUpUi(String url){
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference("Songs");
        Query query = mReference.orderByChild("songUrl").equalTo(url).limitToFirst(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    MusicListAdapterData mld = ds.getValue(MusicListAdapterData.class);
                    assert mld != null;
                    if(getActivity() == null){
                        return;
                    }
                    Glide.with(getActivity()).load(mld.getImageUrl()).into(media_thumbnail);
                    media_song_name.setText(mld.getSongName());
                    media_song_artist.setText(mld.getSongArtist());
                    media_song_duration.setText(mld.getSongDuration());
                    song_email = mld.getEmail();

                    if(mld.getEmail().equals(email)){
                        media_follow.setVisibility(View.GONE);
                    } else {
                        setFollow(email, mld.getEmail());
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        DatabaseReference mReference2 = FirebaseDatabase.getInstance().getReference("Like");
        Query query2 = mReference2.orderByChild("email_songUrl").equalTo(email+"_"+url).limitToFirst(1);
        query2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getChildren() != null && snapshot.getChildren().iterator().hasNext()){
                    //exist
                    for(DataSnapshot ds: snapshot.getChildren()){
                        like_key = ds.getKey();
                        media_like_btn.setImageResource(R.drawable.baseline_favorite_24);
                        like_check = true;
                    }
                } else {
                    //not exist
                    media_like_btn.setImageResource(R.drawable.baseline_favorite_border_24);
                    like_check = false;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setFollow(String email, String follow){
        String email_follow = email + "_" + follow;
        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference("Follow");
        Query query = mReference.orderByChild("email_follow").equalTo(email_follow).limitToFirst(1);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getChildren() != null && snapshot.getChildren().iterator().hasNext()){
                    //exist
                    for(DataSnapshot ds: snapshot.getChildren()){
                        follow_key = ds.getKey();
                        media_follow.setImageResource(R.drawable.baseline_person_add_disabled_24);
                        follow_check = true;
                    }
                } else {
                    //not exist
                    media_follow.setImageResource(R.drawable.baseline_person_add_24);
                    follow_check = false;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bottomSheetBehavior = BottomSheetBehavior.from((View) (view.getParent()));
        bottomSheetBehavior.setMaxWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        view.findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }
}