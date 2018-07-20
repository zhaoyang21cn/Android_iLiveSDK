package com.tencent.qcloud.videocall.bussiness.view;

import com.tencent.qcloud.videocall.bussiness.model.LoginInfo;

/**
 * 从业务服务器获取登录接口
 */
public interface SyncUserInfoView {
    /** 获取登录信息成功 */
    void onSyncLoginSuccess(LoginInfo info);
    /** 获取登录信息失败 */
    void onSyncLoginFailed(int code, String errInfo);
}
