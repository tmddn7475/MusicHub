package com.example.capstone.Fragment1;

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

import com.example.capstone.Account.AccountFragment;
import com.example.capstone.Adapter.AccountListAdapter;
import com.example.capstone.Data.AccountData;
import com.example.capstone.Data.FollowData;
import com.example.capstone.MainActivity;
import com.example.capstone.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FollowerFragment extends Fragment {

    AccountListAdapter accountListAdapter;
    ImageView follower_back_btn;
    String email;
    ListView follower_list;
    TextView textView1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_follower, container, false);

        follower_back_btn = v.findViewById(R.id.follower_back_btn);
        follower_list = v.findViewById(R.id.follower_list);
        textView1 = v.findViewById(R.id.textView1);

        accountListAdapter = new AccountListAdapter();
        email = getArguments().getString("email");

        FirebaseDatabase.getInstance().getReference("Follow").orderByChild("follow").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.getChildren() != null && snapshot.getChildren().iterator().hasNext()){
                            for(DataSnapshot ds: snapshot.getChildren()){
                                FollowData followData = ds.getValue(FollowData.class);
                                assert followData != null;
                                getAccount(followData.getEmail());
                            }
                            textView1.setVisibility(View.GONE);
                        } else {
                            textView1.setVisibility(View.VISIBLE);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        follower_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AccountData data = (AccountData) follower_list.getItemAtPosition(position);

                MainActivity mainActivity = (MainActivity) getContext();
                assert mainActivity != null;
                FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
                AccountFragment accountFragment = new AccountFragment();

                Bundle bundle = new Bundle();
                bundle.putString("email", data.getEmail());
                accountFragment.setArguments(bundle);

                fragmentManager.beginTransaction().replace(R.id.container, accountFragment).addToBackStack(null).commit();
            }
        });

        follower_back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });

        return v;
    }

    private void back(){
        MainActivity mainActivity = (MainActivity) getContext();
        assert mainActivity != null;
        FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
        fragmentManager.beginTransaction().remove(this).commit();
        fragmentManager.popBackStack();
    }

    void getAccount(String email){
        FirebaseDatabase.getInstance().getReference("accounts").orderByChild("email").equalTo(email).limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            AccountData accountData = ds.getValue(AccountData.class);
                            accountListAdapter.addItemToList(accountData);
                        }
                        follower_list.setAdapter(accountListAdapter);
                        accountListAdapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}