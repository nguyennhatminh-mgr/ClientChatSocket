package huy.nguyen.androidclient;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class AuthenUtil {
    private static Socket socket;
    public static void setSocket(Socket valuesocket){
        socket = valuesocket;
    }
    public static Socket getSocket(){
        return socket;
    }
    public static void doSignUp(String username, String password, String accountname, final SignupCallback callback){
        try {
            PrintWriter output = new PrintWriter(socket.getOutputStream());
            output.write("SIGNUP"+"\n");
            output.write(username+"\n");
            output.write(password+"\n");
            output.write(accountname+"\n");
            Log.e("a1","hello");
            Thread a = new Thread(){
                @Override
                public void run() {
                    BufferedReader input = null;
                    try {
                        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.e("a1","hello");
                    while (true){
                        try {
                            String result = input.readLine();
                            Log.e("a1","hello");
                            if (result!=null){
                                Log.e("a1","hi");
                                callback.notify(result);
                                break;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            a.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
