package com.js.launcher.recevier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.js.launcher.MyApplication;
import com.js.launcher.manager.HandlerManager;

// 应用安装卸载广播接收者
public class MyInstalledReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
 
		if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {		// install
			String packageName = intent.getDataString();
			Log.i("launcher-homer", "安装了 :" + packageName);
			Handler handler = HandlerManager.getHandler();
			Message message = new Message();
			message.what = HandlerManager.INSTALL_APP_SUCCESS;
			message.obj = packageName;
			handler.sendMessageAtTime(message, 100);
		}
 
		if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {	// uninstall
			String packageName = intent.getDataString();
			Handler handler = HandlerManager.getHandler();
			Message message = new Message();
			message.what = HandlerManager.REMOVED_APP_SUCCESS;
			message.obj = packageName;
			handler.sendMessageAtTime(message, 100);
			Log.i("launcher-homer", "卸载了 :" + packageName);
			if ("com.tencent.qqmusicpad".equals(packageName.split(":")[1])) {
				Handler musicHandler = HandlerManager.getHandler();
				Message musicMessage = new Message();
				musicMessage.what = HandlerManager.MUSIC_INFORMATION_UPDATE;
				Bundle bundle = new Bundle();
				bundle.putString("trackName", "暂无歌名");
				bundle.putString("artist", "暂无歌手");
				musicMessage.obj = bundle;
				musicHandler.sendMessageAtTime(musicMessage, 100);
			}
		}
	}
}