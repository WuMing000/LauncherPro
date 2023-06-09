package com.js.launcher;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.js.launcher.service.GuardService;
import com.js.launcher.service.MusicNotificationListenerService;
import com.js.launcher.service.MyService;

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
        // 运行后台，用于监听应用安装卸载和音乐变化
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, MyService.class));
            startForegroundService(new Intent(this, GuardService.class));
        } else {
            startService(new Intent(this, MyService.class));
            startService(new Intent(this, GuardService.class));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            startService(new Intent(this, MusicNotificationListenerService.class));
        }
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
