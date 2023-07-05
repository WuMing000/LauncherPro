package com.js.launcher.utils;

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
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.js.launcher.MyApplication;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;

public class CustomUtil {

    private static final String TAG = "CustomUtil==========>";

    /**
     * 判断视图v是否应该隐藏输入软键盘，若v不是输入框，返回false
     *
     * @param v     视图
     * @param event 屏幕事件
     * @return 视图v是否应该隐藏输入软键盘，若v不是输入框，返回false
     */
    public static boolean isShouldHideInput(View v, MotionEvent event) {
        if ((v instanceof EditText)) {
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
            return bottom >= y && x >= left && x <= right;
        }
        return false;
    }

    /** 根据包名获取应用图标和名称 */
    public static Map<Drawable, String> getIconANDAppName(String packageName) {
        Map<Drawable, String > map = new HashMap<>();
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
        return "com.js.appstore".equals(packageName) || "com.js.photoalbum".equals(packageName);
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
     */
    public static byte[] bitmap2Bytes(Bitmap bm){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    /**
     * 隐藏底部底部导航栏
     */
    public static void hideNavigationBar(Activity activity) {

        Window window;
        window = activity.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE;
            window.setAttributes(params);


            int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION; // hide nav bar; // hide status bar

            uiFlags |= 0x00001000;    //SYSTEM_UI_FLAG_IMMERSIVE_STICKY: hide navigation bars - compatibility: building API level is lower than 19, use magic number directly for higher API target level

            activity.getWindow().getDecorView().setSystemUiVisibility(uiFlags);
        }
    }

    /**
     * 获取状态栏的高度
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

    public static String getServerFile(String path) {
        //获取网络数据
        String str = "";
        try {
            //2.实例化url
            URL url = new URL(path);
            //3.获取连接属性
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //4.设置请求方式
            conn.setRequestMethod("GET");
            //以及请求时间
            conn.setConnectTimeout(5000);
            //5.获取响应码
            int code = conn.getResponseCode();
            if (200 == code) {
                //6.获取返回的数据json
                InputStream is = conn.getInputStream();
                //7.测试（删除-注释）
                //缓冲字符流
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
//                stringBuffer = new StringBuilder();
                if ((str = br.readLine()) != null) {
//                    stringBuffer.append(str);
                    Log.i("tt", str);
                    return str;
                }
//                Log.i("tt", stringBuffer.toString());
                //8.解析
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    /**
     * 获取本地软件版本号名称
     */
    public static String getLocalVersionName() {
        String localVersion = "";
        try {
            PackageInfo packageInfo = MyApplication.getInstance().getContext().getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(MyApplication.getInstance().getContext().getPackageName(), 0);
            localVersion = packageInfo.versionName;
            Log.d(TAG, "本软件的版本名：" + localVersion);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return localVersion;
    }

    /**
     * oldPath 和 newPath必须是新旧文件的绝对路径
     */
    private static File renameFile(String oldPath, String newPath) {
        if (TextUtils.isEmpty(oldPath)) {
            return null;
        }

        if (TextUtils.isEmpty(newPath)) {
            return null;
        }
        File oldFile = new File(oldPath);
        File newFile = new File(newPath);
        boolean b = oldFile.renameTo(newFile);
        return new File(newPath);
    }

    /**
     * 安装APK内容
     */
    public static void installAPK(Context mContext, File apkName) {
        try {
            if (!apkName.exists()) {
                Log.e("TAG", "app not exists!");
                return;
            }
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//安装完成后打开新版本
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // 给目标应用一个临时授权
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//判断版本大于等于7.0
                Log.e("TAG", "11111111111111");
                //如果SDK版本>=24，即：Build.VERSION.SDK_INT >= 24，使用FileProvider兼容安装apk
                String packageName = mContext.getApplicationContext().getPackageName();
                String authority = packageName + ".fileprovider";
                Uri apkUri = FileProvider.getUriForFile(mContext, authority, apkName);
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            } else {
                intent.setDataAndType(Uri.fromFile(apkName), "application/vnd.android.package-archive");
            }
            mContext.startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("TAG", e.toString());
        }
    }

    /**
     * 下载文件
     * @param FilePath  要存放的文件的路径
     * @param FileName   远程FTP服务器上的那个文件的名字
     * @return   true为成功，false为失败
     */
    public static boolean downLoadFile(FTPClient ftpClient, String FilePath, String FileName) {
        Log.e(TAG, "=================run update APK");
        if (!ftpClient.isConnected()) {
            Log.e(TAG, "ftp is not connect");
            return false;
        }
        APPListDataSaveUtils update_size = new APPListDataSaveUtils(MyApplication.getInstance().getContext(), "update_size");
        OutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            // 转到指定下载目录
//            ftpClient.changeWorkingDirectory("/data");
            // 列出该目录下所有文件
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            FTPFile[] files = ftpClient.listFiles(new String(FileName.getBytes("GBK"),"iso-8859-1"));
            if (files.length != 1) {
                Log.e(TAG, "remote file is not exists!");
                return false;
            }
            //根据绝对路径初始化文件
            File localFile = new File(FilePath);
            if (localFile.length() > files[0].getSize()) {
                Log.e(TAG, "local size large remote size.");
                return false;
            }
            if (update_size.getDataString("updateSize").length() != 0 && !update_size.getDataString("updateSize").equals(String.valueOf(files[0].getSize()))) {
                Log.e(TAG, "remote size is difference.");
                new File(FilePath).delete();
            }
            update_size.setDataString("updateSize", String.valueOf(files[0].getSize()));
            // 输出流
            outputStream = new FileOutputStream(localFile, true);
            ftpClient.setRestartOffset(localFile.length());
            Log.e(TAG, localFile.length() + "");
            inputStream = ftpClient.retrieveFileStream(new String(FileName.getBytes("GBK"),"iso-8859-1"));
            byte[] bytes = new byte[4096];
            int c;
            int finishSize = (int) localFile.length();
            while((c = inputStream.read(bytes)) != -1){
                outputStream.write(bytes, 0, c);
                finishSize += c;
                Log.d(TAG, "downSize======" + finishSize);
                if (finishSize == files[0].getSize()) {
                    Log.e(TAG, "download success!");
                    renameFile(FilePath, FilePath + ".apk");
//                            installAPK(MyApplication.getInstance().getContext(), new File(FilePath + ".apk"));
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            Log.e(TAG, "close connect");
            try {
                //退出登陆FTP，关闭ftpClient的连接
                ftpClient.logout();
                ftpClient.disconnect();
                //关闭流
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * 是否开启通知权限
     * @param context
     * @return
     */
    public static boolean isNotificationListenerEnabled(Context context) {
        Set<String> packageNames = NotificationManagerCompat.getEnabledListenerPackages(context);
        Log.e(TAG, packageNames.toString());
        Log.e(TAG, context.getPackageName());
        if (!packageNames.contains(context.getPackageName())) {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return false;
        }
        return true;
    }

}
