package huy.nguyen.androidclient.Model;

public class Message {
    String textMessage;
    User user;
    boolean belongsToCurrentUser;

    public Message(String textMessage, User user, boolean belongsToCurrentUser) {
        this.textMessage = textMessage;
        this.user = user;
        this.belongsToCurrentUser = belongsToCurrentUser;
    }

    public String getTextMessage() {
        return textMessage;
    }

    public void setTextMessage(String textMessage) {
        this.textMessage = textMessage;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isBelongsToCurrentUser() {
        return belongsToCurrentUser;
    }

    public void setBelongsToCurrentUser(boolean belongsToCurrentUser) {
        this.belongsToCurrentUser = belongsToCurrentUser;
    }
}
