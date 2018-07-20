package com.tencent.qcloud.videocall.bussiness;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.tencent.qcloud.videocall.bussiness.model.BussinessConstants;
import com.tencent.qcloud.videocall.bussiness.model.PrivateMapKeyInfo;
import com.tencent.qcloud.videocall.bussiness.model.LoginInfo;
import com.tencent.qcloud.videocall.bussiness.view.SyncPrivateMapkeyView;
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
    private List<SyncPrivateMapkeyView> syncPrivateMapKeyViews = new LinkedList<>();

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

    public void addPrivateMapKeyView(SyncPrivateMapkeyView view){
        if (!syncPrivateMapKeyViews.contains(view)){
            syncPrivateMapKeyViews.add(view);
        }
    }

    public void removePrivateMapKeyView(SyncPrivateMapkeyView view){
        syncPrivateMapKeyViews.remove(view);
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
                    notifySyncLoginFailed(1, e.toString());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()){
                        notifySyncLoginFailed(response.code(), response.message());
                    }else{
                        parseLoginInfo(response.body().string());
                    }
                }
            });
        }catch (Exception e){
            notifySyncLoginFailed(3, e.toString());
        }
    }

    /** 从业务服务器获取privateMapKey信息 */
    public void getPrivateMapKey(String userId, int roomId){
        try {
            JSONObject jsonReq = new JSONObject();
            jsonReq.put(BussinessConstants.JSON_USERID, userId);
            jsonReq.put(BussinessConstants.JSON_ROOMID, roomId);
            RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonReq.toString());
            Request req = new Request.Builder()
                    .url(BussinessConstants.SERVER_PATH+"/get_privatemapkey")
                    .post(body)
                    .build();
            Log.i(TAG, "getPrivateMapKey->url: "+req.url().toString());
            Log.i(TAG, "getPrivateMapKey->post: "+body.toString());
            okHttpClient.newCall(req).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    notifySyncPrivateMapKeyFailed(1, e.toString());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()){
                        notifySyncPrivateMapKeyFailed(response.code(), response.message());
                    }else{
                        parseKeyInfo(response.body().string());
                    }
                }
            });
        }catch (Exception e){
            notifySyncPrivateMapKeyFailed(3, e.toString());
        }
    }

    private void notifySyncLoginSuccess(final LoginInfo info){
        mMainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                for (SyncUserInfoView view : syncUserInfoViews){
                    view.onSyncLoginSuccess(info);
                }
            }
        }, 0);
    }

    private void notifySyncLoginFailed(final int errCode, final String errMsg){
        mMainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                for (SyncPrivateMapkeyView view : syncPrivateMapKeyViews){
                    view.onSyncKeyFailed(errCode, errMsg);
                }
            }
        }, 0);
    }

    private void notifySyncPrivateMapKeySuccess(final PrivateMapKeyInfo info){
        mMainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                for (SyncPrivateMapkeyView view : syncPrivateMapKeyViews){
                    view.onSyncKeySuccess(info);
                }
            }
        }, 0);
    }

    private void notifySyncPrivateMapKeyFailed(final int errCode, final String errMsg){
        mMainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                for (SyncUserInfoView view : syncUserInfoViews){
                    view.onSyncLoginFailed(errCode, errMsg);
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
                notifySyncLoginFailed(code, msgJson.getString(BussinessConstants.JSON_MESSAGE));
            }else{
                notifySyncLoginSuccess(new LoginInfo(msgJson));
            }
        }catch (Exception e){
            notifySyncLoginFailed(2, e.toString());
        }
    }

    private void parseKeyInfo(String rsp){
        try {
            Log.i(TAG, "parseKeyInfo->rsp: "+rsp);
            JSONTokener jsonTokener = new JSONTokener(rsp);
            JSONObject msgJson = (JSONObject) jsonTokener.nextValue();
            int code = msgJson.getInt(BussinessConstants.JSON_CODE);
            if (0 != code){
                notifySyncPrivateMapKeyFailed(code, msgJson.getString(BussinessConstants.JSON_MESSAGE));
            }else{
                notifySyncPrivateMapKeySuccess(new PrivateMapKeyInfo(msgJson));
            }
        }catch (Exception e){
            notifySyncPrivateMapKeyFailed(2, e.toString());
        }
    }
}
