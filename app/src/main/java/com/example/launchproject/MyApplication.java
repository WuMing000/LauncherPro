package com.example.launchproject;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.example.launchproject.service.MyService;

@SuppressLint("StaticFieldLeak")
public class MyApplication extends Application {

    // 定义单态
    private static MyApplication singleton;

    private Context context;

    public static MyApplication getInstance(){
        return singleton;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 运行后台，用于监听应用安装卸载和音乐变化
        startService(new Intent(this, MyService.class));
        singleton = this;
        context = getApplicationContext();
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
