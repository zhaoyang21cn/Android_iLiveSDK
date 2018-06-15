package com.tencent.qcloud.videocall.trtcsdk.view;

import com.tencent.ilivesdk.data.msg.ILiveTextMessage;

/**
 * 音视频房间回调接口
 */
public interface RoomView {
    /** 进入音视频房间成功 */
    void onRoomEnter();
    /** 进入房间失败 */
    void onEnterRoomFailed(String module, int errCode, String errMsg);
    /** 退出音视频房间成功 */
    void onRoomExit();
    /** 房间被回收 */
    void onRoomDisconnected(String module, int errCode, String errMsg);
    /** 房间异常事件 */
    void onRoomException(int eventId, int errCode, String errMsg);

    /** 切换角色成功 */
    void onChangeRoleSuccess();
    /** 切换角色失败 */
    void onChangeRoleFailed(String module, int errCode, String errMsg);

    /** 反馈加调 */
    void onFeedBackResult(String module, int errCode, String errMsg);

    /** 发送消息成功 */
    void onSendMessageSuccess(ILiveTextMessage message);
    /** 发送消息失败 */
    void onSendMessageFailed(String module, int errCode, String errMsg);
}
