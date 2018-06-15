package com.tencent.qcloud.videocall.bussiness.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.tencent.qcloud.videocall.ui.Constants;

/**
 * Created by tencent on 2018/5/21.
 */
public class UserInfo {
    private String userId = "";
    private String nickName = "";
    private String userSig = "";
    private int curRoomId = 0;

    private static UserInfo instance;

    public static UserInfo getInstance() {
        if (null == instance) {
            synchronized (UserInfo.class) {
                if (null == instance) {
                    instance = new UserInfo();
                }
            }
        }
        return instance;
    }

    private UserInfo() {
    }


    public void writeToCache(Context context) {
        SharedPreferences shareInfo = context.getSharedPreferences(Constants.PER_DATA, 0);
        SharedPreferences.Editor editor = shareInfo.edit();
        editor.putString(Constants.PER_USERID, userId);
        editor.putString(Constants.PER_USERSIG, userSig);
        editor.commit();
    }

    public void clearCache(Context context) {
        SharedPreferences shareInfo = context.getSharedPreferences(Constants.PER_DATA, 0);
        SharedPreferences.Editor editor = shareInfo.edit();
        editor.clear();
        editor.commit();
    }

    public void getCache(Context context) {
        SharedPreferences shareInfo = context.getSharedPreferences(Constants.PER_DATA, 0);
        userId = shareInfo.getString(Constants.PER_USERID, null);
        userSig = shareInfo.getString(Constants.PER_USERSIG, null);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserSig() {
        return userSig;
    }

    public void setUserSig(String userSig) {
        this.userSig = userSig;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public int getCurRoomId() {
        return curRoomId;
    }

    public void setCurRoomId(int curRoomId) {
        this.curRoomId = curRoomId;
    }
}
