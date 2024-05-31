package com.example.capstone.Fragment1;

import static androidx.core.app.ActivityCompat.finishAffinity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.capstone.Account.AccountFragment;
import com.example.capstone.Activity.LoginActivity;
import com.example.capstone.Activity.UploadActivity;
import com.example.capstone.Data.AccountData;
import com.example.capstone.Data.FollowData;
import com.example.capstone.Data.MusicListAdapterData;
import com.example.capstone.Adapter.FeedAccountAdapter;
import com.example.capstone.Adapter.FeedListAdapter;
import com.example.capstone.Data.PlayListDB;
import com.example.capstone.Interface.MusicListener;
import com.example.capstone.MainActivity;
import com.example.capstone.Service.MusicService;
import com.example.capstone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FeedFragment extends Fragment {

    RecyclerView feed_account_recycler;
    ArrayList<AccountData> mList;
    FeedAccountAdapter feedAccountAdapter;
    Dialog dialog;
    ImageView feed_logout, feed_upload, feed_account;
    TextView feed_text;
    ListView feed_list;
    FeedListAdapter feedListAdapter;
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
        View v = inflater.inflate(R.layout.fragment_feed, container, false);

        feed_logout = v.findViewById(R.id.feed_logout);
        feed_upload = v.findViewById(R.id.feed_upload);
        feed_account = v.findViewById(R.id.feed_account);
        feed_text = v.findViewById(R.id.feed_text);

        dialog = new Dialog(requireActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 다이어로그에 타이틀 안 나오게 하기
        dialog.setContentView(R.layout.progress_layout2);
        dialog.show();
        dialog.setCancelable(false);

        // account
        feed_account_recycler = v.findViewById(R.id.feed_account_recycler);
        feed_account_recycler.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));	// 가로
        mList = new ArrayList<>();
        feedAccountAdapter = new FeedAccountAdapter(mList);

        // song list
        feed_list = v.findViewById(R.id.feed_list);
        feedListAdapter = new FeedListAdapter(getContext(), musicListener);
        feed_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MainActivity mainActivity = (MainActivity) getContext();
                assert mainActivity != null;
                FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
                SongInfoFragment songInfoFragment = new SongInfoFragment();

                Bundle bundle = new Bundle();
                bundle.putString("url", feedListAdapter.getUrl(position));
                songInfoFragment.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.container, songInfoFragment).addToBackStack(null).commit();
            }
        });
        addItem();
        click();

        return v;
    }

    void click(){
        // 업로드
        feed_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UploadActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        //계정
        feed_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) getContext();
                assert mainActivity != null;
                FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
                AccountFragment accountFragment = new AccountFragment();

                Bundle bundle = new Bundle();
                bundle.putString("email", FirebaseAuth.getInstance().getCurrentUser().getEmail());
                accountFragment.setArguments(bundle);

                fragmentManager.beginTransaction().replace(R.id.container, accountFragment).addToBackStack(null).commit();
            }
        });
        // 로그아웃
        feed_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert_ex = new AlertDialog.Builder(getActivity());
                alert_ex.setMessage("로그아웃하시겠습니까?");
                alert_ex.setNegativeButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PlayListDB playListDB = new PlayListDB(getContext());
                        playListDB.deleteAll();
                        SharedPreferences preferences = getContext().getSharedPreferences("pref", Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("url", "");
                        editor.apply(); // 저장

                        FirebaseAuth.getInstance().signOut();

                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finishAffinity(getActivity());
                        Intent intent2 = new Intent(getActivity(), MusicService.class);
                        getActivity().stopService(intent2);
                    }
                });
                alert_ex.setPositiveButton("아니요", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                });
                AlertDialog alert = alert_ex.create();
                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.DKGRAY));
                alert.show();
            }
        });
    }


    // 팔로우 
    private void addItem(){
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference("accounts");
        Query query = mReference.orderByChild("email").equalTo(email);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    AccountData data = ds.getValue(AccountData.class);
                    assert data != null;
                    if(getActivity() == null){
                        return;
                    }
                    if(data.getImageUrl().equals("")){
                        feed_account.setImageResource(R.drawable.baseline_account_circle_24);
                    } else {
                        Glide.with(getActivity()).load(data.getImageUrl()).into(feed_account);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        FirebaseDatabase.getInstance().getReference("Follow").orderByChild("email")
                .equalTo(email).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        mList.clear();
                        feedListAdapter.resetList();
                        if(snapshot.getChildren() != null && snapshot.getChildren().iterator().hasNext()){
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                FollowData data = ds.getValue(FollowData.class);
                                String follow_email = data.getFollow();
                                addItem2(follow_email);
                            }
                            feed_text.setVisibility(View.GONE);
                            dialog.dismiss();
                        } else {
                            feed_text.setVisibility(View.VISIBLE);
                            dialog.dismiss();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "오류가 발생했습니다! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
    }

    // 곡 정보 가져오기
    private void addItem2(String email){
        FirebaseDatabase.getInstance().getReference("accounts").orderByChild("email")
                .equalTo(email).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            AccountData data = ds.getValue(AccountData.class);
                            mList.add(data);
                        }
                        feed_account_recycler.setAdapter(feedAccountAdapter);
                        feedAccountAdapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "오류가 발생했습니다! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });

        FirebaseDatabase.getInstance().getReference("Songs").orderByChild("email")
                .equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            MusicListAdapterData mld = ds.getValue(MusicListAdapterData.class);
                            assert mld != null;
                            feedListAdapter.addItemToList(mld);
                        }
                        feedListAdapter.sort();
                        feed_list.setAdapter(feedListAdapter);
                        feedListAdapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "오류가 발생했습니다! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
    }
}