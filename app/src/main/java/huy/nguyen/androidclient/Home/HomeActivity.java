package huy.nguyen.androidclient.Home;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import huy.nguyen.androidclient.MainActivity;
import huy.nguyen.androidclient.Model.UserInfo;
import huy.nguyen.androidclient.R;
import huy.nguyen.androidclient.Utilities.Interface.OnlineUserCallback;
import huy.nguyen.androidclient.Utilities.SocketUtil;
import huy.nguyen.androidclient.Utilities.SocketWriter;
import huy.nguyen.androidclient.Utilities.ThreadManager;

public class HomeActivity extends AppCompatActivity {

    ArrayList<UserInfo> userArrayList;
    HomeUserAdpter homeUserAdpter;
    ListView listView;
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
        initMyServerSocket();
        listView = findViewById(R.id.lvListUserInHome);
        userArrayList = new ArrayList<>();
        homeUserAdpter = new HomeUserAdpter(HomeActivity.this, userArrayList);
        listView.setAdapter(homeUserAdpter);
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
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final UserInfo info = (UserInfo) homeUserAdpter.getItem(position);
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                if (info.isNewMessage()) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            PrintWriter writer = SocketWriter.writer.get(info.getIp());
                            writer.write(RESPONSE_CHAT+"\n");
                            writer.flush();
                        }
                    }).start();
                    intent.putExtra("Create",false);
                } else{
                    intent.putExtra("Create",true);
                }
                intent.putExtra("PeerIp",info.getIp());
                startActivity(intent);

            }
        });
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
            PrintWriter writer;
            String[] arrIp = socket.getRemoteSocketAddress().toString().split(":");
            final String ip = arrIp[0].substring(1);
            try {
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream());
                SocketWriter.writer.put(ip,writer);
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
