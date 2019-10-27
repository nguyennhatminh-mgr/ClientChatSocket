package huy.nguyen.androidclient.Home;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import huy.nguyen.androidclient.Model.GroupChat;
import huy.nguyen.androidclient.Model.UserInfo;
import huy.nguyen.androidclient.R;
import huy.nguyen.androidclient.Utilities.Interface.OnlineUserCallback;
import huy.nguyen.androidclient.Utilities.SocketUtil;

public class GroupChatActivity extends AppCompatActivity {

    ArrayList<UserInfo> userInfoArrayList;
    HomeUserAdpter homeUserAdpter;
    ListView listView;
    ArrayList<UserInfo> listGroupUser;
    CheckBox checkBox;
    Button btnCreateGroup;
    EditText groupName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        listView=findViewById(R.id.lstUserInGroup);
        btnCreateGroup=findViewById(R.id.btnCreateGroupInGroup);
        groupName=findViewById(R.id.edtNameGroup);

        userInfoArrayList=new ArrayList<>();
        listGroupUser=new ArrayList<>();
        homeUserAdpter=new HomeUserAdpter(GroupChatActivity.this,userInfoArrayList);
        listView.setAdapter(homeUserAdpter);

        SocketUtil.retriveOnlineUser(new OnlineUserCallback() {
            @Override
            public void retriveOnlineList(ArrayList<UserInfo> onlineList) {
                userInfoArrayList.addAll(onlineList);
                homeUserAdpter.notifyDataSetChanged();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                UserInfo userInfo= (UserInfo) homeUserAdpter.getItem(i);
                listGroupUser.add(userInfo);
                checkBox=view.findViewById(R.id.chkAddToGroup);
                checkBox.setVisibility(View.VISIBLE);
                checkBox.setChecked(true);
            }
        });
        btnCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GroupChat groupChat=new GroupChat(groupName.getText().toString(),listGroupUser);
                //TODO
                finish();
            }
        });
    }
}
