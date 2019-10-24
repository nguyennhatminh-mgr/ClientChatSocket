package huy.nguyen.androidclient.Utilities;

import java.util.ArrayList;

import huy.nguyen.androidclient.Model.UserInfo;

public interface OnlineUserCallback {
    void retriveOnlineList(ArrayList<UserInfo> onlineList);
}
