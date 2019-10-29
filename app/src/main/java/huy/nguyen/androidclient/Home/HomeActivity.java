package huy.nguyen.androidclient.Home;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import huy.nguyen.androidclient.MainActivity;
import huy.nguyen.androidclient.Message.MessageGroupActivity;
import huy.nguyen.androidclient.Model.User;
import huy.nguyen.androidclient.Model.UserInfo;
import huy.nguyen.androidclient.R;
import huy.nguyen.androidclient.Utilities.GroupUtil;
import huy.nguyen.androidclient.Utilities.Interface.OnlineUserCallback;
import huy.nguyen.androidclient.Utilities.SocketProtocol;
import huy.nguyen.androidclient.Utilities.SocketReader;
import huy.nguyen.androidclient.Utilities.SocketUtil;
import huy.nguyen.androidclient.Utilities.SocketWriter;
import huy.nguyen.androidclient.Utilities.ThreadManager;

import static huy.nguyen.androidclient.Utilities.SocketUtil.socketMap;

public class HomeActivity extends AppCompatActivity {

    public static ArrayList<UserInfo> userArrayList;
    ArrayList<UserInfo> listUserInGroup;
    HomeUserAdpter groupUserAdapter;
    ListView listViewGroup;
    HomeUserAdpter homeUserAdpter;
    ListView listView;
    Button button;
    Button btnCreateGroup;
    ServerSocket serverSocket;
    Thread Thread1 = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        createSocket();
        listView = findViewById(R.id.lvListUserInHome);
        listViewGroup=findViewById(R.id.lvUserInGroup);
        button = findViewById(R.id.button);
//        checkBox=findViewById(R.id.chkAddToGroup);
        btnCreateGroup = findViewById(R.id.btnCreateGroup);
        userArrayList = new ArrayList<>();
        homeUserAdpter = new HomeUserAdpter(HomeActivity.this, userArrayList);
        listView.setAdapter(homeUserAdpter);

        listUserInGroup=new ArrayList<>();
        groupUserAdapter=new HomeUserAdpter(HomeActivity.this,listUserInGroup);
        listViewGroup.setAdapter(groupUserAdapter);


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
                intent.putExtra("PeerIp", info.getIp());
                startActivity(intent);

            }
        });

        initMyServerSocket();
//        initGroupSocket();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLogOut();
            }
        });

        btnCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(HomeActivity.this, MessageGroupActivity.class);
                startActivity(intent);
            }
        });
    }

    private void createSocket() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket("192.168.43.62", 8080);
                    GroupUtil.setSocket(socket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void initGroupSocket() {


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = GroupUtil.getSocket();
                    PrintWriter output=new PrintWriter(socket.getOutputStream());
                    output.write("GROUP_ACTION"+"\n");
                    output.flush();
                    BufferedReader input=new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    while (true){
                        String msg=input.readLine();
                        Log.e("123",msg);
                        if(msg.equals("NOTIFY_JOIN_TO_GROUP")){
                            String temp;
//                            Log.e("123 temp", temp );
                            listUserInGroup.clear();
                            while (!(temp=input.readLine()).equals("END_NOTIFY_JOIN_TO_GROUP")){
                                if(temp!=null){
                                    String[] temp1=temp.split("[:]");
                                    if(temp1.length==2){
                                        final UserInfo user=new UserInfo(temp1[0],temp1[1]);
//                            Log.e("123",temp1.toString());
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {

                                                listUserInGroup.add(user);
                                                groupUserAdapter.notifyDataSetChanged();
                                            }
                                        });
                                    }
                                }
                            }
//                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
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
                    String[] arrIp = socket.getRemoteSocketAddress().toString().split(":");
                    String ip = arrIp[0].substring(1);
                    BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    Log.e("1234", "new socket" );
                    String socketType = input.readLine();
                    if (socketType.equals(SocketProtocol.CHAT_SOCKET)) {
                        socketMap.put(ip, socket);
                        SocketReader.reader.put(ip, input);
                        PrintWriter writer = new PrintWriter(socket.getOutputStream());
                        SocketWriter.writer.put(ip, writer);
                        for (int i = 0; i < userArrayList.size(); i++) {
                            final UserInfo info = userArrayList.get(i);
                            if (info.getIp().equals(ip)) {
                                info.setNewMessage(true);
                                userArrayList.set(i, info);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(HomeActivity.this, info.getAccountname() + " muốn chat với bạn", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                break;
                            }
                        }
                    } else if (socketType.equals(SocketProtocol.FILE_SOCKET)) {
                        try {
                            final String fileName = input.readLine();
                            File file = new File(Environment.getExternalStorageDirectory(), fileName);
                            DataInputStream dis = new DataInputStream(socket.getInputStream());
                            FileOutputStream fos = new FileOutputStream(file);
                            byte[] buffer = new byte[4069];
                            int read;
                            Log.e("1234", "get there" );
                            while ((read = dis.read(buffer)) > 0) {
                                fos.write(buffer, 0, read);
                            }
                            fos.close();
//                            dis.close();
                        } catch (Exception e) {
                            Log.e("1234", "test " + e.toString());
                        }

                    }

                }
            } catch (IOException e) {
                Log.e("123", "error 1 " + e);
                e.printStackTrace();
            }
        }
    }

    private void doLogOut() {
        final Socket socket = SocketUtil.getSocket();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    PrintWriter writer = new PrintWriter(socket.getOutputStream());
                    writer.print(SocketProtocol.LOGOUT_ACTION + "\n");
                    writer.flush();
                    writer.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        finish();
    }
}
