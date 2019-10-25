package huy.nguyen.androidclient.Model;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class UserInfo implements Serializable {
    private String accountname;
    private String ip;

    public UserInfo() {
    }

    public UserInfo(String accountname) {
        this.accountname = accountname;
    }

    public UserInfo(String accountname, String ip) {
        this.accountname = accountname;
        this.ip = ip;
    }

    public String getAccountname() {
        return accountname;
    }

    public void setAccountname(String accountname) {
        this.accountname = accountname;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public static UserInfo parseUser(String userString){
        String[] userInfo = userString.split(":");
        return new UserInfo(userInfo[0],userInfo[1]);
    }

    @NonNull
    @Override
    public String toString() {
        return accountname+":"+ip+"\n";
    }
}
