package com.tencent.qcloud.videocall.trtcsdk.model;

import android.util.Log;

import java.util.HashMap;

/**
 * Created by tencent on 2018/6/12.
 */
public class RoomTipsInfo {
    public class StreamInfo{
        public String identifer = "";   // 用户id
        public int width = 0;          // 视频宽
        public int heigth = 0;         // 视频高
        public int fps = 0;

        @Override
        public String toString() {
            return "StreamInfo{" +
                    "identifer='" + identifer + '\'' +
                    ", width=" + width +
                    ", heigth=" + heigth +
                    ", fps=" + fps +
                    '}';
        }
    }

    private HashMap<String, StreamInfo> streamMap;
    private String curRole = "";         // 角色
    private int captureWidth = 0;       // 采集宽度
    private int captureHeigth = 0;      // 采集高度
    private int captureFps = 0;         // 采集帧率

    public RoomTipsInfo(String tips){
        String sep = "[](),\n";
        streamMap = new HashMap<>();
        try {
            curRole = getValue(tips, "ControlRole", sep);
            String captureInfo = getValue(tips, "视频采集", "\n");

            captureWidth = Integer.valueOf(getValue(captureInfo, "W", sep));
            captureHeigth = Integer.valueOf(getValue(captureInfo, "H", sep));
            captureFps = Integer.valueOf(getValue(captureInfo, "FPS", sep))/10;

            int idx_begin = tips.indexOf("解码部分");
            int idx_end = tips.indexOf("接收参数");
            if (-1 != idx_begin && idx_end > idx_begin) {
                String info = tips.substring(idx_begin + 5, idx_end);
                String[] params = info.split("\n");
                for (String line : params) {
                    if (line.length() < 2)continue;
                    StreamInfo streamInfo = new StreamInfo();
                    streamInfo.identifer = getValue(line, "成员", " ");
                    streamInfo.width = Integer.valueOf(getValue(line, "W", sep));
                    streamInfo.heigth = Integer.valueOf(getValue(line, "H", sep));
                    streamInfo.fps = Integer.valueOf(getValue(line, "FPS", sep))/10;
                    streamMap.put(streamInfo.identifer, streamInfo);
                }
            }
        }catch (Exception e){
        }
    }

    private String getValue(String src, String param, String sep) {
        int idx = src.indexOf(param);
        if (-1 != idx) {
            idx += param.length() + 1;
            if (-1 != sep.indexOf(src.charAt(idx))) {
                idx++;
            }
            for (int i = idx; i < src.length(); i++) {
                if (-1 != sep.indexOf(src.charAt(i))) {
                    return src.substring(idx, i).trim();
                }
            }
            return src.substring(idx).trim();
        }

        return "";
    }

    public HashMap<String, StreamInfo> getStreamMap() {
        return streamMap;
    }

    public String getCurRole() {
        return curRole;
    }

    public int getCaptureWidth() {
        return captureWidth;
    }

    public int getCaptureHeigth() {
        return captureHeigth;
    }

    public int getCaptureFps() {
        return captureFps;
    }
}
