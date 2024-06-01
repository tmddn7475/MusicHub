package com.example.capstone.Fragment1;

import static androidx.core.app.ActivityCompat.finishAffinity;

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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.capstone.Account.AccountFragment;
import com.example.capstone.Activity.LoginActivity;
import com.example.capstone.Adapter.CategoryAdapter;
import com.example.capstone.Data.CategoryData;
import com.example.capstone.Data.PlayListDB;
import com.example.capstone.Fragment2.EtcFragment;
import com.example.capstone.MainActivity;
import com.example.capstone.Activity.UploadActivity;
import com.example.capstone.Data.AccountData;
import com.example.capstone.Adapter.MusicListAdapter;
import com.example.capstone.Data.MusicListAdapterData;
import com.example.capstone.Interface.MusicListener;
import com.example.capstone.Interface.MusicListListener;
import com.example.capstone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomeFragment extends Fragment implements MusicListListener {

    MusicListAdapter musicListAdapter;
    ListView songsList;
    Dialog dialog;
    MusicListener musicListener;
    ImageView upload, account, logout;

    ArrayList<CategoryData> categoryList;
    CategoryAdapter categoryAdapter;
    RecyclerView category_recycler;

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
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        // 로딩 창
        dialog = new Dialog(requireActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 다이어로그에 타이틀 안 나오게 하기
        dialog.setContentView(R.layout.progress_layout2);
        dialog.show();
        dialog.setCancelable(false);
        
        // 카테고리
        category_recycler = v.findViewById(R.id.category_recycler);
        category_recycler.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));	// 가로
        categoryList = new ArrayList<>();
        categoryList.add(new CategoryData("ambient", "Ambient"));
        categoryList.add(new CategoryData("classical", "Classical"));
        categoryList.add(new CategoryData("disco", "Disco"));
        categoryList.add(new CategoryData("edm", "Dance & EDM"));
        categoryList.add(new CategoryData("hiphop", "Hip hop"));
        categoryList.add(new CategoryData("jazz", "Jazz"));
        categoryList.add(new CategoryData("rnb", "R&B"));
        categoryList.add(new CategoryData("reggae", "Reggae"));
        categoryList.add(new CategoryData("rock", "Rock"));

        categoryAdapter = new CategoryAdapter(categoryList);
        category_recycler.setAdapter(categoryAdapter);

        // 곡
        upload = v.findViewById(R.id.home_upload);
        account = v.findViewById(R.id.home_account);
        logout = v.findViewById(R.id.home_logout);
        songsList = v.findViewById(R.id.songsList);

        getAccountImage(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        click();

        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference("Songs");
        musicListAdapter = new MusicListAdapter(getContext(), this);
        mReference.limitToLast(10).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                musicListAdapter.resetList();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    MusicListAdapterData mld = ds.getValue(MusicListAdapterData.class);
                    assert mld != null;
                    musicListAdapter.addItemToList2(mld);
                }
                songsList.setAdapter(musicListAdapter);
                musicListAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "오류가 발생했습니다! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        songsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MusicListAdapterData data = (MusicListAdapterData) songsList.getItemAtPosition(position);
                String url = data.getSongUrl();
                musicListener.sendMessage(url);
            }
        });

        return v;
    }

    private void getAccountImage(String email){
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
                        account.setImageResource(R.drawable.baseline_account_circle_24);
                    } else {
                        Glide.with(getActivity()).load(data.getImageUrl()).into(account);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "오류가 발생했습니다! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    void click(){
        // 업로드
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UploadActivity.class);
                startActivity(intent);
            }
        });
        //계정
        account.setOnClickListener(new View.OnClickListener() {
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
        logout.setOnClickListener(new View.OnClickListener() {
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
                        startActivity(intent);
                        finishAffinity(getActivity());
                        MainActivity.mediaController.stop();
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