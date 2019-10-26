package huy.nguyen.androidclient;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
    ImageView btnSend,btnUploadFile;
    Button btnSwap;
    String SERVER_IP;
    int SERVER_PORT;
    Socket socket;
    PrintWriter output;
    LinearLayout top,bottom;
    private BufferedReader input;
    private final int PICK_IMAGE_REQUEST = 71;
    private Uri filePath;
    ImageView btnSend;
    LinearLayout top, bottom;
    String ip;

    private ArrayList<Thread> listThread = new ArrayList<>();

    private boolean backPressed = false;
    private volatile static boolean onlineFriend = false;

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
        btnUploadFile=findViewById(R.id.btnFileUpLoad);
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
        btnUploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                etMessage.setText(filePath.toString());
                Toast.makeText(MainActivity.this,"Choose file success",Toast.LENGTH_SHORT).show();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void initSocket() {
        Intent intent = getIntent();
        ip = intent.getStringExtra("PeerIp");
        boolean resocket = false;
        if (intent.hasExtra("resocket"))
            resocket = true;
        final Map<String, Socket> socketMap = SocketUtil.socketMap;
        if (!socketMap.containsKey(ip)) {
            Thread a = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Socket socket;
                        socket = new Socket(ip, 8080);
                        Log.e("hello", "6");
                        SocketUtil.socketMap.put(ip, socket);
                        PrintWriter output = new PrintWriter(socket.getOutputStream());
                        BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        SocketReader.reader.put(ip,input);
                        SocketWriter.writer.put(ip,output);
//                        EchoThread thread = new EchoThread(socket)
                        new ReqResThread().run();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            listThread.add(a);
            a.start();
        } else {
                if (!resocket){
                    top.setVisibility(View.GONE);
                    bottom.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.VISIBLE);
                }
                Thread b = new Thread(new ReceiverThread());
                listThread.add(b);
                b.start();
        }
    }

    private void addControls() {
        listView = findViewById(R.id.lstMessage);
    }


    class ReceiverThread implements Runnable {
        @Override
        public void run() {
            BufferedReader input = SocketReader.reader.get(ip);
            while (!Thread.interrupted()) {
                try {
                    final String message = input.readLine();
                    if (message != null) {
                        if (message.equals(END_CHAT)) {
//                            onlineFriend = false;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    top.setVisibility(View.VISIBLE);
                                    bottom.setVisibility(View.INVISIBLE);
                                    listView.setVisibility(View.INVISIBLE);
                                }
                            });
                            new ReqResThread().run();
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
            PrintWriter output = SocketWriter.writer.get(ip);
            output.write(message + "\n");
            output.flush();
            Log.e("hello", "9");
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
            PrintWriter output = SocketWriter.writer.get(ip);
            BufferedReader input = SocketReader.reader.get(ip);
            Log.e("hello", "10" );
            output.write(REQUEST_CHAT + "\n");
            output.flush();
            while (true) {
                try {
                    String res = input.readLine();
                    Log.e("test", res);
                    if (res.equals(RESPONSE_CHAT)) {
                        Log.e("test", "kaka");
                        onlineFriend = true;
                        Log.e("test", "keke");
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
//        backPressed = true;
        ArrayList<UserInfo> userArr = HomeActivity.userArrayList;
        Toast.makeText(this, onlineFriend?"1":"0", Toast.LENGTH_SHORT).show();
        if (onlineFriend) {
            for (int i = 0; i < userArr.size(); i++) {
                UserInfo info = userArr.get(i);
                if (info.getIp().equals(ip)) {
                    info.setNewMessage(true);
                    HomeActivity.userArrayList.set(i, info);
                    break;
                }
            }
        } else {
            for (int i = 0; i < userArr.size(); i++) {
                UserInfo info = userArr.get(i);
                if (info.getIp().equals(ip)) {
                    info.setNewMessage(false);
                    HomeActivity.userArrayList.set(i, info);
                    break;
                }
            }
        }
        for(Thread thread:listThread){
            thread.interrupt();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                PrintWriter output = SocketWriter.writer.get(ip);
                output.write(END_CHAT + "\n");
                output.flush();
            }
        }).start();
        super.onBackPressed();
    }
}