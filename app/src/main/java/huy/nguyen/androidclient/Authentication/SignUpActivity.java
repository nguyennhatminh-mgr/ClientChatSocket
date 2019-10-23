package huy.nguyen.androidclient.Authentication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.Socket;

import huy.nguyen.androidclient.AuthenUtil;
import huy.nguyen.androidclient.R;
import huy.nguyen.androidclient.SignupCallback;

public class SignUpActivity extends AppCompatActivity {

    EditText edtAccountName,edtUsername,edtPassword;
    Button btnSignUp;
    TextView txtBackToLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        createSocket();
        addControls();
        txtBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(SignUpActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SignUpActivity.this,"abc",Toast.LENGTH_SHORT).show();
                AuthenUtil.doSignUp("a", "b", "c", new SignupCallback() {
                    @Override
                    public void notify(String result) {
                        Toast.makeText(SignUpActivity.this,result,Toast.LENGTH_SHORT).show();
                        Log.e("ok",result);
                    }
                });
            }
        });

    }

    private void createSocket() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket("192.168.137.1",8080);
                    AuthenUtil.setSocket(socket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void addControls() {
        edtAccountName=findViewById(R.id.edtAccountNameSignUp);
        edtUsername=findViewById(R.id.edtUsernameSignUp);
        edtPassword=findViewById(R.id.edtPasswordSignUp);
        btnSignUp=findViewById(R.id.btnSignUp);
        txtBackToLogin=findViewById(R.id.txtBackToLoginInSignUp);
    }

}
