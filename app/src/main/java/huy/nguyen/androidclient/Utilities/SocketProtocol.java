package huy.nguyen.androidclient.Utilities;

public class SocketProtocol {
    /*
     * this block is for the authentication protocol
     */
    public static final String SIGNUP_ACTION = "SIGNUP";
    public static final String SIGNUP_SUCCESS = "SIGNUP_SUCCESS";
    public static final String SIGNUP_FAIL_USERNAME = "SIGNUP_FAIL_USERNAME";
    public static final String LOGIN_ACTION = "LOGIN";
    public static final String LOGIN_SUCCESS = "LOGIN_SUCCESS";
    public static final String LOGIN_FAIL_PASSWORD = "LOGIN_FAIL_PASSWORD";
    public static final String LOGIN_FAIL_USERNAME = "LOGIN_FAIL_USERNAME";

    /*
     * This block is for the getting personal information
     */
    public static final String GET_PERSONAL_INFO = "GET_PERSONAL_INFO";
    public static final String VALID_USER = "VALID_USER";
    public static final String INVALID_USER = "INVALID_USER";


    /*
     * This block is for the getting online user
     */
    public static final String REQUEST_ONLINE = "REQUEST_ONLINE";
    public static final String NOTIFY_ONLINE = "NOTIFY_ONLINE";
    public static final String END_NOTIFY_ONLINE = "END_NOTIFY_ONLINE";

    /*
     * This block is for the request type of socket messenger
     */
    public static final String CHAT_SOCKET = "CHAT_SOCKET";
    public static final String FILE_SOCKET = "FILE_SOCKET";
    public static final String REQUEST_CALL = "REQUEST_CALL";


    /*
     * This block is for the request type of messenger
     */
    public static final String TEXT_MESSAGE = "TEXT_MESSAGE";
    public static final String FILE_MESSAGE = "FILE_MESSAGE";
    public static final String END_CHAT = "END_CHAT";

    /*
     * This block is for the response of call request
     */
    public static final String CALL_OK = "CALL_OK";
    public static final String CALL_DENY = "CALL_DENY";

    // this action is for the log out action
    public static final String LOGOUT_ACTION = "LOGOUT";
//    public static final String LOGOUT_DONE = "LOGOUT_DONE";

    //Group
    public static final String MESSAGE_IN_GROUP = "MESSAGE_IN_GROUP";
    public static final String GROUP_ACTION = "GROUP_ACTION";
    public static final String JOIN_TO_GROUP = "JOIN_TO_GROUP";
    public static final String REQ_TO_GET_MESSAGE = "REQ_TO_GET_MESSAGE";
    public static final String NOTIFY_JOIN_TO_GROUP = "NOTIFY_JOIN_TO_GROUP";
    public static final String END_NOTIFY_JOIN_TO_GROUP = "END_NOTIFY_JOIN_TO_GROUP";
    public static final String MESSAGE_RESPONE_IN_GROUP = "MESSAGE_RESPONE_IN_GROUP";
    public static final String END_MESSAGE_RESPONE_IN_GROUP = "END_MESSAGE_RESPONE_IN_GROUP";
    public static final String MESSAGE_SINGLE_RESPONE_IN_GROUP = "MESSAGE_SINGLE_RESPONE_IN_GROUP";
    public static final String END_MESSAGE_SINGLE_RESPONE_IN_GROUP = "END_MESSAGE_SINGLE_RESPONE_IN_GROUP";
    public static final String OUT_GROUP = "OUT_GROUP";


    public static final String IP_SOCKET_SERVER = "192.168.137.1";


}
