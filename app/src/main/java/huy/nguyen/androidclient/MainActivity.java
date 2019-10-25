package huy.nguyen.androidclient;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import huy.nguyen.androidclient.Message.MessageListViewAdapter;
import huy.nguyen.androidclient.Model.Message;
import huy.nguyen.androidclient.Model.User;
import huy.nguyen.androidclient.Utilities.EchoThread;
import huy.nguyen.androidclient.Utilities.SocketUtil;

@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity {
    Thread Thread1 = null;
    EditText etIP, etPort;
    TextView tvMessages;
    EditText etMessage;
    ImageView btnSend;
    Button btnSwap;
    String SERVER_IP;
    int SERVER_PORT;
    Socket socket;
    PrintWriter output;
    private static final String NOTICE_MSG = "NOTICE_MSG";
    private static final String END_MSG = "END_MSG";
    private static final String END_SOCKET = "END_SOCKET";

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
        initSocket();
        addControls();
//        messageArrayList=new ArrayList<>();
//        messageAdpter=new HomeUserAdpter(MainActivity.this,messageArrayList);
//        rcvMessage.setAdapter(messageAdpter);
//        rcvMessage.setLayoutManager(new LinearLayoutManager(MainActivity.this,LinearLayoutManager.VERTICAL,false));

        messagesListView=new ArrayList<>();
        messageListViewAdapter=new MessageListViewAdapter(MainActivity.this,messagesListView);
        listView.setAdapter(messageListViewAdapter);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = etMessage.getText().toString().trim();
                if (!message.isEmpty()) {
                    new Thread(new Thread3(message)).start();

                }
            }
        });
    }

    private void initSocket() {
        Intent intent = getIntent();
        final String ip = intent.getStringExtra("PeerIp");
        Map<String, Socket> socketMap = SocketUtil.socketMap;
        if (!socketMap.containsKey(ip)){
            Toast.makeText(this, ip, Toast.LENGTH_SHORT).show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        socket = new Socket(ip, 8080);
                        SocketUtil.socketMap.put(ip,socket);
                        output = new PrintWriter(socket.getOutputStream());
                        Log.e("123", "get a" );
//                        EchoThread thread = new EchoThread(socket)
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } else{

        }
    }

    private void addControls() {
        listView=findViewById(R.id.lstMessage);
    }



















    private BufferedReader input;
    class Thread1 implements Runnable {
        public void run() {
            Socket socket;
            try {
                socket = new Socket(SERVER_IP, SERVER_PORT);
                output = new PrintWriter(socket.getOutputStream());
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvMessages.setText("Connected\n");
                    }
                });
                new Thread(new Thread2()).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    class Thread2 implements Runnable {
        @Override
        public void run() {
//            Log.e("msg","go here ?");
            while (true) {
//                Log.e("msg","adef");
                try {
                    final String message = input.readLine();
//                    Log.e("msg",message);
                    if (message != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                tvMessages.append("server: " + message + "\n");
                                User user=new User("server");
                                Message messageReal=new Message(message,user,false);
//                                messageArrayList.add(messageReal);
//                                messageAdpter.notifyDataSetChanged();
                                messagesListView.add(messageReal);
                                messageListViewAdapter.notifyDataSetChanged();
                            }
                        });
                    } else {
//                        Log.e("msg","get a");
                        Thread1 = new Thread(new Thread1());
                        Thread1.start();
                        return;
                    }
                } catch (IOException e) {
//                    Log.e("msg","get there");
                    e.printStackTrace();
                }
            }
        }
    }
    class Thread3 implements Runnable {
        private String message;
        Thread3(String message) {
            this.message = message;
        }
        @Override
        public void run() {
            output.write(NOTICE_MSG+"\n");
            output.write(message+"\n");
            output.write(END_MSG+"\n");
            output.flush();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    tvMessages.append("client: " + message + "\n");
                    etMessage.setText("");
                    User user=new User("client");
                    Message messageReal=new Message(message,user,true);
//                    messageArrayList.add(messageReal);
//                    messageAdpter.notifyDataSetChanged();
                    messagesListView.add(messageReal);
                    messageListViewAdapter.notifyDataSetChanged();
                }
            });
        }
    }
}