package com.example.capstone.Search;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.capstone.Account.AccountFragment;
import com.example.capstone.Adapter.AccountListAdapter;
import com.example.capstone.Data.AccountData;
import com.example.capstone.MainActivity;
import com.example.capstone.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SearchAccountFragment extends Fragment {

    ListView search_account_list;
    AccountListAdapter accountListAdapter;
    TextView none;
    String query;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search_account, container, false);

        query = getArguments().getString("search").toLowerCase();
        none = v.findViewById(R.id.none);
        search_account_list = v.findViewById(R.id.search_account_list);
        accountListAdapter = new AccountListAdapter();

        FirebaseDatabase.getInstance().getReference("accounts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                accountListAdapter.resetList();
                for(DataSnapshot ds: snapshot.getChildren()){
                    AccountData data = ds.getValue(AccountData.class);
                    assert data != null;
                    if(data.getNickname().toLowerCase().contains(query)){
                        accountListAdapter.addItemToList(data);
                    }
                }
                search_account_list.setAdapter(accountListAdapter);
                accountListAdapter.notifyDataSetChanged();
                if(accountListAdapter.getCount() == 0){
                    none.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        search_account_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AccountData data = (AccountData) search_account_list.getItemAtPosition(position);

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

        return v;
    }
}