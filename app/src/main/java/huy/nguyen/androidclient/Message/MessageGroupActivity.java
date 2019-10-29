package huy.nguyen.androidclient.Message;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import huy.nguyen.androidclient.Model.Message;
import huy.nguyen.androidclient.Model.User;
import huy.nguyen.androidclient.Model.UserInfo;
import huy.nguyen.androidclient.R;
import huy.nguyen.androidclient.Utilities.GroupUtil;
import huy.nguyen.androidclient.Utilities.SocketUtil;

public class MessageGroupActivity extends AppCompatActivity {

    ListView lvGroupMessage;
    ImageView btnGroupFileUpLoad,btnGroupSend;
    EditText edtGroupMessage;

    ArrayList<Message> listMessage;
    MessageListViewAdapter listMessageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_group);
        addControls();
        listMessage=new ArrayList<>();
        listMessageAdapter=new MessageListViewAdapter(MessageGroupActivity.this,listMessage);
        lvGroupMessage.setAdapter(listMessageAdapter);

        initGroupSocket();
//        requestToServer();

        btnGroupSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Socket socket=GroupUtil.getSocket();
                final String mes=edtGroupMessage.getText().toString();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            PrintWriter output=new PrintWriter(socket.getOutputStream());
                            output.write("MESSAGE_IN_GROUP"+"\n");
                            output.write(mes+"\n");
                            output.write(SocketUtil.getMyIp()+"\n");
                            output.flush();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    edtGroupMessage.setText("");
                                    User user = new User("server");
                                    Message messageReal = new Message(mes, user, true);
                                    listMessage.add(messageReal);
                                    listMessageAdapter.notifyDataSetChanged();
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
            }
        });
    }

    private void requestToServer() {
        final Socket socket= GroupUtil.getSocket();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    PrintWriter output=new PrintWriter(socket.getOutputStream());
                    output.write("REQ_TO_GET_MESSAGE"+"\n");
//                    output.write(SocketUtil.getMyIp()+"\n");
                    output.flush();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void initGroupSocket() {
        final Socket socket=GroupUtil.getSocket();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BufferedReader input=new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter output=new PrintWriter(socket.getOutputStream());
                    output.write("GROUP_ACTION"+"\n");
                    output.flush();
                    output.write("JOIN_TO_GROUP"+"\n");
                    output.write(SocketUtil.getMyIp()+"\n");
                    output.flush();
                    output.write("REQ_TO_GET_MESSAGE"+"\n");
                    output.flush();
                    while (true){
                        String nofityGroup=input.readLine();
                        if(nofityGroup.equals("NOTIFY_JOIN_TO_GROUP")){
                            String temp;
//                            Log.e("123 temp", temp );
                            while (!(temp=input.readLine()).equals("END_NOTIFY_JOIN_TO_GROUP")){
                                if(temp!=null){
                                    String[] temp1=temp.split("[:]");
                                    if(temp1.length==2){
                                        final UserInfo user=new UserInfo(temp1[0],temp1[1]);
//                            Log.e("123",temp1.toString());
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(MessageGroupActivity.this,"Add"+user.getAccountname(),Toast.LENGTH_SHORT).show();
//                                                listUserInGroup.add(user);
//                                                groupUserAdapter.notifyDataSetChanged();
                                            }
                                        });
                                    }
                                }
                            }

                        }
                        else if(nofityGroup.equals("MESSAGE_RESPONE_IN_GROUP")){
//                            listMessage.clear();
                            String msg;
                            while (!(msg=input.readLine()).equals("END_MESSAGE_RESPONE_IN_GROUP")){
                                String username = msg;
                                String message = input.readLine();
                                User user = new User("server");
                                final Message messageReal = new Message(username+" send "+message, user, false);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        listMessage.add(messageReal);
                                        listMessageAdapter.notifyDataSetChanged();
                                    }
                                });
                            }

                        }
                        else if(nofityGroup.equals("MESSAGE_SINGLE_RESPONE_IN_GROUP")){
                            String msg;
                            while (!(msg=input.readLine()).equals("END_MESSAGE_SINGLE_RESPONE_IN_GROUP")){
                                String username = msg;
                                String message = input.readLine();
                                User user = new User("server");
                                final Message messageReal = new Message(username+" send "+message, user, false);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        listMessage.add(messageReal);
                                        listMessageAdapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    BufferedReader input=new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                    while (true){
//                        String nofityGroup=input.readLine();
//                        if(nofityGroup.equals("MESSAGE_SINGLE_RESPONE_IN_GROUP")){
//                            final String msg=input.readLine();
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    User user = new User("server");
//                                    Message messageReal = new Message(msg, user, false);
//                                    listMessage.add(messageReal);
//                                    listMessageAdapter.notifyDataSetChanged();
//                                }
//                            });
//                        }
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
    }

    private void addControls() {
        lvGroupMessage=findViewById(R.id.lvGroupMessage);
        btnGroupFileUpLoad=findViewById(R.id.btnGroupFileUpLoad);
        btnGroupSend=findViewById(R.id.btnGroupSend);
        edtGroupMessage=findViewById(R.id.edtGroupMessage);
    }
}
