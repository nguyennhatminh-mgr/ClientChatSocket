package huy.nguyen.androidclient.Authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import huy.nguyen.androidclient.R;
import huy.nguyen.androidclient.Utilities.Interface.SignupCallback;
import huy.nguyen.androidclient.Utilities.SocketUtil;

public class SignUpActivity extends AppCompatActivity {

    EditText edtAccountName,edtUsername,edtPassword;
    Button btnSignUp;
    TextView txtBackToLogin;
    int checkUsername=0;
    int checkPassword=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

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
                if(checkUsername==1 && checkPassword==1) {
                    SocketUtil.doSignUp(edtUsername.getText().toString(), edtPassword.getText().toString(), edtAccountName.getText().toString(), new SignupCallback() {
                        @Override
                        public void notifySignup(final String result) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (result.equals("SIGNUP_SUCCESS")) {
                                        Toast.makeText(SignUpActivity.this, result, Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                                        intent.putExtra("username",edtUsername.getText().toString());
                                        intent.putExtra("password",edtPassword.getText().toString());
                                        startActivity(intent);
                                        finish();
                                    } else if (result.equals("SIGNUP_FAIL_USERNAME")) {
                                        edtUsername.setError("User name is exist");
                                    }
                                }
                            });
                        }
                    });
                }
                else if(checkUsername==1 && checkPassword!=1){
                    Toast.makeText(SignUpActivity.this,"Password is wrong format",Toast.LENGTH_SHORT).show();
                }
                else if(checkUsername!=1 && checkPassword==1){
                    Toast.makeText(SignUpActivity.this,"Username is wrong format",Toast.LENGTH_SHORT).show();
                }
//                AuthenUtil.doLogin("helloaa", "ba", new LoginCallback() {
//                    @Override
//                    public void notifyLogin(final String result) {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(SignUpActivity.this,result,Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                    }
//                });

            }
        });
        checkRegression();
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
                if(edtUsername.getText().toString().matches("[0-9a-zA-Z_]+")){
                    checkUsername=1;
                }
                else{
//                    Toast.makeText(SignUpActivity.this,"Not valid password",Toast.LENGTH_SHORT).show();
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
                if(edtPassword.getText().toString().matches("[0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z]([0-9a-zA-Z])*")){
                    checkPassword=1;
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
