package com.example.launchproject;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

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
