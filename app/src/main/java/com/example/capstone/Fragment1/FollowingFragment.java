package com.example.capstone.Fragment1;

import android.media.Image;
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
import android.widget.Toast;

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

import org.w3c.dom.Text;

public class FollowingFragment extends Fragment {

    AccountListAdapter accountListAdapter;
    ImageView following_back_btn;
    String email;
    ListView following_list;
    TextView textView1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_following, container, false);

        following_back_btn = v.findViewById(R.id.following_back_btn);
        following_list = v.findViewById(R.id.following_list);
        textView1 = v.findViewById(R.id.textView1);

        accountListAdapter = new AccountListAdapter();
        email = getArguments().getString("email");

        FirebaseDatabase.getInstance().getReference("Follow").orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.getChildren() != null && snapshot.getChildren().iterator().hasNext()){
                            for(DataSnapshot ds: snapshot.getChildren()){
                                FollowData followData = ds.getValue(FollowData.class);
                                assert followData != null;
                                getAccount(followData.getFollow());
                            }
                            textView1.setVisibility(View.GONE);
                        } else {
                            textView1.setVisibility(View.VISIBLE);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        following_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AccountData data = (AccountData) following_list.getItemAtPosition(position);

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

        following_back_btn.setOnClickListener(new View.OnClickListener() {
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
                        following_list.setAdapter(accountListAdapter);
                        accountListAdapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }
}