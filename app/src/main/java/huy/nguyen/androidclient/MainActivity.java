package huy.nguyen.androidclient;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;

import huy.nguyen.androidclient.Home.HomeActivity;
import huy.nguyen.androidclient.Message.MessageListViewAdapter;
import huy.nguyen.androidclient.Model.Message;
import huy.nguyen.androidclient.Model.User;
import huy.nguyen.androidclient.Model.UserInfo;
import huy.nguyen.androidclient.Utilities.EchoThread;
import huy.nguyen.androidclient.Utilities.Interface.ActiveCallback;
import huy.nguyen.androidclient.Utilities.SocketReader;
import huy.nguyen.androidclient.Utilities.SocketUtil;
import huy.nguyen.androidclient.Utilities.SocketWriter;

@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity {
    TextView tvMessages;
    EditText etMessage;
    ImageView btnSend;
    Socket socket;
    PrintWriter output;
    LinearLayout top,bottom;
    String ip;
    private BufferedReader input;

    private boolean backPressed = false;

    private static final String REQUEST_CHAT = "REQUEST_CHAT";
    private static final String RESPONSE_CHAT = "RESPONSE_CHAT";

    private static final String END_CHAT = "END_CHAT";

    ArrayList<Message> messagesListView;
    ListView listView;
    public static MessageListViewAdapter messageListViewAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvMessages = findViewById(R.id.tvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        top = findViewById(R.id.alert);
        bottom = findViewById(R.id.lyBot);
        addControls();

        initSocket();

        messagesListView = new ArrayList<>();
        messageListViewAdapter = new MessageListViewAdapter(MainActivity.this, messagesListView);
        listView.setAdapter(messageListViewAdapter);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = etMessage.getText().toString().trim();
                if (!message.isEmpty()) {
                    new Thread(new SendThread(message)).start();
//                    new ReqResThread().run();
                }
            }
        });
    }

    private void initSocket() {
        Intent intent = getIntent();
         ip = intent.getStringExtra("PeerIp");
        final Map<String, Socket> socketMap = SocketUtil.socketMap;
        if (!socketMap.containsKey(ip)) {
            Toast.makeText(this, ip, Toast.LENGTH_SHORT).show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        socket = new Socket(ip, 8080);
                        Log.e("hello", "6" );
                        SocketUtil.socketMap.put(ip, socket);
                        output = new PrintWriter(socket.getOutputStream());
                        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                        EchoThread thread = new EchoThread(socket)
                        new Thread(new ReqResThread()).start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {
            top.setVisibility(View.GONE);
            bottom.setVisibility(View.VISIBLE);
            listView.setVisibility(View.VISIBLE);
            output = SocketWriter.writer.get(ip);
            input = SocketReader.reader.get(ip);
            new Thread(new ReceiverThread()).start();
            Log.e("hello", "7" );
        }
    }

    private void addControls() {
        listView = findViewById(R.id.lstMessage);
    }


    class ReceiverThread implements Runnable {
        @Override
        public void run() {
            String msg;
            while (true) {
                try {
//                    input = SocketReader.reader.get(ip);
                    Log.e("hello", "8" );
                    final String message = input.readLine();
                    if (message != null) {
                        if (message.equals(END_CHAT)){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    top.setVisibility(View.VISIBLE);
                                    bottom.setVisibility(View.INVISIBLE);
                                    listView.setVisibility(View.INVISIBLE);
                                }
                            });
                            new Thread(new ReqResThread()).start();
                            break;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                User user = new User("server");
                                Message messageReal = new Message(message, user, false);
                                messagesListView.add(messageReal);
                                messageListViewAdapter.notifyDataSetChanged();
                            }
                        });

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }
    }


    class SendThread implements Runnable {
        private String message;

        SendThread(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            output.write(message + "\n");
            output.flush();
            Log.e("hello", "9" );
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    tvMessages.append("client: " + message + "\n");
                    etMessage.setText("");
                    User user = new User("client");
                    Message messageReal = new Message(message, user, true);
//                    messageArrayList.add(messageReal);
//                    messageAdpter.notifyDataSetChanged();
                    messagesListView.add(messageReal);
                    messageListViewAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    class ReqResThread implements Runnable {
        @Override
        public void run() {
            output.write(REQUEST_CHAT + "\n");
            Log.e("hello", "10" );
            output.flush();
            while (true) {
                try {
                    String res = input.readLine();
                    if (res.equals(RESPONSE_CHAT)) {
                        Log.e("hello", "11" );
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                top.setVisibility(View.GONE);
                                bottom.setVisibility(View.VISIBLE);
                                listView.setVisibility(View.VISIBLE);
                            }
                        });
                        new ReceiverThread().run();
                        break;
                    }
                } catch (IOException e) {
//                    Log.e("123","error 5 "+e);
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        backPressed = true;
        ArrayList<UserInfo> userArr = HomeActivity.userArrayList;
        for (int i=0;i<userArr.size();i++){
            UserInfo info = userArr.get(i);
            if (info.getIp().equals(ip)){
                info.setNewMessage(true);
                HomeActivity.userArrayList.set(i,info);
                break;
            }
        }
        output.write(END_CHAT + "\n");
        super.onBackPressed();
    }
}