package com.example.capstone.Account;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.capstone.Activity.AccountEditActivity;
import com.example.capstone.Activity.UploadActivity;
import com.example.capstone.Fragment1.FollowerFragment;
import com.example.capstone.Fragment1.FollowingFragment;
import com.example.capstone.Static_command;
import com.example.capstone.Fragment2.EtcFragment;
import com.example.capstone.Adapter.ViewPagerAdapter;
import com.example.capstone.MainActivity;
import com.example.capstone.Data.AccountData;
import com.example.capstone.Interface.MusicListListener;
import com.example.capstone.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountFragment extends Fragment implements MusicListListener {
    TextView account_name, account_followers, account_info, account_show_more, account_following;
    ImageView account_back_btn, account_edit, account_follow, account_upload;
    CircleImageView account_circleImage;
    Dialog dialog;
    boolean follow_check = false;
    String follow_key, getEmail;

    TabLayout tabLayout;
    ViewPager2 viewPager;
    ViewPagerAdapter viewPagerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_account, container, false);

        account_edit = v.findViewById(R.id.account_edit);
        account_name = v.findViewById(R.id.account_name);
        account_back_btn = v.findViewById(R.id.account_back_btn);
        account_upload = v.findViewById(R.id.account_upload);
        account_circleImage = v.findViewById(R.id.account_circleImage);
        account_follow = v.findViewById(R.id.account_follow);
        account_info = v.findViewById(R.id.account_info);
        account_followers = v.findViewById(R.id.account_followers);
        account_following = v.findViewById(R.id.account_following);
        account_show_more = v.findViewById(R.id.account_show_more);

        assert getArguments() != null;
        getEmail = getArguments().getString("email");

        // viewPager
        tabLayout = v.findViewById(R.id.album_edit_tabLayout);
        viewPager = v.findViewById(R.id.account_viewPager);

        AccountTrackFragment trackFragment = new AccountTrackFragment();
        AccountAlbumFragment listFragment = new AccountAlbumFragment();

        Bundle bundle = new Bundle();
        bundle.putString("email", getEmail);
        trackFragment.setArguments(bundle);
        listFragment.setArguments(bundle);

        viewPagerAdapter = new ViewPagerAdapter(getActivity());
        viewPagerAdapter.addFragment(trackFragment, "곡");
        viewPagerAdapter.addFragment(listFragment, "앨범");

        viewPager.setAdapter(viewPagerAdapter);
        TabLayoutMediator tm = new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(viewPagerAdapter.getTitle(position)) ;
        });
        tm.attach();

        // 로딩창
        dialog = new Dialog(requireActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 다이어로그에 타이틀 안 나오게 하기
        dialog.setContentView(R.layout.progress_layout2);
        dialog.show();
        dialog.setCancelable(false);

        // 계정 정보
        getAccountData(getEmail);
        account_back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });

        // 업로드
        account_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UploadActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        // 계정 정보 수정
        account_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AccountEditActivity.class);
                startActivity(intent);
            }
        });

        // 팔로우
        account_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (follow_check){
                    Static_command.checkUnfollow(follow_key);
                    account_follow.setImageResource(R.drawable.baseline_person_add_24);
                    follow_check = false;
                    Toast.makeText(getContext(), "해당 계정을 언팔로우했습니다", Toast.LENGTH_SHORT).show();
                } else {
                    Static_command.checkFollow(getEmail);
                    account_follow.setImageResource(R.drawable.baseline_person_add_disabled_24);
                    follow_check = true;
                    Toast.makeText(getContext(), "해당 계정을 팔로우했습니다", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 팔로우
        account_followers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) getContext();
                assert mainActivity != null;
                FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
                FollowerFragment followerFragment = new FollowerFragment();

                Bundle bundle = new Bundle();
                bundle.putString("email", getEmail);
                followerFragment.setArguments(bundle);

                fragmentManager.beginTransaction().replace(R.id.container, followerFragment).addToBackStack(null).commit();
            }
        });

        account_following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) getContext();
                assert mainActivity != null;
                FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
                FollowingFragment followingFragment = new FollowingFragment();

                Bundle bundle = new Bundle();
                bundle.putString("email", getEmail);
                followingFragment.setArguments(bundle);

                fragmentManager.beginTransaction().replace(R.id.container, followingFragment).addToBackStack(null).commit();
            }
        });

        // 더 보기
        account_show_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMore();
            }
        });

        return v;
    }

    private void showMore(){
        if (account_info.getMaxLines() == 1) {
            account_info.setMaxLines(Integer.MAX_VALUE);
            account_info.setEllipsize(null);
            account_show_more.setText("닫기");
        } else {
            account_info.setMaxLines(1);
            account_info.setEllipsize(TextUtils.TruncateAt.END);
            account_show_more.setText("더 보기");
        }
    }

    private void back(){
        MainActivity mainActivity = (MainActivity) getContext();
        assert mainActivity != null;
        FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
        fragmentManager.beginTransaction().remove(this).commit();
        fragmentManager.popBackStack();
    }

    // 계정 정보 가져오기
    private void getAccountData(String email){
        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference("accounts");
        Query query = mReference.orderByChild("email").equalTo(email);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    AccountData data = ds.getValue(AccountData.class);
                    assert data != null;

                    account_name.setText(data.getNickname());

                    if(data.getEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())){
                        account_edit.setVisibility(View.VISIBLE);
                        account_upload.setVisibility(View.VISIBLE);
                        account_follow.setVisibility(View.GONE);
                    } else {
                        account_edit.setVisibility(View.GONE);
                        account_upload.setVisibility(View.GONE);
                        account_follow.setVisibility(View.VISIBLE);
                        setFollow(FirebaseAuth.getInstance().getCurrentUser().getEmail(), data.getEmail());
                    }

                    if(data.getImageUrl().equals("")){
                        account_circleImage.setImageResource(R.drawable.baseline_account_circle_24);
                    } else {
                        Glide.with(getActivity()).load(data.getImageUrl()).into(account_circleImage);
                    }
                    if(data.getInfo().equals("")){
                        account_info.setVisibility(View.GONE);
                    } else {
                        account_info.setVisibility(View.VISIBLE);
                        account_info.setText(data.getInfo());
                    }
                }
                if(account_info.getLineCount() == 1){
                    account_show_more.setVisibility(View.GONE);
                } else {
                    showMore();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // 팔로워
        FirebaseDatabase.getInstance().getReference("Follow").orderByChild("follow").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int num = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    num++;
                }

                if(num < 2){
                    account_followers.setText(num + " follower" + " · ");
                } else {
                    account_followers.setText(num + " followers" + " · ");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // 팔로잉
        FirebaseDatabase.getInstance().getReference("Follow").orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int num = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    num++;
                }

                if(num < 2){
                    account_following.setText(num + " following");
                } else {
                    account_followers.setText(num + " following");
                }
                dialog.dismiss();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
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
                        account_follow.setImageResource(R.drawable.baseline_person_add_disabled_24);
                        follow_check = true;
                    }
                } else {
                    //not exist
                    account_follow.setImageResource(R.drawable.baseline_person_add_24);
                    follow_check = false;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}