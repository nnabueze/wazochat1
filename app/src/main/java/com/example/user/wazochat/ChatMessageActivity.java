package com.example.user.wazochat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.example.user.wazochat.Adapter.ChatMessageAdapter;
import com.example.user.wazochat.Common.Common;
import com.example.user.wazochat.Holder.QBChatMessagesHolder;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBIncomingMessagesManager;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.listeners.QBChatDialogParticipantListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.model.QBPresence;
import com.quickblox.chat.request.QBDialogRequestBuilder;
import com.quickblox.chat.request.QBMessageGetBuilder;
import com.quickblox.chat.request.QBMessageUpdateBuilder;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

import java.util.ArrayList;
import java.util.Collection;

public class ChatMessageActivity extends AppCompatActivity implements QBChatDialogMessageListener {

    QBChatDialog qbChatDialog;
    ListView lstChatMessages;
    ImageButton submitButtom;
    EditText edtContent;

    ChatMessageAdapter adapter;

    //variable for edit/update message
    int contextMenuIndexClicked = -1;
    boolean isEditMode = false;
    QBChatMessage editMessage;

    //update online users
    ImageView image_online_user;
    TextView txt_online_user;
    LinearLayout dialoG_info;

    //Toolbar
    Toolbar toolbar;

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        contextMenuIndexClicked = info.position;

        switch (item.getItemId()) {
            case R.id.chat_message_update_message:
                updateMessage();
                break;
            case R.id.chat_message_delete_message:
                deleteMessage();
                break;
            default:
                break;

        }
        return true;
    }

    private void deleteMessage() {
        final ProgressDialog deleteDialog = new ProgressDialog(ChatMessageActivity.this);
        deleteDialog.setTitle("Please Wait....");
        deleteDialog.show();


        editMessage = QBChatMessagesHolder.getInstance().getChatMessageByDialogId(qbChatDialog.getDialogId())
                .get(contextMenuIndexClicked);
        QBRestChatService.deleteMessage(editMessage.getId(), false).performAsync(new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                retrieveMessage();
                deleteDialog.dismiss();
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }

    private void updateMessage() {
        editMessage = QBChatMessagesHolder.getInstance().getChatMessageByDialogId(qbChatDialog.getDialogId())
                .get(contextMenuIndexClicked);
        edtContent.setText(editMessage.getBody());
        isEditMode = true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.chat_message_context_menu, menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        qbChatDialog.removeMessageListrener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        qbChatDialog.removeMessageListrener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (qbChatDialog.getType() == QBDialogType.PUBLIC_GROUP || qbChatDialog.getType() == QBDialogType.GROUP)
            getMenuInflater().inflate(R.menu.chat_message_group_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.chat_group_edit_name:
                editGroupName();
                break;
            case R.id.chat_group_add_user:
                addUser();
                break;
            case R.id.chat_group_remove_user:
                removeUser();
                break;
            default:
                break;

        }
        return true;
    }

    private void removeUser() {
        Intent intent = new Intent(this, ListUserActivity.class);
        intent.putExtra(Common.UPDATE_DIALOG_EXTRA, qbChatDialog);
        intent.putExtra(Common.UPDATE_MODE, Common.UPDATE_REMOVE_MODE);
        startActivity(intent);
    }

    private void addUser() {
        Intent intent = new Intent(this, ListUserActivity.class);
        intent.putExtra(Common.UPDATE_DIALOG_EXTRA, qbChatDialog);
        intent.putExtra(Common.UPDATE_MODE, Common.UPDATE_ADD_MODE);
        startActivity(intent);
    }

    private void editGroupName() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_edit_group_layout, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(view);

        final EditText newName = (EditText) view.findViewById(R.id.edt_new_group_name);
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        qbChatDialog.setName(newName.getText().toString());

                        QBDialogRequestBuilder requestBuilder = new QBDialogRequestBuilder();
                        QBRestChatService.updateGroupChatDialog(qbChatDialog, requestBuilder)
                                .performAsync(new QBEntityCallback<QBChatDialog>() {
                                    @Override
                                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                                        Toast.makeText(ChatMessageActivity.this, "Group Name Edited", Toast.LENGTH_SHORT).show();
                                        toolbar.setTitle(qbChatDialog.getName());
                                    }

                                    @Override
                                    public void onError(QBResponseException e) {
                                        Toast.makeText(ChatMessageActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        //create dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_message);

        initView();
        initChatDialog();

        retrieveMessage();

        submitButtom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if (! edtContent.getText().toString().isEmpty()){
                if (!isEditMode) {
                    QBChatMessage chatMessage = new QBChatMessage();
                    chatMessage.setBody(edtContent.getText().toString());
                    chatMessage.setSenderId(QBChatService.getInstance().getUser().getId());
                    chatMessage.setSaveToHistory(true);
                    try {
                        qbChatDialog.sendMessage(chatMessage);
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }
                    //fix private chat dont show
                    if (qbChatDialog.getType() == QBDialogType.PRIVATE) {

                        QBChatMessagesHolder.getInstance().putMessage(qbChatDialog.getDialogId(), chatMessage);
                        ArrayList<QBChatMessage> messages = QBChatMessagesHolder.getInstance().getChatMessageByDialogId(chatMessage.getDialogId());

                        adapter = new ChatMessageAdapter(getBaseContext(), messages);
                        lstChatMessages.setAdapter(adapter);
                        adapter.notifyDataSetChanged();

                    }

                    //remove text from editor
                    edtContent.setText("");
                    edtContent.setFocusable(true);
                } else {
                    final ProgressDialog updateDialog = new ProgressDialog(ChatMessageActivity.this);
                    updateDialog.setTitle("Please Wait....");
                    updateDialog.show();

                    QBMessageUpdateBuilder messageUpdateBuilder = new QBMessageUpdateBuilder();
                    messageUpdateBuilder.updateText(edtContent.getText().toString()).markDelivered().markRead();

                    QBRestChatService.updateMessage(editMessage.getId(), qbChatDialog.getDialogId(), messageUpdateBuilder)
                            .performAsync(new QBEntityCallback<Void>() {
                                @Override
                                public void onSuccess(Void aVoid, Bundle bundle) {
                                    //Refresh data
                                    retrieveMessage();
                                    isEditMode = false;
                                    updateDialog.dismiss();

                                    //refershedit text
                                    edtContent.setText("");
                                    edtContent.setFocusable(true);
                                }

                                @Override
                                public void onError(QBResponseException e) {
                                    isEditMode = false;

                                    edtContent.setText("");
                                    edtContent.setFocusable(true);

                                    updateDialog.dismiss();
                                    Toast.makeText(ChatMessageActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }

            }
            }
        });
    }

    private void retrieveMessage() {
        QBMessageGetBuilder messageGetBuilder = new QBMessageGetBuilder();
        messageGetBuilder.setLimit(500);

        if (qbChatDialog != null) {
            QBRestChatService.getDialogMessages(qbChatDialog, messageGetBuilder).performAsync(new QBEntityCallback<ArrayList<QBChatMessage>>() {
                @Override
                public void onSuccess(ArrayList<QBChatMessage> qbChatMessages, Bundle bundle) {
                    QBChatMessagesHolder.getInstance().putMessages(qbChatDialog.getDialogId(), qbChatMessages);
                    adapter = new ChatMessageAdapter(getBaseContext(), qbChatMessages);
                    lstChatMessages.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onError(QBResponseException e) {

                }
            });
        }
    }

    private void initChatDialog() {
        qbChatDialog = (QBChatDialog) getIntent().getSerializableExtra(Common.DIALOG_EXTRA);
        qbChatDialog.initForChat(QBChatService.getInstance());
        QBIncomingMessagesManager incomingMessage = QBChatService.getInstance().getIncomingMessagesManager();
        incomingMessage.addDialogMessageListener(new QBChatDialogMessageListener() {
            @Override
            public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {

            }

            @Override
            public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {

            }
        });

        //add join group to test group chat
        if (qbChatDialog.getType() == QBDialogType.PUBLIC_GROUP || qbChatDialog.getType() == QBDialogType.GROUP) {
            DiscussionHistory discussionHistory = new DiscussionHistory();
            discussionHistory.setMaxStanzas(0);

            qbChatDialog.join(discussionHistory, new QBEntityCallback() {
                @Override
                public void onSuccess(Object o, Bundle bundle) {

                }

                @Override
                public void onError(QBResponseException e) {
                    Log.e("Error", "" + e.getMessage());
                }
            });
            dialoG_info.setVisibility(View.VISIBLE);

            QBChatDialogParticipantListener participantListener = new QBChatDialogParticipantListener() {
                @Override
                public void processPresence(String dialogId, QBPresence qbPresence) {
                    if (dialogId == qbChatDialog.getDialogId()) {
                        QBRestChatService.getChatDialogById(dialogId).performAsync(new QBEntityCallback<QBChatDialog>() {
                            @Override
                            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                                //getting online users
                                try {
                                    Collection<Integer> onlineList = qbChatDialog.getOnlineUsers();
                                    TextDrawable.IBuilder builder = TextDrawable.builder()
                                            .beginConfig()
                                            .width(4)
                                            .endConfig()
                                            .round();
                                    TextDrawable online = builder.build("", Color.RED);
                                    image_online_user.setImageDrawable(online);
                                    txt_online_user.setText(String.format("%d/%d online", onlineList.size(), qbChatDialog.getOccupants().size()));
                                } catch (XMPPException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onError(QBResponseException e) {

                            }
                        });
                    }
                }
            };

            qbChatDialog.addParticipantListener(participantListener);
        }


        qbChatDialog.addMessageListener(this);

        toolbar.setTitle(qbChatDialog.getName());
        setSupportActionBar(toolbar);
    }

    private void initView() {
        lstChatMessages = (ListView) findViewById(R.id.list_of_message);
        submitButtom = (ImageButton) findViewById(R.id.send_button);
        edtContent = (EditText) findViewById(R.id.edt_content);

        image_online_user = (ImageView) findViewById(R.id.image_online_count);
        txt_online_user = (TextView) findViewById(R.id.txt_online_count);
        dialoG_info = (LinearLayout) findViewById(R.id.dialog_info);

        //Add context menu
        registerForContextMenu(lstChatMessages);
        toolbar = (Toolbar) findViewById(R.id.chat_message_toolbar);
    }

    @Override
    public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {
        QBChatMessagesHolder.getInstance().putMessage(qbChatMessage.getDialogId(), qbChatMessage);
        ArrayList<QBChatMessage> messages = QBChatMessagesHolder.getInstance().getChatMessageByDialogId(qbChatMessage.getDialogId());

        adapter = new ChatMessageAdapter(getBaseContext(), messages);
        lstChatMessages.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {

    }
}
