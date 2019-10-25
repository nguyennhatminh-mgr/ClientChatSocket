package huy.nguyen.androidclient.Home;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
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
import huy.nguyen.androidclient.Utilities.ThreadManager;

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

    private static final String REQUEST_CHAT = "REQUEST_CHAT";
    private static final String RESPONSE_CHAT = "RESPONSE_CHAT";

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
                    String[] arrIp = socket.getRemoteSocketAddress().toString().split(":");
                    String ip = arrIp[0].substring(1);
                    SocketUtil.socketMap.put(ip,socket);
                    ServerThread serverThread = new ServerThread(socket);
                    ThreadManager.threadList.put(ip,serverThread);
                    serverThread.start();
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
            String[] arrIp = socket.getRemoteSocketAddress().toString().split(":");
            final String ip = arrIp[0].substring(1);
            try {
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String msg;
                while ((msg = input.readLine()) != null) {
                    if (msg.equals(REQUEST_CHAT)) {
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
                            }
                        });
                        break;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
