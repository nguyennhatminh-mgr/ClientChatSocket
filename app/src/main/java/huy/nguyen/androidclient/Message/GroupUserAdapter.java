package huy.nguyen.androidclient.Message;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import huy.nguyen.androidclient.R;

public class GroupUserAdapter extends RecyclerView.Adapter<GroupUserAdapter.GroupUserViewHolder> {

    private ArrayList<String> listAccountName;
    Context context;

    public GroupUserAdapter( Context context,ArrayList<String> listAccountName) {
        this.listAccountName = listAccountName;
        this.context = context;
    }

    @NonNull
    @Override
    public GroupUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_in_group,parent,false);
        return new GroupUserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupUserViewHolder holder, int position) {
        String accoutName=listAccountName.get(position);
        String temp="";
        if(accoutName.length()>6){
            temp=accoutName.substring(0,6)+"...";
        }
        else{
            temp=accoutName;
        }
        holder.txtAccountname.setText(temp);

    }

    @Override
    public int getItemCount() {
        return listAccountName.size();
    }

    public class GroupUserViewHolder extends RecyclerView.ViewHolder{

        ImageView imgAvatar;
        TextView txtAccountname;
        public GroupUserViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar=itemView.findViewById(R.id.imgAvartarItemUserInGroup);
            txtAccountname=itemView.findViewById(R.id.txtAccoutnameItemUserInGroup);
        }
    }
}
