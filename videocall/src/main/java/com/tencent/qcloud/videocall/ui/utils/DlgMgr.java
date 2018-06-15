package com.tencent.qcloud.videocall.ui.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.qcloud.videocall.R;


/**
 * Created by xkazerzhang on 2016/12/28.
 */
public class DlgMgr {

    private static void customDemoDialg(Context context, AlertDialog dialog){
        try {
            // 修改样式
            Resources res = dialog.getContext().getResources();
            // 获取标题title
            TextView tvTitle = (TextView) dialog.findViewById(res.getIdentifier("alertTitle", "id", "android"));
            tvTitle.setTextColor(context.getResources().getColor(R.color.colorWhite));
            // 获取分隔线
            View divider = dialog.findViewById(res.getIdentifier("titleDivider", "id", "android"));
            divider.setBackgroundResource(R.color.colorWhite);
        }catch (Exception e){
        }
    }

    public static AlertDialog showAlertDlg(Context context, AlertDialog.Builder builder) {
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
        customDemoDialg(context, alertDialog);
        return alertDialog;
    }

    public static void dismessDlg(AlertDialog dialog) {
        if (null != dialog && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    // 显示提示信息
    public static AlertDialog showMsg(Context context, String msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.msg_title)
                .setMessage(msg);
        return showAlertDlg(context, builder);
    }

    public static AlertDialog showMsg(Context context, int res){
        return showMsg(context, context.getString(res));
    }

    // 显示toast
    public static void showToast(Context context, String msg){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
