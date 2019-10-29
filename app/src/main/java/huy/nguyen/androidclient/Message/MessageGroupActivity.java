package huy.nguyen.androidclient.Message;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import huy.nguyen.androidclient.Model.Message;
import huy.nguyen.androidclient.Model.User;
import huy.nguyen.androidclient.R;
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
        requestToServer();
        initGroupSocket();
        btnGroupSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Socket socket=SocketUtil.getSocket();
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
        final Socket socket=SocketUtil.getSocket();
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
        final Socket socket=SocketUtil.getSocket();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BufferedReader input=new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    while (true){
                        String nofityGroup=input.readLine();
                        final String msg=input.readLine();
                        if(nofityGroup.equals("MESSAGE_RESPONE_IN_GROUP")){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    User user = new User("server");
                                    Message messageReal = new Message(msg, user, false);
                                    listMessage.add(messageReal);
                                    listMessageAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void addControls() {
        lvGroupMessage=findViewById(R.id.lvGroupMessage);
        btnGroupFileUpLoad=findViewById(R.id.btnGroupFileUpLoad);
        btnGroupSend=findViewById(R.id.btnGroupSend);
        edtGroupMessage=findViewById(R.id.edtGroupMessage);
    }
}
