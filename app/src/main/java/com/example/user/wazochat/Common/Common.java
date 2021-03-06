package com.example.user.wazochat.Common;

import com.example.user.wazochat.Holder.QBUserHolder;
import com.quickblox.users.model.QBUser;

import java.util.List;

/**
 * Created by Eze on 2/21/2018.
 */

public class Common {

    public static final String DIALOG_EXTRA = "Dialogs";

    public static final String UPDATE_DIALOG_EXTRA = "chatDialogs";
    public static final String UPDATE_ADD_MODE = "add";
    public static final String UPDATE_REMOVE_MODE = "remove";
    public static final String UPDATE_MODE = "mODE";

    public static  String createChatDialogName(List<Integer> qbUsers){

        List<QBUser> qbUsers1 = QBUserHolder.getInstance().getUsersByIds(qbUsers);
        StringBuilder name = new StringBuilder();
        for (QBUser user:qbUsers1)
            name.append(user.getFullName()).append(" ");
        if (name.length() > 30)
            name = name.replace(30,name.length()-1,"....");

        return name.toString();

    }

    public static boolean isNullOrEmptySting(String content){
        return (content != null && !content.trim().isEmpty()?false:true);
    }
}
