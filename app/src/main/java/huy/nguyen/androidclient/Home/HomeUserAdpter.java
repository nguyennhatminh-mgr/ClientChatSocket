package huy.nguyen.androidclient.Home;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import huy.nguyen.androidclient.Model.Message;
import huy.nguyen.androidclient.Model.UserInfo;
import huy.nguyen.androidclient.R;

public class HomeUserAdpter extends BaseAdapter {
    ArrayList<UserInfo> userInfos;
    Context context;

    public HomeUserAdpter(Context context,ArrayList<UserInfo> userInfos) {
        this.userInfos = userInfos;
        this.context = context;
    }

    @Override
    public int getCount() {
        return userInfos.size();
    }

    @Override
    public Object getItem(int i) {
        return userInfos.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater layoutInflater= (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        UserInfo userInfo=userInfos.get(i);
        view=layoutInflater.inflate(R.layout.item_user_in_home,null);
        TextView txtAccountname=view.findViewById(R.id.txtAccountnameInHomeItem);
        txtAccountname.setText(userInfo.getAccountname());

        return view;
    }
}
//class MessageListViewHolder{
//
//}
