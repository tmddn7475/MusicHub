package com.example.capstone.Fragment2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.media3.session.MediaController;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.capstone.Interface.MusicListener;
import com.example.capstone.Interface.PlayListListener;
import com.example.capstone.MainActivity;
import com.example.capstone.Adapter.ItemTouchHelperCallback;
import com.example.capstone.Data.MusicListAdapterData;
import com.example.capstone.Adapter.PlayListAdapter;
import com.example.capstone.Data.PlayListDB;
import com.example.capstone.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PlayListFragment extends BottomSheetDialogFragment implements PlayListListener {

    public static SeekBar playlist_progress;
    public static ImageView playlist_play_btn;
    MediaController mediaController;

    BottomSheetBehavior bottomSheetBehavior;
    PlayListAdapter playListAdapter;
    public static PlayListDB playListDB;
    RecyclerView playlist;
    ImageView playlist_media_btn, playlist_skip_next_btn, playlist_skip_previous_btn;
    MusicListener musicListener;
    ItemTouchHelper helper;

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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_play_list, container, false);

        playlist = v.findViewById(R.id.playlist);
        playlist_progress = v.findViewById(R.id.playlist_progress);
        playlist_play_btn = v.findViewById(R.id.playlist_play_btn);
        playlist_skip_next_btn = v.findViewById(R.id.playlist_skip_next_btn);
        playlist_skip_previous_btn = v.findViewById(R.id.playlist_skip_previous_btn);

        if (mediaController != null) {
            if(mediaController.isPlaying()){
                playlist_play_btn.setImageResource(R.drawable.pause);
            } else {
                playlist_play_btn.setImageResource(R.drawable.play_arrow);
            }
        }

        // 플레이 버튼
        playlist_play_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaController != null) {
                    if (mediaController.isPlaying()) {
                        mediaController.pause();
                        playlist_play_btn.setImageResource(R.drawable.play_arrow);
                    } else {
                        mediaController.play();
                        playlist_play_btn.setImageResource(R.drawable.pause);
                    }
                } else {
                    Toast.makeText(getContext(), "MediaController is not connected yet.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 전 곡
        playlist_skip_previous_btn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View v) {
                musicListener.previousMessage();
                playlist_progress.setProgress(0);
            }
        });
        
        // 다음 곡
        playlist_skip_next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicListener.nextMessage();
                playlist_progress.setProgress(0);
            }
        });

        playlist_progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(mediaController != null){
                    mediaController.seekTo(seekBar.getProgress());
                }
            }
        });

        // DB, 리스트
        playListAdapter = new PlayListAdapter(getContext(), this);
        playListDB = new PlayListDB(getContext());
        SQLiteDatabase database = playListDB.getWritableDatabase();
        String sql = "select * from playlist";
        Cursor cursor = database.rawQuery(sql, null);
        while(cursor.moveToNext()){
            getTrack(cursor.getString(1), cursor.getString(2));
        }
        helper = new ItemTouchHelper(new ItemTouchHelperCallback(playListAdapter));
        helper.attachToRecyclerView(playlist);

        // media로 돌아가기
        playlist_media_btn = v.findViewById(R.id.playlist_media_btn);
        playlist_media_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) getContext();
                assert mainActivity != null;
                FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();

                MediaFragment mediaFragment = MainActivity.mediaFragment;

                mediaFragment.setMediaController(mediaController);
                mediaFragment.show(fragmentManager, mediaFragment.getTag());
                dismiss();
            }
        });

        return v;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setUpCurrent(String url){
        playListAdapter.selectedItem = playListAdapter.getNumber(url);
        playListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bottomSheetBehavior = BottomSheetBehavior.from((View) (view.getParent()));
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.setPeekHeight(view.getMeasuredHeight());
        bottomSheetBehavior.setDraggable(false);

        view.findViewById(R.id.play_list_down_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    private void getTrack(String url, String time){
        FirebaseDatabase.getInstance().getReference("Songs").orderByChild("songUrl").equalTo(url).limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            MusicListAdapterData mld = ds.getValue(MusicListAdapterData.class);
                            assert mld != null;
                            playListAdapter.addItem(mld, time);
                        }
                        playListAdapter.sort();
                        playlist.setAdapter(playListAdapter);
                        playListAdapter.notifyDataSetChanged();
                        setUpCurrent(MainActivity.current_url);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    @Override
    public void sendMessage(String message) {
        musicListener.sendMessage(message);
        setUpCurrent(message);
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