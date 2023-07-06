package com.js.launcher.service;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.media.RemoteController;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.service.notification.NotificationListenerService;
import android.util.Log;

import com.js.launcher.manager.HandlerManager;
import com.js.launcher.utils.CustomUtil;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
@SuppressLint("OverrideAbstract")
public class MusicNotificationListenerService extends NotificationListenerService implements RemoteController.OnClientUpdateListener {

    private String trackName;
    private String artistName;
    private String albumArtistName;
    private String albumName;
    private Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void dispatchMessage(@NonNull Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what) {
                case 0x001:
                    handler.postDelayed(musicRunnable, 500);
                    break;
                case 0x002:
                    handler.postDelayed(controlMusic, 500);
                    break;
            }
        }
    };

    public MusicNotificationListenerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("MY_TAG", "MusicNotificationListenerService onCreate");
//        initNotify("MediaController", "MusicNotificationListenerService");
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        Log.i("MY_TAG", "MusicNotificationListenerService onStartCommand");
        Log.e("MY_TAG", CustomUtil.isNotificationListenerEnabled(this) + "");
        if (CustomUtil.isNotificationListenerEnabled(this)) {//开启通知使用权后再设置,否则会报权限错误
            initMediaSessionManager();
            registerRemoteController();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                startService(new Intent(this, MusicNotificationListenerService.class));
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("MY_TAG", "MusicNotificationListenerService onDestroy");
    }

    Runnable musicRunnable = new Runnable() {
        @Override
        public void run() {
            Handler musicHandler = HandlerManager.getHandler();
            Message message = new Message();
            message.what = HandlerManager.MUSIC_INFORMATION_UPDATE;
            Bundle bundle = new Bundle();
            bundle.putString("trackName", trackName);
            bundle.putString("artist", artistName);
            bundle.putString("albumName", albumName);
            message.obj = bundle;
            musicHandler.sendMessageAtTime(message, 100);
        }
    };

    Runnable controlMusic = new Runnable() {
        @Override
        public void run() {
            Handler controlHandler = HandlerManager.getHandler();
            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            if (audioManager.isMusicActive()) {
                controlHandler.sendEmptyMessageAtTime(HandlerManager.MUSIC_PAUSE_UI, 100);
            } else {
                controlHandler.sendEmptyMessageAtTime(HandlerManager.MUSIC_PLAY_UI, 100);
            }
        }
    };

    //////////////////////////////////MediaController获取音乐信息/////////////////////////////////////
    private List<MediaController> mActiveSessions;
    private MediaController.Callback mSessionCallback;
    private void initMediaSessionManager() {
        MediaSessionManager mediaSessionManager = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediaSessionManager = (MediaSessionManager) getSystemService(MEDIA_SESSION_SERVICE);
        }
        ComponentName localComponentName = new ComponentName(this, MusicNotificationListenerService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediaSessionManager.addOnActiveSessionsChangedListener(new MediaSessionManager.OnActiveSessionsChangedListener() {
                @Override
                public void onActiveSessionsChanged(@Nullable final List<MediaController> controllers) {
                    if (controllers != null) {
                        for (MediaController mediaController : controllers) {
                            String packageName = mediaController.getPackageName();
                            Log.e("MY_TAG", "MyApplication onActiveSessionsChanged mediaController.getPackageName: " + packageName);
                            synchronized (this) {
                                mActiveSessions = controllers;
                                registerSessionCallbacks();
                            }
                        }
                    }
                }
            }, localComponentName);
        }
        synchronized (this) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mActiveSessions = mediaSessionManager.getActiveSessions(localComponentName);
            }
            registerSessionCallbacks();
        }
    }

    private void registerSessionCallbacks() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            for (MediaController controller : mActiveSessions) {
                if (mSessionCallback == null) {
                    mSessionCallback = new MediaController.Callback() {
                        @Override
                        public void onMetadataChanged(MediaMetadata metadata) {
                            if (metadata != null) {
                                trackName = metadata.getString(MediaMetadata.METADATA_KEY_TITLE);
                                artistName = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST);
                                albumArtistName = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST);
                                albumName = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM);
                                Log.i("MY_TAG", "---------------------------------");
                                Log.i("MY_TAG", "| trackName: " + trackName);
                                Log.i("MY_TAG", "| artistName: " + artistName);
                                Log.i("MY_TAG", "| albumArtistName: " + albumArtistName);
                                Log.i("MY_TAG", "| albumName: " + albumName);
                                Log.i("MY_TAG", "---------------------------------");
                                handler.sendEmptyMessageAtTime(0x001, 100);

                            }
                        }

                        @Override
                        public void onAudioInfoChanged(MediaController.PlaybackInfo info) {
                            super.onAudioInfoChanged(info);
                        }

                        @Override
                        public void onPlaybackStateChanged(PlaybackState state) {
                            super.onPlaybackStateChanged(state);
//                            if(state != null){
//                                boolean isPlaying = state.getState() == PlaybackState.STATE_PLAYING;
//                                Log.e("MY_TAG", "MediaController.Callback onPlaybackStateChanged isPlaying: " + audioManager.isMusicActive());
                                handler.sendEmptyMessageAtTime(0x002, 100);
//                            }
                        }
                    };
                }
                controller.registerCallback(mSessionCallback);
            }
        }
    }

    //////////////////////////////////RemoteController获取音乐信息/////////////////////////////////////
    public RemoteController remoteController;
    public void registerRemoteController() {
        remoteController = new RemoteController(this, this);
        boolean registered;
        try {
            registered = ((AudioManager) getSystemService(AUDIO_SERVICE)).registerRemoteController(remoteController);
        } catch (NullPointerException e) {
            registered = false;
        }
        if (registered) {
            try {
                remoteController.setArtworkConfiguration(100,100);
                remoteController.setSynchronizationMode(RemoteController.POSITION_SYNCHRONIZATION_CHECK);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClientChange(final boolean clearing) {
        Log.d("MY_TAG", "clearing == " + clearing);
    }

    @Override
    public void onClientPlaybackStateUpdate(final int state) {
        Log.d("MY_TAG", "state1 == " + state);
    }

    @Override
    public void onClientPlaybackStateUpdate(final int state, final long stateChangeTimeMs, final long currentPosMs, final float speed) {
        Log.i("MY_TAG", "state2 == " + state + "stateChangeTimeMs == " + stateChangeTimeMs + "currentPosMs == " + currentPosMs + "speed == " + speed);
    }

    @Override
    public void onClientTransportControlUpdate(final int transportControlFlags) {
        Log.d("MY_TAG", "transportControlFlags == " + transportControlFlags);
    }

    @Override
    public void onClientMetadataUpdate(final RemoteController.MetadataEditor metadataEditor) {
        String artist = metadataEditor.
                getString(MediaMetadataRetriever.METADATA_KEY_ARTIST, "null");
        String album = metadataEditor.
                getString(MediaMetadataRetriever.METADATA_KEY_ALBUM, "null");
        String title = metadataEditor.
                getString(MediaMetadataRetriever.METADATA_KEY_TITLE, "null");
        Long duration = metadataEditor.
                getLong(MediaMetadataRetriever.METADATA_KEY_DURATION, -1);
        Bitmap defaultCover = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_menu_compass);
        Bitmap bitmap = metadataEditor.
                getBitmap(RemoteController.MetadataEditor.BITMAP_KEY_ARTWORK, defaultCover);
        Log.e("MY_TAG", "artist:" + artist + ", album:" + album + ", title:" + title + ", duration:" + duration);
    }

    /**
     * 添加一个常驻通知
     * @param title
     * @param context
     */
//    public void initNotify(String title, String context) {
//        String CHANNEL_ONE_ID = "1000";
//
//        Drawable drawable = null;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            drawable = getResources().getDrawable(R.mipmap.ic_launcher, null);
//        }
//        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
//        //Bitmap bitmapIcon = BitmapUtils.getBitmapFromDrawable(drawable);
//
//        Intent nfIntent = new Intent(this, MainActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, nfIntent, 0);
//        @SuppressLint("WrongConstant") NotificationCompat.Builder builder = new NotificationCompat.Builder(this.getApplicationContext(), CHANNEL_ONE_ID)
//                .setContentIntent(pendingIntent) // 设置PendingIntent
//                .setSmallIcon(R.mipmap.ic_launcher) // 设置状态栏内的小图标
//                //.setLargeIcon(bitmapIcon)// 设置大图标
//                .setContentTitle(title)
//                .setContentText(context) // 设置上下文内容
//                .setWhen(System.currentTimeMillis())// 设置该通知发生的时间
//                .setVisibility(VISIBILITY_PUBLIC)// 锁屏显示全部通知
//                //.setDefaults(Notification.DEFAULT_ALL)// //使用默认的声音、振动、闪光
//                .setPriority(PRIORITY_HIGH);// 通知的优先级
//
//        //----------------  新增代码 ------------------------
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            //修改安卓8.1以上系统报错
//            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ONE_ID, "app_service_notify", NotificationManager.IMPORTANCE_MIN);
//            notificationChannel.enableLights(false);//如果使用中的设备支持通知灯，则说明此通知通道是否应显示灯
//            notificationChannel.setShowBadge(false);//是否显示角标
//            notificationChannel.enableVibration(false);//是否震动
//            notificationChannel.setLockscreenVisibility(VISIBILITY_PUBLIC);//锁屏显示全部通知
//            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//            manager.createNotificationChannel(notificationChannel);
//            builder.setChannelId(CHANNEL_ONE_ID);
//        }
//        Notification notification = builder.build(); // 获取构建好的Notification
//        notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音
//        startForeground(1, notification);
//    }

}