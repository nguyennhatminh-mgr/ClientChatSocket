package huy.nguyen.androidclient;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import huy.nguyen.androidclient.Utilities.SocketProtocol;
import huy.nguyen.androidclient.Utilities.SocketReader;
import huy.nguyen.androidclient.Utilities.SocketUtil;
import huy.nguyen.androidclient.Utilities.SocketWriter;

@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity {
    TextView tvMessages;
    EditText etMessage;
    ImageView btnSend, btnUploadFile;
    LinearLayout top, bottom;
    private final int PICK_IMAGE_REQUEST = 71;
    String ip;
    BufferedReader input;

    private ArrayList<Thread> listThread = new ArrayList<>();

    private boolean backPressed = false;
    private volatile static boolean onlineFriend = false;

    private static final String REQUEST_CHAT = "REQUEST_CHAT";
    private static final String RESPONSE_CHAT = "RESPONSE_CHAT";

    private static final String END_CHAT = "END_CHAT";
    private static final String SEND_FILE = "SEND_FILE";
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
        btnUploadFile = findViewById(R.id.btnFileUpLoad);
        top = findViewById(R.id.alert);
        bottom = findViewById(R.id.lyBot);
        addControls();

        initSocket();
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }
            }
        } catch (Exception e) {
            Log.e("1234", "error" + e);
        }

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
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            final Uri uri = data.getData();

            final String fileName = getFileName(uri);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Socket socket = new Socket(ip, 8080);
                        PrintWriter fileWriter = new PrintWriter(socket.getOutputStream());
                        fileWriter.write(SocketProtocol.FILE_SOCKET + "\n");
                        fileWriter.flush();
                        fileWriter.write(fileName + "\n");
                        Log.e("1234", "filename "+fileName );
                        fileWriter.flush();
                        PrintWriter chatWriter = SocketWriter.writer.get(ip);
                        chatWriter.write(SocketProtocol.FILE_MESSAGE+"\n");
                        chatWriter.write(fileName+"\n");
                        chatWriter.flush();
                        FileInputStream stream = (FileInputStream) getContentResolver().openInputStream(uri);
                        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                        byte[] myBuffer = new byte[4069];
                        while (stream.read(myBuffer) > 0) {
                            dos.write(myBuffer);
                        }
                        stream.close();
//                        dos.close();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                User user = new User("server");
                                Message messageReal = new Message(fileName, user, true);
                                messagesListView.add(messageReal);
                                messageListViewAdapter.notifyDataSetChanged();
                            }
                        });
                    } catch (Exception e) {
                        Log.e("1234", "error in sending "+e.toString());
                    }

                }
            }).start();
        }
    }

    private void initSocket() {
        Intent intent = getIntent();
        ip = intent.getStringExtra("PeerIp");
        final Map<String, Socket> socketMap = SocketUtil.socketMap;
        if (!socketMap.containsKey(ip)) {
            Thread a = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Socket socket;
                        socket = new Socket(ip, 8080);
//                        Log.e("hello", "6");
                        SocketUtil.socketMap.put(ip, socket);
                        PrintWriter output = new PrintWriter(socket.getOutputStream());
                        output.write(SocketProtocol.CHAT_SOCKET+"\n");
                        output.flush();
                        BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        SocketReader.reader.put(ip, input);
                        SocketWriter.writer.put(ip, output);
                        new ReceiverThread().run();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            listThread.add(a);
            a.start();
        } else {
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
            Socket socket = SocketUtil.socketMap.get(ip);
            try {
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final String message = input.readLine();
                if (message != null) {
                    switch (message) {
                        case SocketProtocol.TEXT_MESSAGE: {
                            final String chatMsg = input.readLine();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    User user = new User("server");
                                    Message messageReal = new Message(chatMsg, user, false);
                                    messagesListView.add(messageReal);
                                    messageListViewAdapter.notifyDataSetChanged();
                                }
                            });
                            new ReceiverThread().run();
                            break;
                        }
                        case SocketProtocol.FILE_MESSAGE: {
                            final String fileName = input.readLine();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    User user = new User("server");
                                    Message messageReal = new Message("received file:"+fileName, user, false);
                                    messagesListView.add(messageReal);
                                    messageListViewAdapter.notifyDataSetChanged();
                                }
                            });
                            new ReceiverThread().run();
                            break;
                        }
                        case SocketProtocol.END_CHAT: {
                            SocketUtil.socketMap.remove(ip);
                            SocketWriter.writer.remove(ip);
                            SocketReader.reader.remove(ip);
                            socket.close();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this,"User out",Toast.LENGTH_SHORT).show();
                                }
                            });
                            finish();
                            break;
                        }
                        default: {
                            new ReceiverThread().run();
                        }
                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
                Log.e("1234", "error file " + e);
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
            output.write(SocketProtocol.TEXT_MESSAGE + "\n");
            output.write(message.trim() + "\n");

            output.flush();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    etMessage.setText("");
                    User user = new User("client");
                    Message messageReal = new Message(message.trim(), user, true);
                    messagesListView.add(messageReal);
                    messageListViewAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                PrintWriter output = SocketWriter.writer.get(ip);
                output.write(SocketProtocol.END_CHAT + "\n");
                output.flush();
                output.close();
                SocketUtil.socketMap.remove(ip);
                SocketWriter.writer.remove(ip);
                SocketReader.reader.remove(ip);
            }
        }).start();

        super.onBackPressed();
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