package com.example.capstone.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.capstone.Data.CommentAdapterData;
import com.example.capstone.Interface.CommentListener;
import com.example.capstone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder>{

    private final ArrayList<CommentAdapterData> mList = new ArrayList<CommentAdapterData>();
    private final ArrayList<String> keyList = new ArrayList<String>();

    CommentListener commentListener;
    Context context;
    String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

    public CommentAdapter(Context context, CommentListener commentListener){
        this.context = context;
        this.commentListener = commentListener;
    }

    public void addItem(CommentAdapterData item, String key){
        mList.add(item);
        keyList.add(key);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.comment_layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CommentAdapterData item = mList.get(position);
        holder.setItem(item);
        int pos = position;

        if(email.equals(item.getEmail())){
            holder.comment_delete.setVisibility(View.VISIBLE);
        } else {
            holder.comment_delete.setVisibility(View.GONE);
        }

        holder.comment_user_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commentListener.GoProfile(item.getEmail());
            }
        });

        holder.comment_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert_ex = new AlertDialog.Builder(context);
                alert_ex.setMessage("해당 댓글을 삭제하시겠습니까?");
                alert_ex.setNegativeButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteComment(keyList.get(pos));
                    }
                });
                alert_ex.setPositiveButton("아니요", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { } });

                AlertDialog alert = alert_ex.create();
                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.DKGRAY));
                alert.show();
            }
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView comment_user_image, comment_delete;
        TextView comment_user_name, comment_text;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            comment_delete = itemView.findViewById(R.id.comment_delete);
            comment_user_image = itemView.findViewById(R.id.comment_user_image);
            comment_user_name = itemView.findViewById(R.id.comment_user_name);
            comment_text = itemView.findViewById(R.id.comment_text);

            comment_user_name.setSingleLine(true);    // 한줄로 표시하기
            comment_user_name.setEllipsize(TextUtils.TruncateAt.MARQUEE); // 흐르게 만들기
            comment_user_name.setSelected(true);      // 선택하기
        }

        @SuppressLint("SetTextI18n")
        public void setItem(CommentAdapterData item){
            if(item.getImageUrl().equals("")){
                comment_user_image.setImageResource(R.drawable.baseline_account_circle_24);
            } else {
                Glide.with(itemView).load(item.getImageUrl()).into(comment_user_image);
            }
            comment_user_name.setText(item.getNickname() + " · " + item.getTime());
            comment_text.setText(item.getComment());
        }
    }

    private void deleteComment(String key){
        FirebaseDatabase.getInstance().getReference("Comments").child(key).removeValue();
    }
}
