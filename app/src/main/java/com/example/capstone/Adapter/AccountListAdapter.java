package com.example.capstone.Adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.capstone.Data.AccountData;
import com.example.capstone.R;

import java.util.ArrayList;
import de.hdodenhof.circleimageview.CircleImageView;

public class AccountListAdapter extends BaseAdapter {
    public ArrayList<AccountData> list = new ArrayList<AccountData>();

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final Context context = parent.getContext();
        // exercise_listview를 inflate 해서 view를 참조한다
        if(view == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.search_account_list, parent, false);
        }

        TextView account_text;
        CircleImageView circleImageView;

        account_text = view.findViewById(R.id.account_text);
        circleImageView = view.findViewById(R.id.circleImageView);
        AccountData accountData = list.get(position);

        account_text.setText(accountData.getNickname());
        if(accountData.getImageUrl().equals("")){
            circleImageView.setImageResource(R.drawable.baseline_account_circle_24);
        } else {
            Glide.with(view).load(accountData.getImageUrl()).into(circleImageView);
        }

        account_text.setSingleLine(true);    // 한줄로 표시하기
        account_text.setEllipsize(TextUtils.TruncateAt.MARQUEE); // 흐르게 만들기
        account_text.setSelected(true);      // 선택하기

        return view;
    }

    public void resetList(){
        list.clear();
    }

    public void addItemToList(AccountData accountData){
        list.add(accountData);
    }
}
