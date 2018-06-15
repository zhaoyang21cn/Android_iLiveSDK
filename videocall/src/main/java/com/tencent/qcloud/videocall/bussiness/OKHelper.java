package com.tencent.qcloud.videocall.bussiness;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.tencent.qcloud.videocall.bussiness.model.BussinessConstants;
import com.tencent.qcloud.videocall.bussiness.model.LoginInfo;
import com.tencent.qcloud.videocall.bussiness.view.SyncUserInfoView;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 业务服务器通讯模块
 * 与业务服务器通讯(获取登录信息、房间列表等)
 * 用户使用自己的业务服务器时可以直接移除bussiness模块
 */
public class OKHelper {
    private static final String TAG = "OKHelper";

    private OkHttpClient okHttpClient;
    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    private List<SyncUserInfoView> syncUserInfoViews = new LinkedList<>();

    private static OKHelper instance;


    private OKHelper(){
        okHttpClient = new OkHttpClient.Builder()
                .readTimeout(BussinessConstants.READ_TIMEOUT, TimeUnit.SECONDS)//设置读取超时时间
                .writeTimeout(BussinessConstants.WRITE_TIMEOUT, TimeUnit.SECONDS)//设置写的超时时间
                .connectTimeout(BussinessConstants.CONNECT_TIMEOUT, TimeUnit.SECONDS)//设置连接超时时间
                .build();
    }

    public static synchronized OKHelper getInstance() {
        if (instance == null) {
            instance = new OKHelper();
        }
        return instance;
    }

    public void addSyncInfoView(SyncUserInfoView view){
        if (!syncUserInfoViews.contains(view)){
            syncUserInfoViews.add(view);
        }
    }

    public void removeSyncInfoView(SyncUserInfoView view){
        syncUserInfoViews.remove(view);
    }

    /** 从业务服务器获取登录信息 */
    public void getLoginInfo(String userId){
        try {
            JSONObject jsonReq = new JSONObject();
            jsonReq.put(BussinessConstants.JSON_USERID, userId);
            RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonReq.toString());
            Request req = new Request.Builder()
                    .url(BussinessConstants.SERVER_PATH+"/get_login_info")
                    .post(body)
                    .build();
            Log.i(TAG, "getLoginInfo->url: "+req.url().toString());
            Log.i(TAG, "getLoginInfo->post: "+body.toString());
            okHttpClient.newCall(req).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    notifySyncFailed(1, e.toString());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()){
                        notifySyncFailed(response.code(), response.message());
                    }else{
                        parseLoginInfo(response.body().string());
                    }
                }
            });
        }catch (Exception e){
            notifySyncFailed(3, e.toString());
        }
    }


    private void notifySyncSuccess(final LoginInfo info){
        mMainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                for (SyncUserInfoView view : syncUserInfoViews){
                    view.onSyncSuccess(info);
                }
            }
        }, 0);
    }

    private void notifySyncFailed(final int errCode, final String errMsg){
        mMainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                for (SyncUserInfoView view : syncUserInfoViews){
                    view.onSyncFailed(errCode, errMsg);
                }
            }
        }, 0);
    }

    private void parseLoginInfo(String rsp){
        try {
            Log.i(TAG, "parseLoginInfo->rsp: "+rsp);
            JSONTokener jsonTokener = new JSONTokener(rsp);
            JSONObject msgJson = (JSONObject) jsonTokener.nextValue();
            int code = msgJson.getInt(BussinessConstants.JSON_CODE);
            if (0 != code){
                notifySyncFailed(code, msgJson.getString(BussinessConstants.JSON_MESSAGE));
            }else{
                notifySyncSuccess(new LoginInfo(msgJson));
            }
        }catch (Exception e){
            notifySyncFailed(2, e.toString());
        }
    }
}
