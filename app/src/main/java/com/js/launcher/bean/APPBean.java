package com.js.launcher.bean;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import java.util.Arrays;
import java.util.Objects;

/**
 * 保存APP图标、包名、应用名称的实体类
 * */
public class APPBean {

    private String appName;

    private Bitmap appIcon;
    private String packageName;
    private Drawable appIconDrawable;
    private byte[] appIconBytes;

    public APPBean() {
        appName = "";
        appIcon = null;
        packageName = "";
        appIconBytes = null;
    }

    public APPBean(String appName, Bitmap appIcon, String packageName) {
        this.appName = appName;
        this.appIcon = appIcon;
        this.packageName = packageName;
    }

    public APPBean(String appName, Drawable appIconDrawable, String packageName) {
        this.appName = appName;
        this.appIconDrawable = appIconDrawable;
        this.packageName = packageName;
    }

    public APPBean(String appName, byte[] appIconBytes, String packageName) {
        this.appName = appName;
        this.appIconBytes = appIconBytes;
        this.packageName = packageName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Bitmap getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Bitmap appIcon) {
        this.appIcon = appIcon;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Drawable getAppIconDrawable() {
        return appIconDrawable;
    }

    public void setAppIconDrawable(Drawable appIconDrawable) {
        this.appIconDrawable = appIconDrawable;
    }

    public byte[] getAppIconBytes() {
        return appIconBytes;
    }

    public void setAppIconBytes(byte[] appIconBytes) {
        this.appIconBytes = appIconBytes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        APPBean appBean = (APPBean) o;
        return Objects.equals(packageName, appBean.packageName);
    }

    @Override
    public String toString() {
        return "APPBean{" +
                "appName='" + appName + '\'' +
                ", appIcon=" + appIcon +
                ", packageName='" + packageName + '\'' +
                ", appIconDrawable=" + appIconDrawable +
                ", appIconBytes=" + Arrays.toString(appIconBytes) +
                '}';
    }
}
