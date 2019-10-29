package huy.nguyen.androidclient;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import huy.nguyen.androidclient.Message.MessageListViewAdapter;
import huy.nguyen.androidclient.Model.Message;
import huy.nguyen.androidclient.Model.User;
import huy.nguyen.androidclient.Utilities.SocketProtocol;
import huy.nguyen.androidclient.Utilities.SocketReader;
import huy.nguyen.androidclient.Utilities.SocketUtil;
import huy.nguyen.androidclient.Utilities.SocketWriter;

@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity {
    TextView tvMessages;
    EditText etMessage;
    ImageView btnSend, btnUploadFile, btnCall;
    LinearLayout top, bottom;
    private final int PICK_FILE = 71;
    String ip;
    String accountname;
    BufferedReader input;
    Random random = new Random();
    static String LOG_TAG = "1234";
    static int PORT_1;
    static int PORT_2 = 10001;
    private ArrayList<Thread> listThread = new ArrayList<>();

    private boolean backPressed = false;
    private volatile static boolean onlineFriend = false;

    private static final String REQUEST_CHAT = "REQUEST_CHAT";
    private static final String RESPONSE_CHAT = "RESPONSE_CHAT";

    private static final int SAMPLE_RATE = 8000; // Hertz
    private static final int SAMPLE_INTERVAL = 20; // Milliseconds
    private static final int SAMPLE_SIZE = 2; // Bytes
    private static final int BUF_SIZE = SAMPLE_INTERVAL * SAMPLE_INTERVAL * SAMPLE_SIZE * 2; //Bytes

    private static final String END_CHAT = "END_CHAT";
    private static final String SEND_FILE = "SEND_FILE";
    ArrayList<Message> messagesListView;
    ListView listView;
    public static MessageListViewAdapter messageListViewAdapter;

    private boolean speakers = true; // Enable speakers?
    private boolean call = true; // Enable speakers?


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvMessages = findViewById(R.id.tvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnUploadFile = findViewById(R.id.btnFileUpLoad);
        btnCall = findViewById(R.id.btnCall);
        top = findViewById(R.id.alert);
        bottom = findViewById(R.id.lyBot);
        addControls();

        initSocket();

        int permissionCheck1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission2 = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        if (permissionCheck1 != PackageManager.PERMISSION_GRANTED || permission2 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, 1);
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
                }
            }
        });
        btnUploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });

        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestCall();
            }
        });


    }

    private void requestCall() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                PrintWriter writer = SocketWriter.writer.get(ip);
                writer.write(SocketProtocol.REQUEST_CALL+"\n");
                PORT_1 = random.nextInt(20)+10000;
                writer.write(PORT_1+"\n");
                writer.flush();
            }
        }).start();
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE && resultCode == RESULT_OK
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
                        Log.e("1234", "filename " + fileName);
                        fileWriter.flush();
                        PrintWriter chatWriter = SocketWriter.writer.get(ip);
                        chatWriter.write(SocketProtocol.FILE_MESSAGE + "\n");
                        chatWriter.write(fileName + "\n");
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
                        Log.e("1234", "error in sending " + e.toString());
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
                        output.write(SocketProtocol.CHAT_SOCKET + "\n");
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
                                    Message messageReal = new Message("received file:" + fileName, user, false);
                                    messagesListView.add(messageReal);
                                    messageListViewAdapter.notifyDataSetChanged();
                                }
                            });
                            new ReceiverThread().run();
                            break;
                        }
                        case SocketProtocol.REQUEST_CALL:{
                            final String receivePort = input.readLine();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog dialog;
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                    builder.setTitle("Confirm");
                                    builder.setCancelable(false);
                                    builder.setMessage(accountname + " muốn gọi cho bạn, đồng ý chứ?");
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Toast.makeText(MainActivity.this,"OK",Toast.LENGTH_SHORT).show();
                                            PORT_2 = random.nextInt(20)+10000;
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    PrintWriter writer = SocketWriter.writer.get(ip);
                                                    writer.write(SocketProtocol.CALL_OK+"\n");
                                                    writer.write(PORT_2+"\n");
                                                    writer.flush();
                                                }
                                            }).start();
                                            Intent intent = new Intent(MainActivity.this, CallActivity.class);
                                            intent.putExtra("MyPort",PORT_2);
                                            intent.putExtra("FriendPort",Integer.parseInt(receivePort));
                                            intent.putExtra("PeerIp",ip);
                                            startActivity(intent);

                                        }
                                    });
                                    builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Toast.makeText(MainActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    PrintWriter writer = SocketWriter.writer.get(ip);
                                                    writer.write(SocketProtocol.CALL_DENY+"\n");
                                                    writer.flush();
                                                }
                                            }).start();
                                        }
                                    });
                                    dialog = builder.create();
                                    dialog.show();
                                }
                            });
                            new ReceiverThread().run();
                            break;
                        }
                        case SocketProtocol.CALL_DENY:{
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "User deny", Toast.LENGTH_SHORT).show();
                                }
                            });
                            new ReceiverThread().run();
                            break;
                        }
                        case SocketProtocol.CALL_OK:{
                            final String friendPort = input.readLine();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(MainActivity.this, CallActivity.class);
                                    intent.putExtra("MyPort",PORT_1);
                                    intent.putExtra("FriendPort",Integer.parseInt(friendPort));
                                    intent.putExtra("PeerIp",ip);
                                    startActivity(intent);
                                }
                            });
                        }
                        case SocketProtocol.END_CHAT: {
                            SocketUtil.socketMap.remove(ip);
                            SocketWriter.writer.remove(ip);
                            SocketReader.reader.remove(ip);
                            socket.close();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "User out", Toast.LENGTH_SHORT).show();
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