package com.example.capstone.Adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.capstone.Account.AccountFragment;
import com.example.capstone.Data.AccountData;
import com.example.capstone.MainActivity;
import com.example.capstone.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class FeedAccountAdapter extends RecyclerView.Adapter<FeedAccountAdapter.ViewHolder>{

    public FeedAccountAdapter(ArrayList<AccountData> mList) {
        this.mList = mList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView feed_account_image;
        TextView feed_account_name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            feed_account_image = itemView.findViewById(R.id.feed_account_image);
            feed_account_name = itemView.findViewById(R.id.feed_account_name);
        }
    }
    private ArrayList<AccountData> mList = null;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.feed_account_recycler, parent, false);
        FeedAccountAdapter.ViewHolder vh = new FeedAccountAdapter.ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AccountData item = mList.get(position);

        if (item.getImageUrl().equals("")){
            holder.feed_account_image.setImageResource(R.drawable.baseline_account_circle_24);
        } else {
            Glide.with(holder.itemView).load(item.getImageUrl()).into(holder.feed_account_image);
        }
        holder.feed_account_name.setText(item.getNickname());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) v.getContext();
                assert mainActivity != null;
                FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
                AccountFragment accountFragment = new AccountFragment();

                Bundle bundle = new Bundle();
                bundle.putString("email", item.getEmail());
                accountFragment.setArguments(bundle);

                fragmentManager.beginTransaction().replace(R.id.container, accountFragment).addToBackStack(null).commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}
