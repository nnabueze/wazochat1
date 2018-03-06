package com.example.user.wazochat;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.user.wazochat.Adapter.ListUsersAdapter;
import com.example.user.wazochat.Common.Common;
import com.example.user.wazochat.Holder.QBUserHolder;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.request.QBDialogRequestBuilder;
import com.quickblox.chat.utils.DialogUtils;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;

import java.util.ArrayList;
import java.util.List;

public class ListUserActivity extends AppCompatActivity {
    ListView lstUser;
    Button btnCreateChat;

    String mode = "";
    QBChatDialog qbChatDialog;
    List<QBUser> userAdd = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_user);

        mode = getIntent().getStringExtra(Common.UPDATE_MODE);
        qbChatDialog = (QBChatDialog) getIntent().getSerializableExtra(Common.UPDATE_DIALOG_EXTRA);


        lstUser = (ListView) findViewById(R.id.lstUser);
        lstUser.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        btnCreateChat = (Button) findViewById(R.id.btn_create_chat);
        btnCreateChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mode == null) {
                    int countChoice = lstUser.getCount();
                    if (lstUser.getCheckedItemPositions().size() == 1) {
                        createPrivateChat(lstUser.getCheckedItemPositions());
                    } else if (lstUser.getCheckedItemPositions().size() > 1) {
                        createGroupChat(lstUser.getCheckedItemPositions());
                    } else {
                        Toast.makeText(ListUserActivity.this, "Please select friend to chat", Toast.LENGTH_SHORT).show();
                    }
                } else if (mode.equals(Common.UPDATE_ADD_MODE) && qbChatDialog != null) {
                    if (userAdd.size() > 0) {
                        QBDialogRequestBuilder requestBuilder = new QBDialogRequestBuilder();

                        int cntChoice = lstUser.getCount();
                        SparseBooleanArray checkItemPosition = lstUser.getCheckedItemPositions();

                        for (int i = 0; i < cntChoice;i++){
                            if (checkItemPosition.get(i)){
                                QBUser user = (QBUser) lstUser.getItemAtPosition(i);
                                requestBuilder.addUsers(user);
                            }
                        }

                        //call service
                        QBRestChatService.updateGroupChatDialog(qbChatDialog, requestBuilder)
                                .performAsync(new QBEntityCallback<QBChatDialog>() {
                                    @Override
                                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                                        Toast.makeText(ListUserActivity.this, "Added User Successfully", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }

                                    @Override
                                    public void onError(QBResponseException e) {

                                    }
                                });
                    }

                }else if (mode.equals(Common.UPDATE_REMOVE_MODE) && qbChatDialog != null){
                    if (userAdd.size() > 0){
                        QBDialogRequestBuilder requestBuilder = new QBDialogRequestBuilder();

                        int cntChoice = lstUser.getCount();
                        SparseBooleanArray checkItemPosition = lstUser.getCheckedItemPositions();

                        for (int i = 0; i < cntChoice;i++){
                            if (checkItemPosition.get(i)){
                                QBUser user = (QBUser) lstUser.getItemAtPosition(i);
                                requestBuilder.removeUsers(user);
                            }
                        }

                        //call service
                        QBRestChatService.updateGroupChatDialog(qbChatDialog, requestBuilder)
                                .performAsync(new QBEntityCallback<QBChatDialog>() {
                                    @Override
                                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                                        Toast.makeText(ListUserActivity.this, "Remove User Successfully", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }

                                    @Override
                                    public void onError(QBResponseException e) {
                                        Toast.makeText(ListUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });
                    }
                }
            }
        });

        if (mode == null && qbChatDialog == null)
            retrieveAllUser();
        else {
            if (mode.equals(Common.UPDATE_ADD_MODE))
                loadListAvailableUser();
            else if (mode.equals(Common.UPDATE_REMOVE_MODE))
                loadListUserInGroup();
        }
    }

    private void loadListUserInGroup() {
        btnCreateChat.setText("Remove User");
        QBRestChatService.getChatDialogById(qbChatDialog.getDialogId())
                .performAsync(new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                        List<Integer> occupantsId = qbChatDialog.getOccupants();
                        List<QBUser> listUserAreadyInGroup = QBUserHolder.getInstance().getUsersByIds(occupantsId);
                        ArrayList<QBUser> users = new ArrayList<>();
                        users.addAll(listUserAreadyInGroup);

                        ListUsersAdapter adapter = new ListUsersAdapter(getBaseContext(), users);
                        lstUser.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        userAdd = users;

                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(ListUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void loadListAvailableUser() {
        btnCreateChat.setText("Add User");

        QBRestChatService.getChatDialogById(qbChatDialog.getDialogId())
                .performAsync(new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                        ArrayList<QBUser> listUser = QBUserHolder.getInstance().getAllUsers();
                        List<Integer> occupantId = qbChatDialog.getOccupants();
                        List<QBUser> listUserAreadyInChatGroup = QBUserHolder.getInstance().getUsersByIds(occupantId);

                        //remove all users already in group chat
                        for (QBUser user : listUserAreadyInChatGroup)
                            listUser.remove(user);
                        if (listUser.size() > 0) {
                            ListUsersAdapter adapter = new ListUsersAdapter(getBaseContext(), listUser);
                            lstUser.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                            userAdd = listUser;
                        }


                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(ListUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }

    private void createGroupChat(SparseBooleanArray checkedItemPositions) {
        final ProgressDialog mDialog = new ProgressDialog(ListUserActivity.this);
        mDialog.setMessage("Please Waiting....");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        int countChoice = lstUser.getCount();
        ArrayList<Integer> occupantIdsList = new ArrayList<>();

        for (int i = 0; i < countChoice; i++) {
            if (checkedItemPositions.get(i)) {
                QBUser user = (QBUser) lstUser.getItemAtPosition(i);
                occupantIdsList.add(user.getId());
            }
        }

        //create chat dialog
        QBChatDialog dialog = new QBChatDialog();
        dialog.setName(Common.createChatDialogName(occupantIdsList));
        dialog.setType(QBDialogType.GROUP);
        dialog.setOccupantsIds(occupantIdsList);

        QBRestChatService.createChatDialog(dialog).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                mDialog.dismiss();
                Toast.makeText(getBaseContext(), "Create chat dialog successful", Toast.LENGTH_SHORT).show();

                QBSystemMessagesManager qbSystemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();
                QBChatMessage qbChatMessage = new QBChatMessage();
                qbChatMessage.setBody(qbChatDialog.getDialogId());
                for (int i = 0; i < qbChatDialog.getOccupants().size(); i++) {
                    qbChatMessage.setRecipientId(qbChatDialog.getOccupants().get(i));
                    try {
                        qbSystemMessagesManager.sendSystemMessage(qbChatMessage);
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }
                }


                finish();
            }

            @Override
            public void onError(QBResponseException e) {
                Log.e("ERROR", e.getMessage());
            }
        });
    }

    private void createPrivateChat(SparseBooleanArray checkedItemPositions) {
        final ProgressDialog mDialog = new ProgressDialog(ListUserActivity.this);
        mDialog.setMessage("Please Waiting....");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        int countChoice = lstUser.getCount();

        for (int i = 0; i < countChoice; i++) {
            if (checkedItemPositions.get(i)) {
                final QBUser user = (QBUser) lstUser.getItemAtPosition(i);
                QBChatDialog dialog = DialogUtils.buildPrivateDialog(user.getId());

                QBRestChatService.createChatDialog(dialog).performAsync(new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                        mDialog.dismiss();
                        Toast.makeText(getBaseContext(), "Create private chat dialog successful", Toast.LENGTH_SHORT).show();
                        QBSystemMessagesManager qbSystemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();
                        QBChatMessage qbChatMessage = new QBChatMessage();
                        qbChatMessage.setRecipientId(user.getId());
                        qbChatMessage.setBody(qbChatDialog.getDialogId());
                        try {
                            qbSystemMessagesManager.sendSystemMessage(qbChatMessage);
                        } catch (SmackException.NotConnectedException e) {
                            e.printStackTrace();
                        }


                        finish();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Log.e("Error", e.getMessage());
                    }
                });
            }
        }
    }

    private void retrieveAllUser() {
        QBUsers.getUsers(null).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {

                //adding user to cache
                QBUserHolder.getInstance().putUsers(qbUsers);

                ArrayList<QBUser> qbUserWithoutCurrent = new ArrayList<QBUser>();
                for (QBUser user : qbUsers) {
                    if (!user.getLogin().equals(QBChatService.getInstance().getUser().getLogin())) {
                        qbUserWithoutCurrent.add(user);
                    }
                }

                ListUsersAdapter adapter = new ListUsersAdapter(getBaseContext(), qbUserWithoutCurrent);
                lstUser.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(QBResponseException e) {
                Log.e("Error", "" + e.getMessage());
            }
        });
    }
}
