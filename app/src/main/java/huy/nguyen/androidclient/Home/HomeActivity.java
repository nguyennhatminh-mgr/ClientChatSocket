package huy.nguyen.androidclient.Home;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
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
import huy.nguyen.androidclient.Utilities.SocketProtocol;
import huy.nguyen.androidclient.Utilities.SocketReader;
import huy.nguyen.androidclient.Utilities.SocketUtil;
import huy.nguyen.androidclient.Utilities.SocketWriter;
import huy.nguyen.androidclient.Utilities.ThreadManager;

import static huy.nguyen.androidclient.Utilities.SocketUtil.socketMap;

public class HomeActivity extends AppCompatActivity {

    public static ArrayList<UserInfo> userArrayList;
    HomeUserAdpter homeUserAdpter;
    ListView listView;
    Button button;
    Socket conn;
    ServerSocket serverSocket;
    Thread Thread1 = null;
    private static final String NOTICE_MSG = "NOTICE_MSG";
    private static final String END_MSG = "END_MSG";

    private static final String REQUEST_CHAT = "REQUEST_CHAT";
    private static final String RESPONSE_CHAT = "RESPONSE_CHAT";
    private static final String END_SOCKET = "END_SOCKET";

    private boolean res = false;
    private boolean buttonPressed = false;

    @Override
    protected void onResume() {
        super.onResume();
        buttonPressed = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        listView = findViewById(R.id.lvListUserInHome);
        button = findViewById(R.id.button);
        userArrayList = new ArrayList<>();
        homeUserAdpter = new HomeUserAdpter(HomeActivity.this, userArrayList);
        listView.setAdapter(homeUserAdpter);
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
                Toast.makeText(HomeActivity.this, info.isNewMessage() ? "1" : "0", Toast.LENGTH_SHORT).show();
                if (info.isNewMessage()) {
                    Log.e("hello", "4");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            PrintWriter writer = SocketWriter.writer.get(info.getIp());
                            writer.write(RESPONSE_CHAT + "\n");
                            writer.flush();
                        }
                    }).start();
                } else {
                    if (socketMap.containsKey(info.getIp())) intent.putExtra("resocket", true);
                    Log.e("hello", "5");
                }
                intent.putExtra("PeerIp", info.getIp());
                startActivity(intent);

            }
        });
        initMyServerSocket();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLogOut();
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
            try {
                serverSocket = new ServerSocket(8080);
                while (true) {
                    Socket socket;
                    socket = serverSocket.accept();
                    Log.e("hello", "1");
                    String[] arrIp = socket.getRemoteSocketAddress().toString().split(":");
                    String ip = arrIp[0].substring(1);
                    socketMap.put(ip, socket);
                    BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    SocketReader.reader.put(ip, input);
                    PrintWriter writer = new PrintWriter(socket.getOutputStream());
                    SocketWriter.writer.put(ip, writer);
                    ServerThread serverThread = new ServerThread(socket);
                    ThreadManager.threadList.put(ip, serverThread);
                    serverThread.start();
                }
            } catch (IOException e) {
                Log.e("123", "error 1 " + e);
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
                input = SocketReader.reader.get(ip);
                Log.e("hello", "2");
                String msg;
                while (true) {
                    msg = input.readLine();
                    if (msg.equals(REQUEST_CHAT)) {
                        Log.e("hello", "3");
                        for (int i = 0; i < userArrayList.size(); i++) {
                            UserInfo info = userArrayList.get(i);
                            if (info.getIp().equals(ip)) {
                                info.setNewMessage(true);
                                userArrayList.set(i, info);
                                break;
                            }
                        }
                        break;
                    }
                }

            } catch (Exception e) {
                Log.e("123", "error 2 " + e);
                e.printStackTrace();
            }
        }
    }

    private void doLogOut(){
        final Socket socket = SocketUtil.getSocket();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    PrintWriter writer = new PrintWriter(socket.getOutputStream());
                    writer.print(SocketProtocol.LOGOUT_ACTION+"\n");
                    writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        finish();
    }
}
