package com.example.launchproject.manager;

import android.os.Handler;

public class HandlerManager {
    public static final int GET_SYSTEM_TIME = 0;
    public static final int HOME_LONG_CLICK = 2;
    public static final int MUSIC_PLAY_UI = 3;
    public static final int MUSIC_PAUSE_UI = 4;
    public static final int REMOVED_APP_SUCCESS = 5;
    public static ThreadLocal<Handler> threadLocal = new ThreadLocal<Handler>();
 
    public static Handler getHandler() {
        return threadLocal.get();
    }
 
    public static void putHandler(Handler value) {
        threadLocal.set(value);//UiThread  id
    }
}