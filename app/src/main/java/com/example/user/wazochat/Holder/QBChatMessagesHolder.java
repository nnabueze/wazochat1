package com.example.user.wazochat.Holder;

import com.quickblox.chat.model.QBChatMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Eze on 2/24/2018.
 */

public class QBChatMessagesHolder {
    private static QBChatMessagesHolder instance;
    private HashMap<String, ArrayList<QBChatMessage>> qbChatMessageArray;

    public static synchronized QBChatMessagesHolder getInstance(){
        QBChatMessagesHolder qbChatMessagesHolder;
        synchronized (QBChatMessagesHolder.class){
            if (instance == null)
                instance = new QBChatMessagesHolder();
            qbChatMessagesHolder = instance;
        }

        return qbChatMessagesHolder;
    }

    private QBChatMessagesHolder(){
        this.qbChatMessageArray = new HashMap<>();
    }

    public void putMessages(String dialogId, ArrayList<QBChatMessage> qbChatMessages){
        qbChatMessageArray.put(dialogId, qbChatMessages);
    }

    public void putMessage(String dialogId, QBChatMessage qbChatMessage){
        List<QBChatMessage> lstResult = this.qbChatMessageArray.get(dialogId);
        lstResult.add(qbChatMessage);
        ArrayList<QBChatMessage> lstAdded = new ArrayList<>(lstResult.size());
        lstAdded.addAll(lstResult);
        putMessages(dialogId, lstAdded);
    }

    public  ArrayList<QBChatMessage> getChatMessageByDialogId(String dialog){
        return (ArrayList<QBChatMessage>) this.qbChatMessageArray.get(dialog);
    }
}
