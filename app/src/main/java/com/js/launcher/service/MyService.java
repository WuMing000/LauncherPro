package com.js.launcher.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.js.launcher.R;
import com.js.launcher.aidl.ProcessConnection;
import com.js.launcher.manager.HandlerManager;
import com.js.launcher.recevier.MusicReceiver;
import com.js.launcher.recevier.MyInstalledReceiver;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import static android.app.NotificationManager.IMPORTANCE_MIN;

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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new ProcessConnection.Stub() {};
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        String channelId;
        // 8.0 以上需要特殊处理
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 静音通知
            channelId = createNotificationChannel();
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
            Notification notification = builder.setOngoing(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setPriority(IMPORTANCE_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
            startForeground(1, notification);
        } else {
            startForeground(1, new Notification());
        }
        //绑定建立链接
        bindService(new Intent(this, GuardService.class),
                mServiceConnection, Context.BIND_IMPORTANT);
        receive();
        return START_STICKY;
    }


    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //链接上
            Log.d("test","StepService:建立链接");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            //断开链接
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(new Intent(MyService.this,GuardService.class));
            } else {
                startService(new Intent(MyService.this,GuardService.class));
            }
            //重新绑定
            bindService(new Intent(MyService.this, GuardService.class),
                    mServiceConnection, Context.BIND_IMPORTANT);
        }
    };

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

    /**
     * 创建通知通道
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(){
        NotificationChannel chan = new NotificationChannel("kim.hsl",
                "ForegroundService", NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return "kim.hsl";
    }
}