package com.tencent.qcloud.videocall.ui.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

/**
 * Created by tencent on 2018/6/12.
 */
public class MsgListView  extends ListView{
    public MsgListView(Context context) {
        super(context);
    }

    public MsgListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MsgListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /** 点击穿透 */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return false;
    }
}
