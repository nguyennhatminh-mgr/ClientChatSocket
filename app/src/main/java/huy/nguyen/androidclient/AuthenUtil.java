package huy.nguyen.androidclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class AuthenUtil {
    private static Socket socket;
    public void setSocket(Socket socket){
        socket = socket;
    }
    public Socket getSocket(){
        return socket;
    }
    public void doSignUp(String username, String password, String accountname, final SignupCallback callback){
        try {
            PrintWriter output = new PrintWriter(socket.getOutputStream());
            final BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output.write("SIGNUP"+"\n");
            output.write(username+"\n");
            output.write(password+"\n");
            output.write(accountname+"\n");
            new Thread(){
                @Override
                public void run() {
                    while (true){
                        try {
                            String result = input.readLine();
                            if (result!=null){
                                callback.notify(result);
                                break;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
