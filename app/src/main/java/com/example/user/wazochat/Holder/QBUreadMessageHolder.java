package com.example.user.wazochat.Holder;

import android.os.Bundle;

/**
 * Created by Eze on 2/27/2018.
 */

public class QBUreadMessageHolder {
    private static QBUreadMessageHolder instance;
    private Bundle bundle;

    public static synchronized QBUreadMessageHolder getInstance(){
        QBUreadMessageHolder qbUreadMessageHolder;
        synchronized (QBUreadMessageHolder.class){
            if (instance == null)
                instance = new QBUreadMessageHolder();
            qbUreadMessageHolder = instance;

        }
        return qbUreadMessageHolder;
    }

    public QBUreadMessageHolder() {
        bundle = new Bundle();
    }

    public void setBundle(Bundle bundle){
        this.bundle = bundle;
    }

    public Bundle getBundle(){
        return this.bundle;
    }

    public int getUnreadMessageByDialogId(String id){
        return this.bundle.getInt(id);
    }
}
