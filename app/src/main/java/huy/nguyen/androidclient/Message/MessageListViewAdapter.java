package huy.nguyen.androidclient.Message;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import huy.nguyen.androidclient.Model.Message;
import huy.nguyen.androidclient.R;

public class MessageListViewAdapter extends BaseAdapter {
    ArrayList<Message> messages;
    Context context;

    public MessageListViewAdapter(Context context,ArrayList<Message> messages) {
        this.messages = messages;
        this.context = context;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int i) {
        return messages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater layoutInflater= (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        Message message=messages.get(i);
        if(!message.isBelongsToCurrentUser()){
            view=layoutInflater.inflate(R.layout.item_message,null);
            TextView txtItemMessage=view.findViewById(R.id.txtItemMessage);
            txtItemMessage.setText(message.getTextMessage());
        }
        else{
            view=layoutInflater.inflate(R.layout.my_item_message,null);
            TextView txtMyItemMessage=view.findViewById(R.id.txtMyItemMessage);
            txtMyItemMessage.setText(message.getTextMessage());
        }
        return view;
    }
}
//class MessageListViewHolder{
//
//}
