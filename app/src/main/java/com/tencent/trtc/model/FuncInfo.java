package com.tencent.trtc.model;

/**
 * Created by tencent on 2018/6/13.
 */
public class FuncInfo {
    private String name;
    private Class cls;

    public FuncInfo(String name, Class cls) {
        this.name = name;
        this.cls = cls;
    }

    public String getName() {
        return name;
    }

    public Class getCls() {
        return cls;
    }
}
