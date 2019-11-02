package huy.nguyen.androidclient.Authentication;

import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import huy.nguyen.androidclient.Home.HomeActivity;
import huy.nguyen.androidclient.R;
import huy.nguyen.androidclient.Utilities.Interface.LoginCallback;
import huy.nguyen.androidclient.Utilities.SocketProtocol;
import huy.nguyen.androidclient.Utilities.SocketUtil;

public class LoginActivity extends AppCompatActivity {

    EditText edtUsername, edtPassword;
    Button btnLogin;
    TextView txtSignUp;
    int checkUsername = 0;
    int checkPassword = 0;
    String MINH_IP = "192.168.137.1";
    String HUY_IP = "192.168.43.226";
    String TVTT_IP = "192.168.200.13";
    String TV_IP = "10.228.230.101";
    String KTX_IP = "172.17.23.47";
    String IP = HUY_IP;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        createSocket();
        addControls();
        txtSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
                finish();
            }
        });
        checkRegression();
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkNull()) {
                    SocketUtil.doLogin(edtUsername.getText().toString(), edtPassword.getText().toString(), new LoginCallback() {
                        @Override
                        public void notifyLogin(final String result) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (result.equals("LOGIN_SUCCESS")) {
                                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else if (result.equals("LOGIN_FAIL_PASSWORD")) {
                                        edtPassword.setError("Password is wrong");
//                                Toast.makeText(LoginActivity.this,"Password is wrong",Toast.LENGTH_SHORT).show();
                                    } else if (result.equals("LOGIN_FAIL_USERNAME")) {
                                        edtUsername.setError("Username is wrong");
//                                Toast.makeText(LoginActivity.this,"Username is wrong",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }
                    });

                }
            }
        });
    }

    private boolean checkNull() {
        if (TextUtils.isEmpty(edtUsername.getText().toString())) {
            edtUsername.setError("Username is not null");
            return false;
        }
        if (TextUtils.isEmpty(edtPassword.getText().toString())) {
            edtPassword.setError("Password is not null");
            return false;
        }
        return true;
    }

    private void checkRegression() {
        edtUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (edtUsername.getText().toString().matches("[0-9a-zA-Z_]+")) {
                    checkUsername = 1;
                } else {
                    edtUsername.setError("Not valid username");
                }
            }
        });
        edtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (edtPassword.getText().toString().matches("[0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z]([0-9a-zA-Z])*")) {
                    checkPassword = 1;
                } else {
                    edtPassword.setError("Password is at least 6 characters");
                }
            }
        });
    }

    private void addControls() {
        edtUsername = findViewById(R.id.edtUsernameLogin);
        edtPassword = findViewById(R.id.edtPasswordLogin);
        btnLogin = findViewById(R.id.btnLogin);
        txtSignUp = findViewById(R.id.txtClickSignUpInLogin);
        Intent intent = getIntent();
        if (intent.hasExtra("username")) {
            edtUsername.setText(intent.getStringExtra("username"));
            edtPassword.setText(intent.getStringExtra("password"));
        }

        SocketUtil.setServerIp(IP);
    }

    private String getLocalIpAddress() throws UnknownHostException {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        assert wifiManager != null;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipInt = wifiInfo.getIpAddress();
        return InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array()).getHostAddress();
    }

    private void createSocket() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(SocketProtocol.IP_SOCKET_SERVER, 8080);
                    SocketUtil.setSocket(socket);
                    SocketUtil.initSetup();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
