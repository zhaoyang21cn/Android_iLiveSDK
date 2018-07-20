package com.tencent.qcloud.videocall.bussiness.model;

import org.json.JSONObject;

/**
 * 用户登录信息
 * 从业务服务器获取
 */
public class PrivateMapKeyInfo {
    private String userId;
    private int roomId;
    private String privateMapKey;

    public PrivateMapKeyInfo(JSONObject jsonInfo) throws Exception{
        userId = jsonInfo.getString(BussinessConstants.JSON_USERID);
        roomId = jsonInfo.getInt(BussinessConstants.JSON_ROOMID);
        privateMapKey = jsonInfo.getString(BussinessConstants.JSON_PRIVATEMAPKEY);
    }

    public String getUserId() {
        return userId;
    }

    public int getRoomId() {
        return roomId;
    }

    public String getPrivateMapKey() {
        return privateMapKey;
    }
}
