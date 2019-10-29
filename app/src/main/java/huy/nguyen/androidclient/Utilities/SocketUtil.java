package huy.nguyen.androidclient.Utilities;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import huy.nguyen.androidclient.Model.UserInfo;
import huy.nguyen.androidclient.Utilities.Interface.LoginCallback;
import huy.nguyen.androidclient.Utilities.Interface.OnlineUserCallback;
import huy.nguyen.androidclient.Utilities.Interface.SignupCallback;

public class SocketUtil {
    private static Socket socket;
    private static ServerSocket serverSocket;
    private static String myIp;
    public static Map<String, Socket> socketMap = new HashMap<>();
    private static final String NOTIFY_ONLINE = "NOTIFY_ONLINE";
    private static final String END_NOTIFY_ONLINE = "END_NOTIFY_ONLINE";
    private static final String REQUEST_ONLINE = "REQUEST_ONLINE";
    private static final int SERVER_PORT = 8080;


    public static void setSocket(Socket valuesocket) {
        socket = valuesocket;
    }

    public static Socket getSocket() {
        return socket;
    }

    public static ServerSocket getServerSocket() throws IOException {
        if (serverSocket == null){
            serverSocket = new ServerSocket(SERVER_PORT);
        }
        return serverSocket;
    }

    public static String getMyIp() {
        return myIp;
    }

    public static void setMyIp(String myIp) {
        SocketUtil.myIp = myIp;
    }

    public static void doSignUp(final String username, final String password, final String accountname, final SignupCallback callback) {
        Thread requestThread = new Thread() {
            @Override
            public void run() {
                PrintWriter output = null;
                try {
                    output = new PrintWriter(socket.getOutputStream());
                    output.write("SIGNUP" + "\n");
                    output.write(username + "\n");
                    output.write(password + "\n");
                    output.write(accountname + "\n");
                    output.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        requestThread.start();
        Thread responseThread = new Thread() {
            @Override
            public void run() {
                BufferedReader input = null;
                try {
                    input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                while (true) {
                    try {
                        String result = input.readLine();
                        if (result != null) {
                            callback.notifySignup(result);
                            break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        responseThread.start();
    }

    public static void doLogin(final String username, final String password, final LoginCallback callback) {
        Thread requestThread = new Thread() {
            @Override
            public void run() {
                PrintWriter output = null;
                try {
                    output = new PrintWriter(socket.getOutputStream());
                    output.write("LOGIN" + "\n");
                    output.write(username + "\n");
                    output.write(password + "\n");
                    output.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        requestThread.start();
        Thread responseThread = new Thread() {
            @Override
            public void run() {
                BufferedReader input = null;
                try {
                    input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                while (true) {
                    try {
                        String result = input.readLine();
                        if (result != null) {
                            callback.notifyLogin(result);
                            break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        responseThread.start();
    }

    public static void retriveOnlineUser(final OnlineUserCallback callback){
        Thread requestThread = new Thread() {
            @Override
            public void run() {
                PrintWriter output = null;
                try {
                    output = new PrintWriter(socket.getOutputStream());
                    output.write(REQUEST_ONLINE+"\n");
                    output.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        requestThread.start();
        Thread getOnlineUserThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    while (true){
                        String code = input.readLine();
                        if (code!=null){
                            switch (code){
                                case NOTIFY_ONLINE:{
                                    String user;
                                    ArrayList<UserInfo> userlist = new ArrayList<>();
                                    while (!(user=input.readLine()).equals(END_NOTIFY_ONLINE)){
                                        userlist.add(UserInfo.parseUser(user));
                                    }
                                    callback.retriveOnlineList(userlist);
                                    break;
                                }
                            }
                        }

                    }
                } catch (IOException e) {
                    Log.e("abc","some error"+e.toString());
                    e.printStackTrace();
                }
            }
        });
        getOnlineUserThread.start();
    }

}
