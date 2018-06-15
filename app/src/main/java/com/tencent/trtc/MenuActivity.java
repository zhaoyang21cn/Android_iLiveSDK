package com.tencent.trtc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.tencent.qcloud.videocall.ui.CreateActivity;
import com.tencent.trtc.adapter.FuncAdapter;
import com.tencent.trtc.model.FuncInfo;

import java.util.ArrayList;

/**
 * Created by tencent on 2018/6/13.
 */
public class MenuActivity extends Activity {
    private ListView lvFuncs;
    private FuncAdapter funcAdapter;
    private ArrayList<FuncInfo> funcList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_menu);
        lvFuncs = (ListView)findViewById(R.id.lv_menu);

        funcList = new ArrayList<>();

        /** 添加视频视频通话 */
        funcList.add(new FuncInfo(getString(R.string.func_videocall), CreateActivity.class));


        if (1 == funcList.size()){  // 只有一个功能时直接进入
            enterFunc(funcList.get(0));
            finish();
        }

        funcAdapter = new FuncAdapter(this, funcList);
        lvFuncs.setAdapter(funcAdapter);

        lvFuncs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FuncInfo info = funcList.get(position);
                enterFunc(info);
            }
        });
    }

    private void enterFunc(FuncInfo info){
        startActivity(new Intent(this, info.getCls()));
    }
}
