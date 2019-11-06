package huy.nguyen.androidclient.Home;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
import huy.nguyen.androidclient.Model.UserAccount;
import huy.nguyen.androidclient.Model.UserInfo;
import huy.nguyen.androidclient.R;
import huy.nguyen.androidclient.Utilities.SocketProtocol;
import huy.nguyen.androidclient.Utilities.SocketReader;
import huy.nguyen.androidclient.Utilities.SocketUtil;
import huy.nguyen.androidclient.Utilities.SocketWriter;

import static huy.nguyen.androidclient.Utilities.SocketUtil.socketMap;

public class HomeActivity extends AppCompatActivity {
    private static int PICK_IMAGE_REQUEST = 113;
    public static ArrayList<UserInfo> userArrayList;
    ArrayList<UserInfo> listUserInGroup;
    HomeUserAdpter groupUserAdapter;
    ListView listViewGroup;
    HomeUserAdpter homeUserAdpter;
    ListView listView;
    ImageView btnJoinToGroup,btnLogout;
    ServerSocket serverSocket;
    Thread Thread1 = null;
    ImageView imgAvatarHome;
    EditText edtAccountName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        listView = findViewById(R.id.lvListUserInHome);
        listViewGroup=findViewById(R.id.lvUserInGroup);
        btnJoinToGroup = findViewById(R.id.btnJoinToGroup);
        btnLogout = findViewById(R.id.btnLogOut);
        imgAvatarHome=findViewById(R.id.imgAvatarHome);
        edtAccountName=findViewById(R.id.edtAccountNameHome);

        userArrayList = new ArrayList<>();
        homeUserAdpter = new HomeUserAdpter(HomeActivity.this, userArrayList);
        listView.setAdapter(homeUserAdpter);

        initSetup();
        listUserInGroup=new ArrayList<>();
        groupUserAdapter=new HomeUserAdpter(HomeActivity.this,listUserInGroup);
        listViewGroup.setAdapter(groupUserAdapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final UserInfo info = (UserInfo) homeUserAdpter.getItem(position);
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                intent.putExtra("PeerIp", info.getIp());
                intent.putExtra("AccountName",info.getAccountname());
                startActivity(intent);

            }
        });

        initMyServerSocket();
//        initGroupSocket();
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLogOut();
            }
        });

        btnJoinToGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(HomeActivity.this, MessageGroupActivity.class);
                startActivity(intent);
            }
        });

        imgAvatarHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        while (true){
            if(SocketUtil.getMyAccount()!=null){
                edtAccountName.setText(SocketUtil.getMyAccount().getUsername().toUpperCase());
                break;
            }
        }

    }


    private void initSetup() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                PrintWriter output = SocketUtil.writer;
                BufferedReader input = SocketUtil.reader;
                output.write(SocketProtocol.GET_PERSONAL_INFO+"\n");
                output.flush();
                try {
                    String status = input.readLine();
                    if (status.equals(SocketProtocol.INVALID_USER)){
                        Log.e("1234", "INVALID" );
                    } else if(status.equals(SocketProtocol.VALID_USER)){
                        String accountname = input.readLine();
                        String username = input.readLine();
                        String ip = input.readLine();
                        SocketUtil.setMyAccount(new UserAccount(username,accountname,ip));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                output.write(SocketProtocol.REQUEST_ONLINE+"\n");
                output.flush();
                while (true) {
                    try {
                        String code = input.readLine();
                        if (code!=null){
                            switch (code){
                                case SocketProtocol.NOTIFY_ONLINE:{
                                    String user;
                                    final ArrayList<UserInfo> userlist = new ArrayList<>();
                                    while (!(user=input.readLine()).equals(SocketProtocol.END_NOTIFY_ONLINE)){
                                        userlist.add(UserInfo.parseUser(user));
                                    }
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            userArrayList.clear();
                                            userArrayList.addAll(userlist);
                                            Toast.makeText(HomeActivity.this, userArrayList.toString(), Toast.LENGTH_SHORT).show();
                                            homeUserAdpter.notifyDataSetChanged();
                                        }
                                    });
                                    break;
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
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
//                            String a = dis.readUTF();
                            byte[] buffer = new byte[1024];
                            int read;
                            while ((read = dis.read(buffer)) >= 0) {
                                fos.write(buffer, 0, read);
                            }
                            Log.e("1234", read+"" );
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(HomeActivity.this,"saved at :"+Environment.getExternalStorageDirectory().toString()+fileName,Toast.LENGTH_SHORT).show();
                                }
                            });
                            fos.close();
                            dis.close();
                            socket.close();
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

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}
