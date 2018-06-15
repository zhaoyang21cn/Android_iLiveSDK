package com.tencent.qcloud.videocall.trtcsdk.view;

/**
 * SDK登录返回接口
 */
public interface LoginView {
    /** 登录成功 */
    void onLoginSuccess(String userId);
    /** 登录失败 */
    void onLoginFailed(String module, int errCode, String errMsg);
}
