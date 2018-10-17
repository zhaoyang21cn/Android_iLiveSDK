package com.tencent.qcloud.videocall.trtcsdk;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.tencent.av.sdk.AVRoomMulti;
import com.tencent.av.sdk.AVVideoCtrl;
import com.tencent.ilivesdk.ILiveCallBack;
import com.tencent.ilivesdk.ILiveConstants;
import com.tencent.ilivesdk.ILiveSDK;
import com.tencent.ilivesdk.adapter.CommonConstants;
import com.tencent.ilivesdk.core.ILiveLog;
import com.tencent.ilivesdk.core.ILiveLoginManager;
import com.tencent.ilivesdk.core.ILiveRoomConfig;
import com.tencent.ilivesdk.core.ILiveRoomManager;
import com.tencent.ilivesdk.core.ILiveRoomOption;
import com.tencent.ilivesdk.data.ILiveTextLabel;
import com.tencent.ilivesdk.data.msg.ILiveCustomMessage;
import com.tencent.ilivesdk.data.msg.ILiveTextMessage;
import com.tencent.ilivesdk.tools.ILiveSpeedTest;
import com.tencent.ilivesdk.tools.quality.ILiveQualityData;
import com.tencent.ilivesdk.tools.quality.LiveInfo;
import com.tencent.ilivesdk.view.AVRootView;
import com.tencent.ilivesdk.view.AVVideoView;
import com.tencent.ilivesdk.view.VideoListener;
import com.tencent.liteav.beauty.TXCVideoPreprocessor;
import com.tencent.qcloud.videocall.R;
import com.tencent.qcloud.videocall.bussiness.model.BussinessConstants;
import com.tencent.qcloud.videocall.bussiness.model.UserInfo;
import com.tencent.qcloud.videocall.trtcsdk.model.RoomTipsInfo;
import com.tencent.qcloud.videocall.trtcsdk.view.LoginView;
import com.tencent.qcloud.videocall.trtcsdk.view.OfflineView;
import com.tencent.qcloud.videocall.trtcsdk.view.RoomView;
import com.tencent.qcloud.videocall.trtcsdk.view.SpeedTestView;
import com.tencent.qcloud.videocall.trtcsdk.view.TipsView;
import com.tencent.qcloud.videocall.ui.Constants;
import com.tencent.qcloud.videocall.ui.utils.DlgMgr;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;


/**
 * 实时音视频SDK通讯模块
 */
public class SDKHelper implements ILiveLoginManager.TILVBStatusListener,
        ILiveRoomOption.onExceptionListener, ILiveRoomOption.onRoomDisconnectListener {
    private final String TAG = "SDKHelper";
    private LinkedList<LoginView> loginViews = new LinkedList<>();
    private LinkedList<OfflineView> offlineViews = new LinkedList<>();
    private LinkedList<RoomView> roomViews = new LinkedList<>();
    private LinkedList<TipsView> tipsViews = new LinkedList<>();
    private LinkedList<SpeedTestView> stViews = new LinkedList<>();

    private int statusTopFix = 0;
    private int infoColor = Color.WHITE;
    private boolean bInfoShow = false;
    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    TXCVideoPreprocessor mTxcFilter;

    private static SDKHelper instance;

    private class VideoViewHolder {
        int name_id;
        int info_id;
    }

    private Runnable infoRun = new Runnable() {
        @Override
        public void run() {
            if (!bInfoShow)
                return;
            ILiveQualityData qualityData = ILiveRoomManager.getInstance().getQualityData();
            if (null != qualityData) {
                RoomTipsInfo tipInfo = new RoomTipsInfo(((AVRoomMulti) ILiveSDK.getInstance().getContextEngine().getRoomObj()).getQualityTips());
                String info = "发送速率:\t" + qualityData.getSendKbps() + "kbps\t"
                        + "丢包率:\t" + qualityData.getSendLossRate() / 100 + "%\n"
                        + "接收速率:\t" + qualityData.getRecvKbps() + "kbps\t"
                        + "丢包率:\t" + qualityData.getRecvLossRate() / 100 + "%\n"
                        + "应用CPU:\t" + qualityData.getAppCPURate() + "%\t"
                        + "系统CPU:\t" + qualityData.getSysCPURate() + "%\n";

                AVRootView avRootView = ILiveRoomManager.getInstance().getRoomView();
                if (null == avRootView)return;
                for (Map.Entry<String, LiveInfo> entry : qualityData.getLives().entrySet()) {
                    int index = avRootView.findUserViewIndex(entry.getKey(), CommonConstants.Const_VideoType_Camera);
                    AVVideoView videoView = avRootView.getViewByIndex(index);
                    if (null != videoView) {
                        int topFix = 0;
                        String size = entry.getValue().getWidth() + "x" + entry.getValue().getHeight();
                        if (0 == index){
                            topFix += statusTopFix;
                        }
                        if (!videoView.getIdentifier().equals(ILiveLoginManager.getInstance().getMyUserId())){
                            if (tipInfo.getStreamMap().containsKey(entry.getKey())){
                                size += " "+tipInfo.getStreamMap().get(entry.getKey()).fps+"fps";
                            }
                        }
                        VideoViewHolder holder;
                        if (null == videoView.getTag()) {
                            holder = new VideoViewHolder();
                            videoView.setTag(holder);
                        } else {
                            holder = (VideoViewHolder) videoView.getTag();
                            videoView.removeLable(holder.name_id);
                            videoView.removeLable(holder.info_id);
                            ILiveLog.ki(TAG, "removeLabel["+index+"]: "+holder.name_id+","+holder.info_id+"---@"+videoView.getIdentifier());
                        }

                        ILiveTextLabel infolabel = new ILiveTextLabel(size);
                        holder.info_id = videoView.addLabelText(infolabel.setPostionMode(ILiveTextLabel.TextPositionMode.POSITION_CUSTOM)
                                .setX(0).setY(topFix)
                                .setTextSize(35)
                                .setTextColor(infoColor));
                        ILiveTextLabel namelabel = new ILiveTextLabel(entry.getKey());
                        holder.name_id = videoView.addLabelText(namelabel.setPostionMode(ILiveTextLabel.TextPositionMode.POSITION_CUSTOM)
                                .setTextSize(25)
                                .setX(0).setY(topFix+40)
                                .setTextColor(infoColor));
                        ILiveLog.ki(TAG, "addLabelText["+index+"]: "+holder.name_id+","+holder.info_id+"---@"+videoView.getIdentifier());
                    }
                }

                info += "角色: "+tipInfo.getCurRole() + "\n";
                info += "SDKAPPID: " + ILiveSDK.getInstance().getAppId() + "\nVersion:" + ILiveSDK.getInstance().getVersion();
                notifyTipsInfo(info);
            }
            ILiveSDK.getInstance().runOnMainThread(this, 2000);
        }
    };

    public static synchronized SDKHelper getInstance() {
        if (instance == null) {
            instance = new SDKHelper();
        }
        return instance;
    }

    /** 初始化SDK */
    public void initTrtcSDK(Context context, int sdkAppid, int accountType){
        ILiveSDK.getInstance().initSdk(context, sdkAppid, accountType);
        /** 初始化房间模块，并设置回调到消息观察者 */
        ILiveRoomManager.getInstance().init(new ILiveRoomConfig()
                .setRoomMsgListener(MessageObservable.getInstance()));
        /** 设置帐号被踢监听 */
        ILiveLoginManager.getInstance().setUserStatusListener(this);
    }

    /** 登录SDK */
    public void loginTrtcSDK(final String identifer, String userSig){
        ILiveLoginManager.getInstance().iLiveLogin(identifer, userSig, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                notifyLoginSuccess(identifer);
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                notifyLoginFailed(module, errCode, errMsg);
            }
        });
    }

    /** 设置渲染控件 */
    public void setTrtcRenderView(final AVRootView avRootView){
        bInfoShow = false;
        ILiveRoomManager.getInstance().initAvRootView(avRootView);
        avRootView.setLocalFullScreen(false);
        avRootView.setSubMarginY(80);
        avRootView.setGravity(AVRootView.LAYOUT_GRAVITY_RIGHT);
        avRootView.setBackground(R.mipmap.com_bg);
        avRootView.setSubCreatedListener(new AVRootView.onSubViewCreatedListener() {
            @Override
            public void onSubViewCreated() {
                for (int i=1; i<ILiveConstants.MAX_AV_VIDEO_NUM; i++){
                    final int index = i;
                    final AVVideoView videoView = avRootView.getViewByIndex(i);
                    videoView.setDragable(true);
                    videoView.setGestureListener(new GestureDetector.SimpleOnGestureListener(){
                        @Override
                        public boolean onSingleTapConfirmed(MotionEvent e) {
                            avRootView.swapVideoView(0, index);
                            return super.onSingleTapConfirmed(e);
                        }
                    });
                }
            }
        });

        mTxcFilter = new TXCVideoPreprocessor(avRootView.getContext(), false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {

            mTxcFilter.setBeautyStyle(0);           // 设置美颜风格，0: 光滑 1: 自然 2: 朦胧
            mTxcFilter.setBeautyLevel(5);           // 设置美颜级别,范围 0～10
            mTxcFilter.setWhitenessLevel(3);        // 设置美白级别,范围 0～10
            mTxcFilter.setRuddyLevel(2);            // 设置红润级别，范围 0～10
        }
    }

    /** 进入房间 */
    public void enterTrtcRoom(final int roomid, final String privateMapKey){
        ILiveRoomOption option = new ILiveRoomOption()
                .exceptionListener(this)
                .privateMapKey(privateMapKey)
                .roomDisconnectListener(this)
                .controlRole(Constants.DEF_ROLE);
        ILiveCallBack callBack = new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                notifyEnterRoomSuccess();
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                if (module.equals(ILiveConstants.Module_IMSDK) && (10010 == errCode || 10015 == errCode)){
                    mMainHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            createRoom(roomid, privateMapKey);
                        }
                    }, 1000);
                }else {
                    notifyEnterRoomFailed(module, errCode, errMsg);
                }
            }
        };
        int ret = ILiveRoomManager.getInstance().joinRoom(roomid, option, callBack);
        if (ILiveConstants.NO_ERR != ret){
            notifyEnterRoomFailed(ILiveConstants.Module_ILIVESDK, ret, "enter room failed");
        }
    }

    /** 退出房间 */
    public void exitTrtcRoom(){
        ILiveRoomManager.getInstance().quitRoom(new ILiveCallBack() {
            @Override
            public void onSuccess(Object o) {
                ((AVVideoCtrl)ILiveSDK.getInstance().getVideoEngine().getVideoObj()).setAfterPreviewListener(null);
                notifyRoomExit();
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                ILiveLog.ki(TAG, "exitRoom->"+module+"|"+errCode+"|"+errMsg);
                notifyRoomExit();
            }
        });
    }

    /** 开始测速 */
    public void startSpeedTest(){
        ILiveSpeedTest.getInstance().startSpeedTest(new ILiveSpeedTest.SpeedTestDelegate() {
            @Override
            public void onSpeedTestAccessPoint(int index, int total, String ip, double upLostRate, double downLostRate, int rtt) {
                notifySpeedTestInfo("["+index+"/"+total+"] "+ ip+" 上行:"+upLostRate+", 下行:"+downLostRate+", 延时:"+rtt);
            }

            @Override
            public void onSpeedTestResult(int errCode, String result) {
                if (0 != errCode)
                    notifySpeedTestInfo("测速结果:"+errCode+"|"+result);
                else{
                    notifySpeedTestInfo("测速结束");
                }
            }
        });
        notifySpeedTestInfo("开始测速...");
    }

    /** 设置状态栏修正高度(沉浸式) */
    public void setFixStatusHeigth(int height){
        statusTopFix = height;
    }

    /** 设置日志展示颜色 */
    public void setTipsInfoColor(int color){
        infoColor = color;
    }

    /** 开关调试信息 */
    public void enableTipsInfo(boolean enable){
        bInfoShow = enable;
        mMainHandler.removeCallbacks(infoRun);
        if (bInfoShow) {
            mMainHandler.postDelayed(infoRun, 0);
        }else{
            AVRootView avRootView = ILiveRoomManager.getInstance().getRoomView();
            for (int i = 0; i < ILiveConstants.MAX_AV_VIDEO_NUM; i++) {
                AVVideoView videoView = avRootView.getViewByIndex(i);
                if (null != videoView && null != videoView.getTag()) {
                    int result = 0;
                    VideoViewHolder holder = (VideoViewHolder) videoView.getTag();
                    result += (videoView.removeLable(holder.name_id) ? 0 : 1);
                    result += (videoView.removeLable(holder.info_id) ? 0 : 1);
                    ILiveLog.ki(TAG, "removeLabel["+result+"]: "+holder.name_id+","+holder.info_id+"---@"+videoView.getIdentifier());
                }
            }
        }
    }

    /** 切换摄像头 */
    public void switchCamera(boolean enable){
        ILiveRoomManager.getInstance().switchCamera(enable ? ILiveConstants.BACK_CAMERA : ILiveConstants.FRONT_CAMERA);
    }

    /** 控制麦克风 */
    public void enableMic(boolean enable){
        ILiveRoomManager.getInstance().enableMic(enable);
    }

    /** 控制美颜 */
    public void enableBeauty(boolean enable){
        if (enable) {
            ((AVVideoCtrl)ILiveSDK.getInstance().getVideoEngine().getVideoObj()).setAfterPreviewListener(new AVVideoCtrl.AfterPreviewListener(){
                @Override
                public void onFrameReceive(AVVideoCtrl.VideoFrame var1) {
                    // 回调的数据，传递给 ilivefilter processFrame 接口处理;
                    if (null != mTxcFilter && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        mTxcFilter.processFrame(var1.data, var1.width, var1.height, var1.rotate, var1.videoFormat, var1.videoFormat);
                    }
                }
            });
        }else{
            ((AVVideoCtrl)ILiveSDK.getInstance().getVideoEngine().getVideoObj()).setAfterPreviewListener(null);
        }
    }

    /** 切换分辨率 */
    public void changeRole(String role){
        ILiveRoomManager.getInstance().changeRole(role, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                notifyChangeRoleSuccess();
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                notifyChangeRoleFailed(module, errCode, errMsg);
            }
        });
    }

    /** 反馈问题 */
    public void uploadProblem(final String info) {
        ILiveSDK.getInstance().uploadLog(info, 0, new ILiveCallBack<String>() {
            @Override
            public void onSuccess(String s) {
                notifyFeedBackResult(ILiveConstants.Module_ILIVESDK, 0, "");
                ILiveLog.ki(TAG, "uploadLog->success: " + info);
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                notifyFeedBackResult(module, errCode, errMsg);
                ILiveLog.ki(TAG, "uploadLog->failed: " + module + "|" + errCode + "|" + errMsg);
            }
        });
    }

    /** 发送消息 */
    public void sendGroupMessage(final String text){
        ILiveCustomMessage customMessage = new ILiveCustomMessage(text.getBytes(), getSendDesc());
        customMessage.setExts(Constants.EXT_TEXT.getBytes());
        ILiveRoomManager.getInstance().sendGroupMessage(customMessage, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                ILiveTextMessage textMessage = new ILiveTextMessage(text);
                textMessage.setSender(UserInfo.getInstance().getNickName());
                notifySendMessageSuccess(textMessage);
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                notifySendMessageFailed(module, errCode, errMsg);
            }
        });
    }

    /** 销毁房间数据 */
    public void onDestoryRoom(){
        ILiveRoomManager.getInstance().onDestory();

        // 退出房间后，一定要销毁filter 资源；否则下次进入房间，setFilter将不生效或其他异常
        if (null != mTxcFilter) {
            mTxcFilter.release();
            mTxcFilter = null;
        }
    }

    @Override
    public void onForceOffline(int errCode, String strMsg) {
        notifyOffline(errCode, strMsg);
    }

    @Override
    public void onException(int event, int errCode, String errMsg) {
        notifyRoomException(event, errCode, errMsg);
    }

    @Override
    public void onRoomDisconnect(int errCode, String errMsg) {
        ((AVVideoCtrl)ILiveSDK.getInstance().getVideoEngine().getVideoObj()).setAfterPreviewListener(null);
        notifyRoomDisconnected(ILiveConstants.Module_AVSDK, errCode, errMsg);
    }

    public void addLoginView(LoginView view){
        if (!loginViews.contains(view))
            loginViews.add(view);
    }

    public void removeLoginView(LoginView view){
        loginViews.remove(view);
    }

    public void notifyLoginSuccess(String identifer){
        ArrayList<LoginView> views = new ArrayList<>(loginViews);
        for (LoginView view : views){
            view.onLoginSuccess(identifer);
        }
    }

    public void notifyLoginFailed(String module, int errCode, String errMsg){
        ArrayList<LoginView> views = new ArrayList<>(loginViews);
        for (LoginView view : views){
            view.onLoginFailed(module, errCode, errMsg);
        }
    }

    public void addOfflineView(OfflineView view){
        if (!offlineViews.contains(view))
            offlineViews.add(view);
    }

    public void removeOfflineView(OfflineView view){
        offlineViews.remove(view);
    }

    public void notifyOffline(int errCode, String errMsg){
        ArrayList<OfflineView> views = new ArrayList<>(offlineViews);
        for (OfflineView view : views){
            view.onOffline(errCode, errMsg);
        }
    }

    public void addRoomView(RoomView view){
        if (!roomViews.contains(view))
            roomViews.add(view);
    }

    public void removeRoomView(RoomView view){
        roomViews.remove(view);
    }

    public void notifyEnterRoomSuccess(){
        ArrayList<RoomView> views = new ArrayList<>(roomViews);
        for (RoomView view : views){
            view.onRoomEnter();
        }
    }

    public void notifyEnterRoomFailed(String module, int errCode, String errMsg){
        ArrayList<RoomView> views = new ArrayList<>(roomViews);
        for (RoomView view : views){
            view.onEnterRoomFailed(module, errCode, errMsg);
        }
    }

    public void notifyRoomDisconnected(String module, int errCode, String errMsg){
        ArrayList<RoomView> views = new ArrayList<>(roomViews);
        for (RoomView view : views){
            view.onRoomDisconnected(module, errCode, errMsg);
        }
    }

    public void notifyRoomException(int event, int errCode, String errMsg){
        ArrayList<RoomView> views = new ArrayList<>(roomViews);
        for (RoomView view : views){
            view.onRoomException(event, errCode, errMsg);
        }
    }

    public void notifyRoomExit(){
        ArrayList<RoomView> views = new ArrayList<>(roomViews);
        for (RoomView view : views){
            view.onRoomExit();
        }
    }

    public void notifyChangeRoleSuccess(){
        ArrayList<RoomView> views = new ArrayList<>(roomViews);
        for (RoomView view : views){
            view.onChangeRoleSuccess();
        }
    }

    public void notifyChangeRoleFailed(String module, int errCode, String errMsg){
        ArrayList<RoomView> views = new ArrayList<>(roomViews);
        for (RoomView view : views){
            view.onChangeRoleFailed(module, errCode, errMsg);
        }
    }

    public void notifyFeedBackResult(String module, int errCode, String errMsg){
        ArrayList<RoomView> views = new ArrayList<>(roomViews);
        for (RoomView view : views){
            view.onFeedBackResult(module, errCode, errMsg);
        }
    }

    public void notifySendMessageSuccess(ILiveTextMessage message){
        ArrayList<RoomView> views = new ArrayList<>(roomViews);
        for (RoomView view : views){
            view.onSendMessageSuccess(message);
        }
    }

    public void notifySendMessageFailed(String module, int errCode, String errMsg){
        ArrayList<RoomView> views = new ArrayList<>(roomViews);
        for (RoomView view : views){
            view.onSendMessageFailed(module, errCode, errMsg);
        }
    }

    public void addTipsView(TipsView view){
        if (!tipsViews.contains(view))
            tipsViews.add(view);
    }

    public void removeTipsView(TipsView view){
        tipsViews.remove(view);
    }

    public void notifyTipsInfo(String info){
        ArrayList<TipsView> views = new ArrayList<>(tipsViews);
        for (TipsView view : views){
            view.onTipsInfo(info);
        }
    }

    public void addSpeedTestView(SpeedTestView view){
        if (!stViews.contains(view))
            stViews.add(view);
    }

    public void removeSpeedTestView(SpeedTestView view){
        stViews.remove(view);
    }

    public void notifySpeedTestInfo(String info){
        ArrayList<SpeedTestView> views = new ArrayList<>(stViews);
        for (SpeedTestView view : views){
            view.appendSpeedTestInfo(info);
        }
    }

    private String getSendDesc() {
        return String.format(Locale.CHINA, "{\"%s\":\"%s\"}", BussinessConstants.JSON_NICKNAME, UserInfo.getInstance().getNickName());
    }

    private void createRoom(int roomid, String privateMapKey){
        ILiveRoomOption option = new ILiveRoomOption()
                .privateMapKey(privateMapKey)
                .exceptionListener(this)
                .roomDisconnectListener(this)
                .controlRole(Constants.DEF_ROLE);
        ILiveCallBack callBack = new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                notifyEnterRoomSuccess();
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                notifyEnterRoomFailed(module, errCode, errMsg);
            }
        };
        int ret = ILiveRoomManager.getInstance().createRoom(roomid, option, callBack);
        if (ILiveConstants.NO_ERR != ret){
            notifyEnterRoomFailed(ILiveConstants.Module_ILIVESDK, ret, "enter room failed");
        }
    }
}
