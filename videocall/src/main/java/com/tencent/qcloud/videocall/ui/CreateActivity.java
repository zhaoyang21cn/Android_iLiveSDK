package com.tencent.qcloud.videocall.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.qcloud.videocall.R;
import com.tencent.qcloud.videocall.bussiness.OKHelper;
import com.tencent.qcloud.videocall.bussiness.model.LoginInfo;
import com.tencent.qcloud.videocall.bussiness.model.UserInfo;
import com.tencent.qcloud.videocall.bussiness.view.SyncUserInfoView;
import com.tencent.qcloud.videocall.trtcsdk.SDKHelper;
import com.tencent.qcloud.videocall.trtcsdk.view.LoginView;
import com.tencent.qcloud.videocall.trtcsdk.view.SpeedTestView;
import com.tencent.qcloud.videocall.ui.utils.DlgMgr;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tencent on 2018/5/21.
 */
public class CreateActivity extends Activity implements SyncUserInfoView, LoginView, SpeedTestView {
    private EditText etRoomId;
    private TextView tvCreate;

    private boolean bLogin = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_activity);

        etRoomId = (EditText)findViewById(R.id.et_room_name);
        tvCreate = (TextView)findViewById(R.id.tv_enter);

        tvCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bLogin) {
                    DlgMgr.showMsg(getContext(), "请先等待登录成功");
                    return;
                }
                int roomId = Integer.valueOf(etRoomId.getText().toString());
                UserInfo.getInstance().setCurRoomId(roomId);
                startActivity(new Intent(getContext(), RoomActivity.class));
            }
        });

        UserInfo.getInstance().getCache(this);

        SDKHelper.getInstance().addLoginView(this);
        SDKHelper.getInstance().addSpeedTestView(this);
        OKHelper.getInstance().addSyncInfoView(this);
        OKHelper.getInstance().getLoginInfo(UserInfo.getInstance().getUserId());

        checkPermission();
    }

    @Override
    protected void onDestroy() {
        SDKHelper.getInstance().removeSpeedTestView(this);
        SDKHelper.getInstance().removeLoginView(this);
        OKHelper.getInstance().removeSyncInfoView(this);
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.REQ_PERMISSION_CODE:
                for (int ret : grantResults) {
                    if (PackageManager.PERMISSION_GRANTED != ret) {
                        DlgMgr.showMsg(getContext(), "用户没有允许需要的权限，使用可能会受到限制！");
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onSyncLoginSuccess(LoginInfo info) {
        UserInfo.getInstance().setUserId(info.getUserId());
        UserInfo.getInstance().setNickName(info.getUserId());
        UserInfo.getInstance().writeToCache(this);

        SDKHelper.getInstance().initTrtcSDK(this, info.getSdkAppId(), info.getAccountType());
        SDKHelper.getInstance().loginTrtcSDK(info.getUserId(), info.getUserSig());
    }

    @Override
    public void onSyncLoginFailed(int errCode, String errInfo) {
        DlgMgr.showMsg(getContext(), "SyncFailed: " + errCode + "|" + errInfo);
    }

    @Override
    public void onLoginSuccess(String userId) {
        bLogin = true;
        SDKHelper.getInstance().startSpeedTest();
        DlgMgr.showToast(getContext(), getString(R.string.str_login_success));
    }

    @Override
    public void onLoginFailed(String module, int errCode, String errMsg) {
        Toast.makeText(getContext(), "login failed:" + module + "|" + errCode + "|" + errMsg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void appendSpeedTestInfo(final String tips) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tvSpeedInfo = (TextView)findViewById(R.id.tv_speed_test_info);
                if (null != tvSpeedInfo){
                    String oldTest = tvSpeedInfo.getText().toString();
                    oldTest += tips + "\n";
                    tvSpeedInfo.setText(oldTest);
                }
            }
        });
    }

    private Context getContext(){
        return this;
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissions = new ArrayList<>();
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)) {
                permissions.add(Manifest.permission.CAMERA);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO)) {
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_STATE)) {
                permissions.add(Manifest.permission.READ_PHONE_STATE);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (permissions.size() != 0) {
                ActivityCompat.requestPermissions(CreateActivity.this,
                        (String[]) permissions.toArray(new String[0]),
                        Constants.REQ_PERMISSION_CODE);
                return false;
            }
        }

        return true;
    }
}
