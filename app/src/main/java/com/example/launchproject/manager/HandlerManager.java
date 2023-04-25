package com.example.launchproject.manager;

import android.os.Handler;

public class HandlerManager {
    public static final int GET_SYSTEM_TIME = 0;
    public static final int GET_APP_LIST = 1;
    public static final int LOAD_APP_FINISH = 2;
    public static final int SHOW_APP_LIST = 3;
    public static final int HOME_LONG_CLICK = 4;
    public static final int MUSIC_PLAY_UI = 5;
    public static final int MUSIC_PAUSE_UI = 6;
    public static final int MUSIC_INFORMATION_UPDATE = 7;
    public static final int REMOVED_APP_SUCCESS = 8;
    public static final int INSTALL_APP_SUCCESS = 9;
    public static final int CLEAR_RECYCLER_ANIMATION = 10;
    public static final int SKIP_NEXT_PAGE = 11;
    public static final int SKIP_PREVIOUS_PAGE = 12;
    public static ThreadLocal<Handler> threadLocal = new ThreadLocal<Handler>();

    public static Handler getHandler() {
        return threadLocal.get();
    }
 
    public static void putHandler(Handler value) {
        threadLocal.set(value);//UiThread  id
    }
}