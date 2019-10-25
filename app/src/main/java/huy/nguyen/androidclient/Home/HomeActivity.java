package huy.nguyen.androidclient.Home;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;

import huy.nguyen.androidclient.Model.User;
import huy.nguyen.androidclient.Model.UserInfo;
import huy.nguyen.androidclient.R;
import huy.nguyen.androidclient.Utilities.OnlineUserCallback;
import huy.nguyen.androidclient.Utilities.SocketUtil;

public class HomeActivity extends AppCompatActivity {

    ArrayList<UserInfo> userArrayList;
    HomeUserAdpter homeUserAdpter;
    RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        recyclerView=findViewById(R.id.rcvListUserInHome);
        userArrayList=new ArrayList<>();
        homeUserAdpter=new HomeUserAdpter(HomeActivity.this,userArrayList);
        recyclerView.setAdapter(homeUserAdpter);
        recyclerView.setLayoutManager(new LinearLayoutManager(HomeActivity.this,LinearLayoutManager.VERTICAL,false));
//        fakeData();
        SocketUtil.retriveOnlineUser(new OnlineUserCallback() {
            @Override
            public void retriveOnlineList(final ArrayList<UserInfo> onlineList) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        userArrayList.clear();
                        userArrayList.addAll(onlineList);
                        Toast.makeText(HomeActivity.this,userArrayList.toString(),Toast.LENGTH_SHORT).show();
                        homeUserAdpter.notifyDataSetChanged();
                    }
                });

            }
        });
    }

    private void fakeData() {
        userArrayList.add(new UserInfo("Huy"));
        userArrayList.add(new UserInfo("Minh"));
        userArrayList.add(new UserInfo("Nhân"));
        userArrayList.add(new UserInfo("Huy"));
        userArrayList.add(new UserInfo("Minh"));
        userArrayList.add(new UserInfo("Nhân"));
        userArrayList.add(new UserInfo("Huy"));
        userArrayList.add(new UserInfo("Minh"));

    }
}
