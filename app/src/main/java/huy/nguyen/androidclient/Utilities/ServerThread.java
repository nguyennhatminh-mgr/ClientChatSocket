package huy.nguyen.androidclient.Utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;

public class ServerThread extends Thread {
    protected Socket socket;

    public ServerThread(Socket clientSocket) {
        this.socket = clientSocket;
    }

    public void run() {
        BufferedReader input;
        PrintWriter output;
        String[] arrIp = socket.getRemoteSocketAddress().toString().split(":");
        String ip = arrIp[0].substring(1);
        String port = arrIp[1];
        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream());

        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
