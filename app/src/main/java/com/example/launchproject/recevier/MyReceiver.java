package com.example.launchproject.recevier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.TextView;
 
public class MyReceiver extends BroadcastReceiver {
 
    private ImageView mCover;
    private TextView mTitle ;
    private TextView singer;
    private TextView malbum;
 
    private DataCallBack mDataCallBack;
 
    public MyReceiver(DataCallBack callBack) {
        mDataCallBack = callBack;
    }
 
    @Override
    public void onReceive(Context context, Intent intent) {
        String albumName = intent.getStringExtra("album");
 
        String artist = intent.getStringExtra("artist");
 
        String trackName = intent.getStringExtra("track");
 
        String xiaMiName =intent.getStringExtra("widget_song_name");
 
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