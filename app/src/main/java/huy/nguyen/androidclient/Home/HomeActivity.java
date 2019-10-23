package huy.nguyen.androidclient.Home;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;

import huy.nguyen.androidclient.Model.User;
import huy.nguyen.androidclient.R;

public class HomeActivity extends AppCompatActivity {

    ArrayList<User> userArrayList;
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
        fakeData();
    }

    private void fakeData() {
        userArrayList.add(new User("Huy"));
        userArrayList.add(new User("Minh"));
    }
}
