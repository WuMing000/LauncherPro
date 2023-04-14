package com.example.launchproject.bean;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public class APPBean {

    private String appName;

    private Bitmap appIcon;
    private String packageName;
    private Drawable appIconDrawable;

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

    @Override
    public String toString() {
        return "APPBean{" +
                "appName='" + appName + '\'' +
                ", appIcon=" + appIcon +
                ", packageName='" + packageName + '\'' +
                ", appIconDrawable=" + appIconDrawable +
                '}';
    }
}
