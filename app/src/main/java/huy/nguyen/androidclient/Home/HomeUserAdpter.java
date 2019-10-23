package huy.nguyen.androidclient.Home;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import huy.nguyen.androidclient.Model.Message;
import huy.nguyen.androidclient.Model.User;
import huy.nguyen.androidclient.R;

public class HomeUserAdpter extends RecyclerView.Adapter<HomeUserAdpter.MessageViewHolder> {

    ArrayList<User> usersList;
    Context context;
    public HomeUserAdpter(Context context, ArrayList<User> arrayList){
        this.context=context;
        this.usersList=arrayList;
    }

//    public void add(Message message){
//        this.messageList.add(message);
//        notifyDataSetChanged();
//    }
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=View.inflate(parent.getContext(),R.layout.item_user_in_home,null);
        return new MessageViewHolder(view,viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        User user=usersList.get(position);
        holder.txtAccountname.setText(user.getName());
//        holder.imgItemMessage.setImageResource(message.getUser().getUrl());
        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                Toast.makeText(context,"Click"+position,Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView txtAccountname;
        ImageView imgOfUser;
        ItemClickListener itemClickListener;
        public MessageViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            txtAccountname=itemView.findViewById(R.id.txtAccountnameInHomeItem);
            imgOfUser=itemView.findViewById(R.id.imgUserInHomeItem);
            itemView.setOnClickListener(this);
        }
        public void setItemClickListener(ItemClickListener itemClickListener){
            this.itemClickListener=itemClickListener;
        }

        @Override
        public void onClick(View view) {
            itemClickListener.onClick(view,getAdapterPosition());
        }
    }
}
