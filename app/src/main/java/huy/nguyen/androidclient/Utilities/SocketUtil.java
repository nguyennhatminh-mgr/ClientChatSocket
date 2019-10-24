package huy.nguyen.androidclient.Utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketUtil {
    private static Socket socket;

    public static void setSocket(Socket valuesocket) {
        socket = valuesocket;
    }

    public static Socket getSocket() {
        return socket;
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
}
