package huy.nguyen.androidclient.Authentication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
//                AuthenUtil.doSignUp("helloa", "b", "c", new SignupCallback() {
//                    @Override
//                    public void notifySignup(final String result) {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(SignUpActivity.this,result,Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                    }
//                });
                AuthenUtil.doLogin("helloaa", "ba", new LoginCallback() {
                    @Override
                    public void notifyLogin(final String result) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SignUpActivity.this,result,Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });
        checkRegression();
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

    private void checkRegression(){
        edtUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(edtUsername.getText().toString().matches("[0-9a-zA-Z_]*")){

                }
                else{
//                    Toast.makeText(SignUpActivity.this,"Not valid password",Toast.LENGTH_SHORT).show();
                    edtUsername.setError("Not valid password");
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
                if(edtPassword.getText().toString().matches("([0-9][0-9][0-9][0-9][0-9][0-9])*")){

                }
                else{
                    edtPassword.setError("Password is at least 6 characters");
                }
            }
        });
    }
    private void addControls() {
        edtAccountName=findViewById(R.id.edtAccountNameSignUp);
        edtUsername=findViewById(R.id.edtUsernameSignUp);
        edtPassword=findViewById(R.id.edtPasswordSignUp);
        btnSignUp=findViewById(R.id.btnSignUp);
        txtBackToLogin=findViewById(R.id.txtBackToLoginInSignUp);
    }

}
