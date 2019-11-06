package huy.nguyen.androidclient.Model;

import java.util.ArrayList;

public class GroupChat {
    String nameGroup;
    ArrayList<UserInfo> userInfoArrayList;
    ArrayList<Message> messageArrayList;

    public GroupChat(String nameGroup, ArrayList<UserInfo> userInfoArrayList) {
        this.nameGroup = nameGroup;
        this.userInfoArrayList = userInfoArrayList;
    }

    public String getNameGroup() {
        return nameGroup;
    }

    public void setNameGroup(String nameGroup) {
        this.nameGroup = nameGroup;
    }

    public ArrayList<UserInfo> getUserInfoArrayList() {
        return userInfoArrayList;
    }

    public void setUserInfoArrayList(ArrayList<UserInfo> userInfoArrayList) {
        this.userInfoArrayList = userInfoArrayList;
    }

    public ArrayList<Message> getMessageArrayList() {
        return messageArrayList;
    }

    public void setMessageArrayList(ArrayList<Message> messageArrayList) {
        this.messageArrayList = messageArrayList;
    }
}
