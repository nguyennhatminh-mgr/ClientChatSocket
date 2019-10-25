package huy.nguyen.androidclient.Home;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;

import huy.nguyen.androidclient.MainActivity;
import huy.nguyen.androidclient.Model.AllMessage;
import huy.nguyen.androidclient.Model.Message;
import huy.nguyen.androidclient.Model.UserInfo;
import huy.nguyen.androidclient.R;
import huy.nguyen.androidclient.ServerActivity;
import huy.nguyen.androidclient.Utilities.Interface.OnlineUserCallback;
import huy.nguyen.androidclient.Utilities.SocketUtil;

public class HomeActivity extends AppCompatActivity {

    ArrayList<UserInfo> userArrayList;
    HomeUserAdpter homeUserAdpter;
    RecyclerView recyclerView;
    Socket conn;
    ServerSocket serverSocket;
    Thread Thread1 = null;
    private static final String NOTICE_MSG = "NOTICE_MSG";
    private static final String END_MSG = "END_MSG";
    private static final String END_SOCKET = "END_SOCKET";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        recyclerView = findViewById(R.id.rcvListUserInHome);
        userArrayList = new ArrayList<>();
        homeUserAdpter = new HomeUserAdpter(HomeActivity.this, userArrayList);
        recyclerView.setAdapter(homeUserAdpter);
        recyclerView.setLayoutManager(new LinearLayoutManager(HomeActivity.this, LinearLayoutManager.VERTICAL, false));
//        fakeData();
        SocketUtil.retriveOnlineUser(new OnlineUserCallback() {
            @Override
            public void retriveOnlineList(final ArrayList<UserInfo> onlineList) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        userArrayList.clear();
                        userArrayList.addAll(onlineList);
                        Toast.makeText(HomeActivity.this, userArrayList.toString(), Toast.LENGTH_SHORT).show();
                        homeUserAdpter.notifyDataSetChanged();
                    }
                });

            }
        });
        initMyServerSocket();
    }

    private void initMyServerSocket() {
        Thread1 = new Thread(new Thread1());
        Thread1.start();
    }

    class Thread1 implements Runnable {
        @Override
        public void run() {
            Socket socket;
            try {
                serverSocket = new ServerSocket(8080);
                try {
                    socket = serverSocket.accept();
                    Log.e("123", "new user " );
                    new ServerThread(socket).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ServerThread extends Thread {
        private Socket socket;

        private ServerThread(Socket clientSocket) {
            this.socket = clientSocket;
        }

        public void run() {
            BufferedReader input;
            PrintWriter output;
            String[] arrIp = socket.getRemoteSocketAddress().toString().split(":");
            final String ip = arrIp[0].substring(1);
            try {
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String msg;
                while (!(msg = input.readLine()).equals(END_SOCKET)) {
                    if (msg.equals(NOTICE_MSG)) {
                        ArrayList<Message> curMess;
                        Map<String, ArrayList<Message>> all = AllMessage.allMessage;
                        if (all.containsKey(ip)) curMess = all.get(ip);
                        else curMess = new ArrayList<>();
                        while (!(msg = input.readLine()).equals(END_MSG)) {
                            curMess.add(new Message(msg, false));
                        }
                        AllMessage.allMessage.put(ip, curMess);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                for (int i = 0; i < userArrayList.size(); i++) {
                                    UserInfo info = userArrayList.get(i);
                                    if (info.getIp().equals(ip)) {
                                        info.setNewMessage(true);
                                        userArrayList.set(i, info);
                                        break;
                                    }
                                }
                                homeUserAdpter.notifyDataSetChanged();
                                MainActivity.messageListViewAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
