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

import huy.nguyen.androidclient.Message.MessageListViewAdapter;
import huy.nguyen.androidclient.Model.Message;
import huy.nguyen.androidclient.Model.User;
import huy.nguyen.androidclient.Utilities.EchoThread;
import huy.nguyen.androidclient.Utilities.Interface.ActiveCallback;
import huy.nguyen.androidclient.Utilities.SocketReader;
import huy.nguyen.androidclient.Utilities.SocketUtil;
import huy.nguyen.androidclient.Utilities.SocketWriter;

@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity {
    Thread Thread1 = null;
    EditText etIP, etPort;
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

    private static final String REQUEST_CHAT = "REQUEST_CHAT";
    private static final String RESPONSE_CHAT = "RESPONSE_CHAT";

    private static final String END_CHAT = "END_CHAT";

    ArrayList<Message> messagesListView;
    ListView listView;
    public static MessageListViewAdapter messageListViewAdapter;
    public ActiveCallback callback;


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
        final String ip = intent.getStringExtra("PeerIp");
        final Map<String, Socket> socketMap = SocketUtil.socketMap;
        if (!socketMap.containsKey(ip)) {
            Toast.makeText(this, ip, Toast.LENGTH_SHORT).show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        socket = new Socket(ip, 8080);
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
//                    input = SocketReader.reader.get(ip)
                    final String message = input.readLine();
                    if (message != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                User user = new User("server");
                                Message messageReal = new Message(message, user, false);
                                messagesListView.add(messageReal);
                                messageListViewAdapter.notifyDataSetChanged();
                            }
                        });
                        if (message.equals(END_CHAT)){
                            break;
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }
    }


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
                Log.e("123","error 3 "+e);
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
                                User user = new User("server");
                                Message messageReal = new Message(message, user, false);
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
                    Log.e("123","error 4 "+e);
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
            Log.e("123", "ran " );
            output.flush();
            while (true) {
                try {
                    String res = input.readLine();
                    Log.e("123", res);
                    if (res.equals(RESPONSE_CHAT)) {
                        Log.e("123", "gg3");
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
                    Log.e("123","error 5 "+e);
                    e.printStackTrace();
                }
            }
        }
    }


}