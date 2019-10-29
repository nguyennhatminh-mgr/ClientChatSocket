package huy.nguyen.androidclient.Utilities;

import java.net.Socket;

public class GroupUtil {
    private static Socket socket;

    public static Socket getSocket() {
        return socket;
    }

    public static void setSocket(Socket socket) {
        GroupUtil.socket = socket;
    }
}
