package huy.nguyen.androidclient;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import huy.nguyen.androidclient.Message.MessageListViewAdapter;
import huy.nguyen.androidclient.Model.Message;
import huy.nguyen.androidclient.Model.User;
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

    //Message
//    ArrayList<Message> messageArrayList;
//    RecyclerView rcvMessage;
//    HomeUserAdpter messageAdpter;

    //Message ListView
    ArrayList<Message> messagesListView;
    ListView listView;
    MessageListViewAdapter messageListViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etIP = findViewById(R.id.etIP);
        etPort = findViewById(R.id.etPort);
        tvMessages = findViewById(R.id.tvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnSwap = findViewById(R.id.btnSwap);
        Button btnConnect = findViewById(R.id.btnConnect);

        addControls();
//        messageArrayList=new ArrayList<>();
//        messageAdpter=new HomeUserAdpter(MainActivity.this,messageArrayList);
//        rcvMessage.setAdapter(messageAdpter);
//        rcvMessage.setLayoutManager(new LinearLayoutManager(MainActivity.this,LinearLayoutManager.VERTICAL,false));

        messagesListView=new ArrayList<>();
        messageListViewAdapter=new MessageListViewAdapter(MainActivity.this,messagesListView);
        listView.setAdapter(messageListViewAdapter);

        socket=SocketUtil.getSocket();

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvMessages.setText("");
                SERVER_IP = etIP.getText().toString().trim();
                SERVER_PORT = Integer.parseInt(etPort.getText().toString().trim());
                Thread1 = new Thread(new Thread1());
                Thread1.start();
            }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = etMessage.getText().toString().trim();
                if (!message.isEmpty()) {
                    new Thread(new Thread3(message)).start();
                }
            }
        });
        btnSwap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,ServerActivity.class);
                startActivity(intent);
                finish();
            }
        });
//        messageAdpter.notifyDataSetChanged();
    }

    private void addControls() {
//        rcvMessage=findViewById(R.id.rcvMesseges);
        listView=findViewById(R.id.lstMessage);
    }


    private PrintWriter output;
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
            output.write(message+"\n");
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