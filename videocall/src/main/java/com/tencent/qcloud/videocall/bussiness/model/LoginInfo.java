package com.tencent.qcloud.videocall.bussiness.model;

import org.json.JSONObject;

/**
 * 用户登录信息
 * 从业务服务器获取
 */
public class LoginInfo {
    private String sdkAppId;
    private String accountType;
    private String userId;
    private String userSig;

    public LoginInfo(JSONObject jsonInfo) throws Exception{
        sdkAppId = jsonInfo.getString(BussinessConstants.JSON_SDKAPPID);
        accountType = jsonInfo.getString(BussinessConstants.JSON_ACCOUNTTYPE);
        userId = jsonInfo.getString(BussinessConstants.JSON_USERID);
        userSig = jsonInfo.getString(BussinessConstants.JSON_USERSIG);
    }

    public int getSdkAppId() {
        return Integer.valueOf(sdkAppId);
    }

    public void setSdkAppId(String sdkAppId) {
        this.sdkAppId = sdkAppId;
    }

    public int getAccountType() {
        return Integer.valueOf(accountType);
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
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
}
