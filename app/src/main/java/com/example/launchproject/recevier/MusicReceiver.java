package com.example.launchproject.recevier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

// QQ音乐广播接收者
public class MusicReceiver extends BroadcastReceiver {
 
    private final DataCallBack mDataCallBack;
 
    public MusicReceiver(DataCallBack callBack) {
        mDataCallBack = callBack;
    }
 
    @Override
    public void onReceive(Context context, Intent intent) {
        String albumName = intent.getStringExtra("album");
        String artist = intent.getStringExtra("artist");
        String trackName = intent.getStringExtra("track");
        String xiaMiName = intent.getStringExtra("widget_song_name");

        Log.e("MusicReceiver======>", intent.getExtras().toString());
        System.out.println("最新的结果是！: " + albumName + " artist: "
                + artist + " Track:" + trackName+" xiaMiName:"+xiaMiName);
        if(albumName != null || artist != null || trackName != null) {
            mDataCallBack.onDataChanged(trackName, artist, albumName);
        }
    }
 
    public interface DataCallBack {
        void onDataChanged(String trackName,String artist,String albumName );
    }
}