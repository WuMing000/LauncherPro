package com.example.launchproject.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Insets;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.example.launchproject.MyApplication;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import androidx.recyclerview.widget.RecyclerView;
import kotlin.Deprecated;

public class CustomUtil {

    /**
     * 判断视图v是否应该隐藏输入软键盘，若v不是输入框，返回false
     *
     * @param v     视图
     * @param event 屏幕事件
     * @return 视图v是否应该隐藏输入软键盘，若v不是输入框，返回false
     */
    public static boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = {0, 0};
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            return !(event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom);
        }
        return false;
    }

    //隐藏软键盘
    public static void hideKeyBoard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /** 判断触摸的位置是否在控件上 */
    public static boolean isTouchPointInView(@NotNull View view, int x, int y) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int left = location[0];
        int top = location[1];
        int right = left + view.getMeasuredWidth();
        int bottom = top + view.getMeasuredHeight();
        if (top <= y) {
            if (bottom >= y && x >= left && x <= right) {
                return true;
            }
        }
        return false;
    }

    /** 根据触摸的位置获取recyclerview的position */
    public static int findItem(RecyclerView recyclerView, int x, int y) {
        View childViewUnder = recyclerView.findChildViewUnder(x, y);
        if (childViewUnder != null) {
            RecyclerView.ViewHolder childViewHolder = recyclerView.getChildViewHolder(childViewUnder);
            if (childViewHolder != null) {
                return childViewHolder.getAdapterPosition();
            }
        }
        return -1;
    }

    /** 根据包名获取应用图标和名称 */
    public static Map<Drawable, String> getIconANDAppName(String packageName) {
        Map<Drawable, String > map = new HashMap<Drawable, String>();
        PackageManager pm = MyApplication.getInstance().getContext().getPackageManager();
        try {
            ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);

            // 应用名称
            String appName = (String) pm.getApplicationLabel(applicationInfo);

            //应用图标
            Drawable appIcon = pm.getApplicationIcon(applicationInfo);
            map.put(appIcon, appName);
            Log.d("CustomUtil", "appName:" + appName);
            return map;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 判断是否是系统应用 */
    public static boolean isSystemApplication(Context context, String packageName){
        PackageManager mPackageManager = context.getPackageManager();
        try {
            final PackageInfo packageInfo = mPackageManager.getPackageInfo(packageName, PackageManager.GET_CONFIGURATIONS);
            if((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM)!=0){
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** 执行命令 */
    public static void runCommand() {
        try {
            Process process = Runtime.getRuntime().exec("pm list package -3");
            BufferedReader bis = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            while ((line = bis.readLine()) != null) {
                System.out.println("MainActivity.runCommand, line=" + line);
            }
        } catch (IOException e) {
            System.out.println("MainActivity.runCommand,e=" + e);
        }
    }

    /**
     * 判断app能不能主动启动 否就隐藏
     * */
    public static boolean NotActiveApp(Context context, String packageName){
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        return intent == null;
    }

    /**
     * drawable转化成bitmap的方法
     * @param drawable 需要转换的Drawable
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
//        System.out.println("Drawable转Bitmap");
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(w,h,config);
        //注意，下面三行代码要用到，否则在View或者surfaceView里的canvas.drawBitmap会看不到图
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * bitmap转化成byte数组
     * @param bm 需要转换的Bitmap
     * @return
     */
    public static byte[] bitmap2Bytes(Bitmap bm){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    /** 隐藏系统底部导航栏 */
    public static void hideBottomUIMenu(Activity activity) {
        int flags;
        int curApiVersion = android.os.Build.VERSION.SDK_INT;
        // This work only for android 4.4+
        if(curApiVersion >= Build.VERSION_CODES.KITKAT){

            // This work only for android 4.4+
            // hide navigation bar permanently in android activity
            // touch the screen, the navigation bar will not show

            flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE;
        }else{
            // touch the screen, the navigation bar will show
            flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }

        // must be executed in main thread :)
        activity.getWindow().getDecorView().setSystemUiVisibility(flags);
    }

    /**
     * 获取状态栏的高度
     *
     * @param context
     * @return
     */
    @SuppressLint("PrivateApi")
    public static int getStatusHeight(Context context) {
        int statusHeight;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            WindowMetrics windowMetrics = wm.getCurrentWindowMetrics();
            WindowInsets windowInsets = windowMetrics.getWindowInsets();
            Insets insets = windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.navigationBars() | WindowInsets.Type.displayCutout());
            return insets.top;
        } else {
            Rect localRect = new Rect();
            ((Activity) context).getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);
            statusHeight = localRect.top;
            if (0 == statusHeight) {
                Class<?> localClass;
                try {
                    localClass = Class.forName("com.android.internal.R$dimen");
                    Object localObject = localClass.newInstance();
                    int height = Integer.parseInt(Objects.requireNonNull(localClass.getField("status_bar_height").get(localObject)).toString());
                    statusHeight = context.getResources().getDimensionPixelSize(height);
                    return statusHeight;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return -1;
    }

}
