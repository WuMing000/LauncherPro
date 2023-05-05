package com.example.launchproject.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.example.launchproject.manager.HandlerManager;
import com.example.launchproject.recevier.MusicReceiver;
import com.example.launchproject.recevier.MyInstalledReceiver;

/** *
 * 后台监听广播
 * 应用安装和卸载、QQ音乐广播
 */
@SuppressLint("LongLogTag")
public class MyService extends Service {

    MyInstalledReceiver myInstalledReceiver;
    MusicReceiver musicReceiver;

    private static final String TAG = "MyService======>";

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate");
        receive();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
        unregisterReceiver(myInstalledReceiver);
        unregisterReceiver(musicReceiver);
    }

    private void receive(){

        //动态注册应用安装和卸载广播
        myInstalledReceiver = new MyInstalledReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addDataScheme("package");
        intentFilter.addAction("android.intent.action.PACKAGE_ADDED");
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        registerReceiver(myInstalledReceiver, intentFilter);

        musicReceiver = new MusicReceiver(new MusicReceiver.DataCallBack() {
            @Override
            public void onDataChanged(String trackName, String artist, String albumName) {
//                musicName = trackName + " - " + artist;
//                tvMusicName.setText(musicName);
//                editor = sp.edit();
//                editor.putString("musicName", musicName);
//                editor.commit();
                Handler handler = HandlerManager.getHandler();
                Message message = new Message();
                message.what = HandlerManager.MUSIC_INFORMATION_UPDATE;
                Bundle bundle = new Bundle();
                bundle.putString("trackName", trackName);
                bundle.putString("artist", artist);
                bundle.putString("albumName", albumName);
                message.obj = bundle;
                handler.sendMessageAtTime(message, 100);

            }
        });
        IntentFilter iF = new IntentFilter();
        iF.addAction("com.android.music.metachanged");
        registerReceiver(musicReceiver, iF);
    }
}