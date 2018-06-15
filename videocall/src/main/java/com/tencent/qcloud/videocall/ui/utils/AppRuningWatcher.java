package com.tencent.qcloud.videocall.ui.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;

import com.tencent.ilivesdk.ILiveSDK;
import com.tencent.qcloud.dr.TXDREventData;

/**
 * Created by valexhuang on 2018/6/6.
 */

public class AppRuningWatcher {
    private boolean initialized, mBackgroued;
    private long mAppStartTime, mAppBackgroudTime;
    private Context mContext;
    private String MY_APP_PACKAGE_NAME;
    private static AppRuningWatcher instance = new AppRuningWatcher();


    private AppRuningWatcher() {

    }

    public static AppRuningWatcher getInstance() {
        return instance;
    }

    public void init(Context context) {
        if (!initialized) {
            initialized = true;
            mContext = context;
            MY_APP_PACKAGE_NAME = mContext.getPackageName();
            startWatch();
        }
    }

    public void startWatch() {
        mAppStartTime = System.currentTimeMillis();
        mAppBackgroudTime = 0;
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
                    ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
                    String packageName = cn.getPackageName();
                    if (!MY_APP_PACKAGE_NAME.equals(packageName)) {
                        if (mAppBackgroudTime == 0)
                            mAppBackgroudTime = System.currentTimeMillis();
                        else if (System.currentTimeMillis() - mAppBackgroudTime > 10000 && !mBackgroued) {
                            mBackgroued = true;
                            ILiveSDK.getInstance().getReportEngine().reportEvent(new TXDREventData().eventId(1201).eventTime(mAppBackgroudTime - mAppStartTime));
                        }
                    } else {
                        //用户按home键退出后再切回的情况，重新记录使用时间
                        if (mBackgroued) {
                            mBackgroued = false;
                            mAppStartTime = System.currentTimeMillis();
                        }
                        mAppBackgroudTime = 0;
                    }
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }
}
