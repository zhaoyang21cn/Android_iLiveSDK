package com.tencent.qcloud.videocall.ui.adapter;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tencent.ilivesdk.data.ILiveMessage;
import com.tencent.ilivesdk.data.msg.ILiveTextMessage;
import com.tencent.qcloud.videocall.R;

import java.util.List;

/**
 * Created by xkazerzhang on 2017/6/22.
 */
public class ChatMsgAdapter extends BaseAdapter {
    private Context context;
    private List<ILiveMessage> chatMsgs;

    private class ViewHolder{
        TextView tvMsgInfo;
    }


    public ChatMsgAdapter(Context ctx, List<ILiveMessage> list){
        context = ctx;
        chatMsgs = list;
    }

    @Override
    public int getCount() {
        return chatMsgs.size();
    }

    @Override
    public Object getItem(int i) {
        return chatMsgs.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView != null) {
            holder = (ViewHolder)convertView.getTag();
        } else {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_msg, null);

            holder = new ViewHolder();
            holder.tvMsgInfo = (TextView) convertView.findViewById(R.id.tv_chat_msg);

            convertView.setTag(holder);
        }

        ILiveMessage info = chatMsgs.get(position);
        if (null != info){
            switch (info.getMsgType()){
                case ILiveMessage.ILIVE_MSG_TYPE_TEXT:
                    SpannableString spanString = new SpannableString(info.getSender() + "  " + ((ILiveTextMessage)info).getText());
                    spanString.setSpan(new ForegroundColorSpan(calcNameColor(info.getSender())),
                            0, info.getSender().length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                    holder.tvMsgInfo.setTextColor(context.getResources().getColor(R.color.colorWhite));
                    holder.tvMsgInfo.setText(spanString);
                    break;
            }
        }

        return convertView;
    }

    /**
     * 通过名称计算颜色
     */
    private int calcNameColor(String strName) {
        if (strName == null) return 0;
        byte idx = 0;
        byte[] byteArr = strName.getBytes();
        for (int i = 0; i < byteArr.length; i++) {
            idx ^= byteArr[i];
        }

        switch (idx & 0x7) {
            case 1:
                return context.getResources().getColor(R.color.colorSendName1);
            case 2:
                return context.getResources().getColor(R.color.colorSendName2);
            case 3:
                return context.getResources().getColor(R.color.colorSendName3);
            case 4:
                return context.getResources().getColor(R.color.colorSendName4);
            case 5:
                return context.getResources().getColor(R.color.colorSendName5);
            case 6:
                return context.getResources().getColor(R.color.colorSendName6);
            case 7:
                return context.getResources().getColor(R.color.colorSendName7);
            case 0:
            default:
                return context.getResources().getColor(R.color.colorSendName);
        }
    }
}
