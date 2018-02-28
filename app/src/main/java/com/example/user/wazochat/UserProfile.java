package com.example.user.wazochat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.user.wazochat.Common.Common;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class UserProfile extends AppCompatActivity {

    EditText edtPassword, edtOldPassword, edtFullName, edtEmail, edtPhone;
    Button btnUpdate, btnCancel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        //setting toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.chat_update__toolbar);
        toolbar.setTitle("WazobiaChat");
        setSupportActionBar(toolbar);

        initView();

        loadUserProfile();

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                QBUser user = new QBUser();
                user.setId(QBChatService.getInstance().getUser().getId());

                if (!Common.isNullOrEmptySting(edtPassword.getText().toString()))
                    user.setPassword(edtPassword.getText().toString());
                if (!Common.isNullOrEmptySting(edtOldPassword.getText().toString()))
                    user.setOldPassword(edtOldPassword.getText().toString());
                if (!Common.isNullOrEmptySting(edtFullName.getText().toString()))
                    user.setFullName(edtFullName.getText().toString());
                if (!Common.isNullOrEmptySting(edtEmail.getText().toString()))
                    user.setEmail(edtEmail.getText().toString());
                if (!Common.isNullOrEmptySting(edtPhone.getText().toString()))
                    user.setPhone(edtPhone.getText().toString());

                final ProgressDialog mDialog = new ProgressDialog(UserProfile.this);
                mDialog.setMessage("Please Wait....");
                mDialog.show();

                QBUsers.updateUser(user).performAsync(new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        Toast.makeText(UserProfile.this, "User: "+qbUser.getLogin() +" updated", Toast.LENGTH_SHORT).show();
                        mDialog.dismiss();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(UserProfile.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void loadUserProfile() {
        QBUser currentUser = QBChatService.getInstance().getUser();
        edtFullName.setText(currentUser.getFullName());
        edtPhone.setText(currentUser.getPhone());
        edtEmail.setText(currentUser.getEmail());
    }

    private void initView() {
        btnCancel = (Button) findViewById(R.id.cancel_user_btn);
        btnUpdate = (Button) findViewById(R.id.update_user_btn);

        edtEmail = (EditText) findViewById(R.id.update_edt_email);
        edtPhone = (EditText) findViewById(R.id.update_edt_phone);
        edtFullName = (EditText) findViewById(R.id.update_edt_fullname);
        edtPassword = (EditText) findViewById(R.id.update_edt_new_password);
        edtOldPassword = (EditText) findViewById(R.id.update_edt_old_password);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_update_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.user_update_log_out:
                logOut();
                break;
            default:
                break;
        }
        return true;
    }

    private void logOut() {
        QBUsers.signOut().performAsync(new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                QBChatService.getInstance().logout(new QBEntityCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid, Bundle bundle) {
                        Toast.makeText(UserProfile.this, "You are logout !!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(UserProfile.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onError(QBResponseException e) {

                    }
                });
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }
}
