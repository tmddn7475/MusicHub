package com.example.capstone.Adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.capstone.Fragment1.CategoryFragment;
import com.example.capstone.Data.CategoryData;
import com.example.capstone.MainActivity;
import com.example.capstone.R;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder>{
    public CategoryAdapter(ArrayList<CategoryData> mList) {
        this.mList = mList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView category_image;
        TextView category_text;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            category_image = itemView.findViewById(R.id.category_image);
            category_text = itemView.findViewById(R.id.category_text);
        }
    }
    private ArrayList<CategoryData> mList = null;

    @NonNull
    @Override
    public CategoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.category_layout, parent, false);
        CategoryAdapter.ViewHolder vh = new CategoryAdapter.ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryData item = mList.get(position);
        Context context = holder.itemView.getContext();

        int imageID = context.getResources().getIdentifier(item.getCimage(), "drawable", context.getPackageName());
        holder.category_text.setText(item.getCname());
        holder.category_image.setImageResource(imageID);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) v.getContext();
                assert mainActivity != null;
                FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
                CategoryFragment categoryFragment = new CategoryFragment();

                Bundle bundle = new Bundle();
                bundle.putString("category", item.getCname());
                categoryFragment.setArguments(bundle);

                fragmentManager.beginTransaction().replace(R.id.container, categoryFragment).addToBackStack(null).commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}
