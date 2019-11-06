package huy.nguyen.androidclient.Model;

public class UserAccount {
    String username;
    String accountname;
    String ip;

    public UserAccount() {
    }

    public UserAccount(String username, String accountname, String ip) {
        this.username = username;
        this.accountname = accountname;
        this.ip = ip;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
}
