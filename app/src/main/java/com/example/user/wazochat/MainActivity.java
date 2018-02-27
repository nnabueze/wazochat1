package com.example.user.wazochat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.quickblox.auth.session.QBSettings;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class MainActivity extends AppCompatActivity {

    static final String APP_ID ="68789";
    static final String AUTH_KEY ="S7jJjDdxenGJCbk";
    static final String AUTH_SECRET ="a5atJS9QNVQYWTf";
    static final String ACCOUNT_KEY ="y5PSMKXw7yqLt6JtEqrd";

    Button btnLogin, btnSignup;
    EditText edtUser, edtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeFrameWork();

        btnLogin =(Button) findViewById(R.id.main_btnLogin);
        btnSignup = (Button) findViewById(R.id.main_btnSignup);

        edtPassword = (EditText) findViewById(R.id.main_editPassword);
        edtUser = (EditText) findViewById(R.id.main_editLogin);

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,SignUpActivity.class));
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String userLogin = edtUser.getText().toString();
                final String password = edtPassword.getText().toString();

                QBUser user1 = new QBUser(userLogin, password);
                QBUsers.signIn(user1).performAsync( new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser user, Bundle args) {
                        // success
                        Toast.makeText(MainActivity.this, "Testing Login Successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, ChatDialogsActivity.class);
                        intent.putExtra("user",userLogin);
                        intent.putExtra("password",password);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onError(QBResponseException error) {
                        // error
                        Toast.makeText(MainActivity.this, "hello:"+error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void initializeFrameWork() {
        QBSettings.getInstance().init(getApplicationContext(), APP_ID,AUTH_KEY,AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(ACCOUNT_KEY);
    }
}
