package huy.nguyen.androidclient.Authentication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Pattern;

import huy.nguyen.androidclient.AuthenUtil;
import huy.nguyen.androidclient.Home.HomeActivity;
import huy.nguyen.androidclient.R;
import huy.nguyen.androidclient.SignupCallback;

public class LoginActivity extends AppCompatActivity {

    EditText edtUsername,edtPassword;
    Button btnLogin;
    TextView txtSignUp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        addControls();
        txtSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(LoginActivity.this,SignUpActivity.class);
                startActivity(intent);
                finish();
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkNull()){
                    Intent intent=new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
    private boolean checkNull(){
        if(TextUtils.isEmpty(edtUsername.getText().toString())){
            edtUsername.setError("Username is null");
            return false;
        }
        if(TextUtils.isEmpty(edtPassword.getText().toString())){
            edtPassword.setError("Password is null");
            return false;
        }
        return true;
    }

    private void addControls() {
        edtUsername=findViewById(R.id.edtUsernameLogin);
        edtPassword=findViewById(R.id.edtPasswordLogin);
        btnLogin=findViewById(R.id.btnLogin);
        txtSignUp=findViewById(R.id.txtClickSignUpInLogin);

    }
}
