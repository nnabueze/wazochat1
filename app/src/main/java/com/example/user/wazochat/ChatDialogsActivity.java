package com.example.user.wazochat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.user.wazochat.Adapter.ChatDialogAdapters;
import com.example.user.wazochat.Common.Common;
import com.example.user.wazochat.Holder.QBChatDialogHolder;
import com.example.user.wazochat.Holder.QBUreadMessageHolder;
import com.example.user.wazochat.Holder.QBUserHolder;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.session.BaseService;
import com.quickblox.auth.session.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBIncomingMessagesManager;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.listeners.QBSystemMessageListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ChatDialogsActivity extends AppCompatActivity implements QBSystemMessageListener, QBChatDialogMessageListener {

    FloatingActionButton floatingActionButton;
    ListView lstChatDialog;

    @Override
    protected void onResume() {
        super.onResume();
        loadChatDialogs();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_dialogs);

        //adding toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.chat_dialog_toolbar);
        toolbar.setTitle("WazobiaChat");
        setSupportActionBar(toolbar);

        createSessioForChat();

        lstChatDialog = (ListView) findViewById(R.id.lstChatDialogs);
        lstChatDialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                QBChatDialog qbChatDialog = (QBChatDialog) lstChatDialog.getAdapter().getItem(i);
                Intent intent = new Intent(ChatDialogsActivity.this, ChatMessageActivity.class);
                intent.putExtra(Common.DIALOG_EXTRA, qbChatDialog);
                startActivity(intent);
            }
        });

        loadChatDialogs();

        floatingActionButton = (FloatingActionButton) findViewById(R.id.chatDialog_addUser);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatDialogsActivity.this, ListUserActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadChatDialogs() {
        QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
        requestBuilder.setLimit(100);

        QBRestChatService.getChatDialogs(null, requestBuilder).performAsync(new QBEntityCallback<ArrayList<QBChatDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBChatDialog> qbChatDialogs, Bundle bundle) {
                //put all dialog to cache
                QBChatDialogHolder.getInstatnce().putDialogs(qbChatDialogs);

                //unread setting
                Set<String> setIds = new HashSet<>();
                for (QBChatDialog chatDialog:qbChatDialogs)
                    setIds.add(chatDialog.getDialogId());

                QBRestChatService.getTotalUnreadMessagesCount(setIds, QBUreadMessageHolder.getInstance().getBundle())
                        .performAsync(new QBEntityCallback<Integer>() {
                            @Override
                            public void onSuccess(Integer integer, Bundle bundle) {
                                //save to cache
                                QBUreadMessageHolder.getInstance().setBundle(bundle);

                                //refresh list dialog
                                ChatDialogAdapters adapter = new ChatDialogAdapters(getBaseContext(), QBChatDialogHolder.getInstatnce().getAllChatDialogs());
                                lstChatDialog.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onError(QBResponseException e) {

                            }
                        });
            }

            @Override
            public void onError(QBResponseException e) {
                Log.e("Error",""+e.getMessage());
            }
        });
    }

    private void createSessioForChat() {
        final ProgressDialog mDialog = new ProgressDialog(ChatDialogsActivity.this);
        mDialog.setMessage("Please wait...");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        String user, password;
        user = getIntent().getStringExtra("user");
        password = getIntent().getStringExtra("password");

        QBUsers.getUsers(null).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                QBUserHolder.getInstance().putUsers(qbUsers);
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });

        final QBUser qbUser = new QBUser(user, password);
        QBAuth.createSession(qbUser).performAsync(new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {
                qbUser.setId(qbSession.getUserId());
                try {
                    qbUser.setPassword(BaseService.getBaseService().getToken());
                } catch (BaseServiceException e) {
                    e.printStackTrace();
                }

                QBChatService.getInstance().login(qbUser, new QBEntityCallback() {
                    @Override
                    public void onSuccess(Object o, Bundle bundle) {
                        mDialog.dismiss();

                        QBSystemMessagesManager qbSystemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();
                        qbSystemMessagesManager.addSystemMessageListener(ChatDialogsActivity.this);

                        QBIncomingMessagesManager qbIncomingMessagesManager = QBChatService.getInstance().getIncomingMessagesManager();
                        qbIncomingMessagesManager.addDialogMessageListener(ChatDialogsActivity.this);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Log.e("Error", ""+e.getMessage());
                    }
                });
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_dialog_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.chat_dialog_menu_user:
                showUserPofile();
                break;
            default:
                break;
        }
        return true;
    }

    private void showUserPofile() {
        Intent intent = new Intent(ChatDialogsActivity.this, UserProfile.class);
        startActivity(intent);
    }

    @Override
    public void processMessage(QBChatMessage qbChatMessage) {
        //put dialog to cache
        QBRestChatService.getChatDialogById(qbChatMessage.getBody()).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                QBChatDialogHolder.getInstatnce().putDialog(qbChatDialog);
                ArrayList<QBChatDialog> adpterSource = QBChatDialogHolder.getInstatnce().getAllChatDialogs();
                ChatDialogAdapters adapters = new ChatDialogAdapters(getBaseContext(), adpterSource);
                lstChatDialog.setAdapter(adapters);
                adapters.notifyDataSetChanged();
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }

    @Override
    public void processError(QBChatException e, QBChatMessage qbChatMessage) {
        Log.e("Error",""+e.getMessage());
    }

    @Override
    public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {
        loadChatDialogs();
    }

    @Override
    public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {

    }
}
