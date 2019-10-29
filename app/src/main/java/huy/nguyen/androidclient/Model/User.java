package huy.nguyen.androidclient.Model;

public class User {
    String name;
    String url;
    String ip;

    public User(String name, String ip) {
        this.name = name;
        this.ip = ip;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public User(String name) {
        this.name = name;
    }
}
