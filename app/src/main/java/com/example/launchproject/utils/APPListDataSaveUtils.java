package com.example.launchproject.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.launchproject.bean.APPBean;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于保存APP信息的工具类，使用SharedPreferences数据库保存
 * 应用开机或者重启不用重新通用方法获取
 * 节省时间和实现记录APP移动的位置
 * */
public class APPListDataSaveUtils {
    private final SharedPreferences preferences;
    private final SharedPreferences.Editor editor;

    public APPListDataSaveUtils(Context mContext, String preferenceName) {
        preferences = mContext.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    /**
     * 保存普通String
     *
     * @param key
     * @param value
     */
    public void setDataString(String key, String value) {
        if (value == null) {
            return;
        }
        Gson gson = new Gson();
        //转换成json数据，再保存
        String strJson = gson.toJson(value);
        editor.clear();
        editor.putString(key, strJson);
        editor.commit();
    }

    /**
     * 获取普通的String
     * @param key
     * @return
     */
    public String getDataString(String key) {
        String value = "";
        String strJson = preferences.getString(key, null);
        if (null == strJson) {
            return value;
        }
        Gson gson = new Gson();
        value = gson.fromJson(strJson, new TypeToken<String>() {
        }.getType());
        return value;
    }

    /**
     * 保存List
     *
     * @param tag
     * @param dataList
     */
    public void setDataList(String tag, List<APPBean> dataList) {
        if (null == dataList || dataList.size() <= 0)
            return;

        Gson gson = new Gson();
        //转换成json数据，再保存
        String strJson = gson.toJson(dataList);
        editor.clear();
        editor.putString(tag, strJson);
        editor.commit();
    }

    /**
     * 获取List
     *
     * @param tag
     * @return
     */
    public List<APPBean> getDataList(String tag) {
        List<APPBean> dataList = new ArrayList();
        String strJson = preferences.getString(tag, null);
        if (null == strJson) {
            return dataList;
        }
        Gson gson = new Gson();
        dataList = gson.fromJson(strJson, new TypeToken<List<APPBean>>() {
        }.getType());
        return dataList;
    }
}
