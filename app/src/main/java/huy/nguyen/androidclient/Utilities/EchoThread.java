package huy.nguyen.androidclient.Utilities;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;

import huy.nguyen.androidclient.Model.AllMessage;
import huy.nguyen.androidclient.Model.Message;
import huy.nguyen.androidclient.Utilities.Interface.NoticeCallback;

public class EchoThread extends Thread {
    protected Socket socket;
    private NoticeCallback callback;
    private static final String NOTICE_MSG = "NOTICE_MSG";
    private static final String END_MSG = "END_MSG";
    private static final String END_SOCKET = "END_SOCKET";

    public EchoThread(Socket clientSocket,NoticeCallback callback) {
        this.socket = clientSocket;
        this.callback = callback;
    }

    public void run() {
        final BufferedReader input;
        PrintWriter output;
        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream());
            String[] arrIp = socket.getRemoteSocketAddress().toString().split(":");
            String ip = arrIp[0].substring(1);
            String msg;
            while(!(msg=input.readLine()).equals(END_SOCKET)){
                if (msg.equals(NOTICE_MSG)){
                    callback.startMsg(ip);
                    ArrayList<Message> curMess;
                    Map<String, ArrayList<Message>> all = AllMessage.allMessage;
                    if (all.containsKey(ip)) curMess = all.get(ip);
                    else curMess = new ArrayList<>();
                    while (!(msg=input.readLine()).equals(END_MSG)){
                        curMess.add(new Message(msg,false));
                    }
                    AllMessage.allMessage.put(ip,curMess);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
