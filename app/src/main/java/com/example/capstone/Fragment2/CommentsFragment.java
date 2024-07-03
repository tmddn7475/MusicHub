package com.example.capstone.Fragment2;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.capstone.Static_command;
import com.example.capstone.Data.AccountData;
import com.example.capstone.Data.MusicListAdapterData;
import com.example.capstone.Adapter.CommentAdapter;
import com.example.capstone.Data.CommentAdapterData;
import com.example.capstone.Interface.CommentListener;
import com.example.capstone.MainActivity;
import com.example.capstone.R;
import com.example.capstone.Account.AccountFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class CommentsFragment extends BottomSheetDialogFragment implements CommentListener {
    BottomSheetBehavior bottomSheetBehavior;
    RecyclerView comment_recycler;
    ImageView comment_song_thumnail, comment_send;
    EditText comment_edit;
    TextView comment_song_name, comment_song_artist;
    String name, imageUrl;
    CommentAdapter commentAdapter;

    CommentListener commentListener = this;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_comments, container, false);

        comment_send = v.findViewById(R.id.comment_send);
        comment_edit = v.findViewById(R.id.comment_edit);
        comment_recycler = v.findViewById(R.id.comment_recycler);
        comment_song_thumnail = v.findViewById(R.id.comment_song_thumnail);
        comment_song_name = v.findViewById(R.id.comment_song_name);
        comment_song_artist = v.findViewById(R.id.comment_song_artist);

        set_song_comment(getArguments().getString("url"));
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        getAccount(email);

        comment_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment = comment_edit.getText().toString();
                if(comment.isEmpty()){
                    Toast.makeText(getActivity(), "내용을 적어주세요", Toast.LENGTH_SHORT).show();
                    uploadComment(FirebaseAuth.getInstance().getCurrentUser().getEmail(),
                            name, imageUrl, comment, Static_command.getTime(), getArguments().getString("url"));
                    comment_edit.setText("");
                } else {
                    uploadComment(FirebaseAuth.getInstance().getCurrentUser().getEmail(),
                            name, imageUrl, comment, Static_command.getTime(), getArguments().getString("url"));
                    comment_edit.setText("");
                }
            }
        });

        return v;
    }

    void getAccount(String email){
        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference("accounts");
        Query query = mReference.orderByChild("email").equalTo(email);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    AccountData data = ds.getValue(AccountData.class);
                    assert data != null;
                    name = data.getNickname();
                    imageUrl = data.getImageUrl();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void uploadComment(String email, String name, String url, String comment, String time, String song){
        CommentAdapterData data = new CommentAdapterData(email,name,url,comment,time,song);
        FirebaseDatabase.getInstance().getReference("Comments")
                .push().setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getContext(), "등록 완료 되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private void set_song_comment(String url){
        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference("Songs");
        Query query = mReference.orderByChild("songUrl").equalTo(url);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    MusicListAdapterData mld = ds.getValue(MusicListAdapterData.class);
                    assert mld != null;

                    if(getActivity() == null){
                        return;
                    }
                    Glide.with(getActivity()).load(mld.getImageUrl()).into(comment_song_thumnail);
                    comment_song_name.setText(mld.getSongName());
                    comment_song_artist.setText(mld.getSongArtist());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        DatabaseReference mReference2 = FirebaseDatabase.getInstance().getReference("Comments");
        Query query2 = mReference2.orderByChild("songUrl").equalTo(url);
        query2.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentAdapter = new CommentAdapter(getContext(), commentListener);
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String key = ds.getKey();
                    CommentAdapterData mld = ds.getValue(CommentAdapterData.class);
                    assert mld != null;
                    commentAdapter.addItem(mld, key);
                    Log.i("data", String.valueOf(mld.getComment()));
                }
                comment_recycler.setAdapter(commentAdapter);
                commentAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bottomSheetBehavior = BottomSheetBehavior.from((View) (view.getParent()));
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.setPeekHeight(view.getMeasuredHeight());
        bottomSheetBehavior.setDraggable(false);

        view.findViewById(R.id.comment_down_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    @Override
    public void GoProfile(String email) {
        MainActivity mainActivity = (MainActivity) getContext();
        assert mainActivity != null;
        FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();

        AccountFragment accountFragment = new AccountFragment();
        Bundle bundle = new Bundle();
        bundle.putString("email", email);
        accountFragment.setArguments(bundle);
        fragmentManager.beginTransaction().replace(R.id.container, accountFragment).addToBackStack(null).commit();

        if(MainActivity.etcFragment.isAdded()){
            MainActivity.etcFragment.dismiss();
        }
        if(MainActivity.mediaFragment.isAdded()){
            MainActivity.mediaFragment.dismiss();
        }
        if(MainActivity.playListFragment.isAdded()){
            MainActivity.playListFragment.dismiss();
        }
        dismiss();
    }
}