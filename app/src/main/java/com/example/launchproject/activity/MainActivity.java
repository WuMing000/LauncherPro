package com.example.launchproject.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Instrumentation;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.launchproject.R;
import com.example.launchproject.adapter.MyGridViewAdapter;
import com.example.launchproject.bean.APPBean;
import com.example.launchproject.manager.HandlerManager;
import com.example.launchproject.recevier.MusicReceiver;
import com.example.launchproject.recevier.MyInstalledReceiver;
import com.example.launchproject.utils.CustomUtil;
import com.example.launchproject.utils.DataUtil;
import com.example.launchproject.view.DragGridView;
import com.example.launchproject.view.OvalImageView;

import java.io.FileDescriptor;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import static android.content.pm.PackageManager.GET_URI_PERMISSION_PATTERNS;

@SuppressLint("LongLogTag")
public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity==============>";

    private ViewPager mViewPager;
    private ArrayList<View> mPageView;
    private LinearLayout llBgHome;

    private List<APPBean> appBeanList;
    ImageView imageView;

    View view1, view2;
    //View1 control
    private TextView tvTime, tvCalendar;
    private ImageView ivPicture;
    private EditText etSource;
    private RelativeLayout rlMusic;
    private OvalImageView ivMusic;
    private TextView tvMusicName;
    private ImageView btnControl, btnPrevious, btnNext;
    private ImageView btnPicture, btnProjectionScreen, btnAlarm, btnBrowser;

    //View2 control
    private ImageView ibVideo, ibMusic, ibTiktok, ibOffice;
    private AudioManager audioManager;
    private Animation animation;

    private MusicReceiver myReceiver;
    private MyInstalledReceiver myInstalledReceiver;
    private String musicName, date, calendar;

    private SharedPreferences sp;
    private String spMusicName;
    private SharedPreferences.Editor editor;
    private PagerAdapter pagerAdapter;

    //app gridview
    private int totalPage;//总的页数(仅代表app加载的页数)
    private int mPageSize = 18;//每页显示的最大数量
    private APPBean saveFrontAPPContent;
    private int downPosition;
    private ScaleAnimation scaleAnimation;
    private boolean isScale;
    private boolean isDown;
    private int savePosition = 0;
    private int copyPageSelectedPosition;
    private boolean isGridView;

    private ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {

            copyPageSelectedPosition = position;

            if (position == 0 || position == 1) {
                return;
            }

            Log.e(TAG, position + ":onPageSelected");
            savePosition = position - 2;
//                Log.e(TAG, position + ":onPageScrolled");
            View view = mPageView.get(position);
            DragGridView gridView = (DragGridView) view;
//            MyGridViewAdapter adapter = (MyGridViewAdapter) gridView.getAdapter();
            gridView.setOnItemMoveListener(new DragGridView.OnItemMoveListener() {
                @Override
                public void onDown(int p, Handler handler, Runnable runnable) {
                    Log.d("TAG", "onDown");
//                Log.e(TAG, "childCount:" + rvAPPList.getChildCount());
                    int newPosition = p + savePosition * mPageSize;
                    if (appBeanList.get(newPosition).getPackageName().length() == 0) {
                        handler.removeCallbacks(runnable);
                    }
                    gridView.isUninstallVisible(CustomUtil.isSystemApplication(MainActivity.this, appBeanList.get(newPosition).getPackageName()));
                    Log.d("wu", "onDown====>" + newPosition + "");
//                if (rvAPPList.isDrag()) {
                    saveFrontAPPContent = new APPBean(appBeanList.get(newPosition).getAppName(), appBeanList.get(newPosition).getAppIcon(), appBeanList.get(newPosition).getPackageName());
//                    Log.e(TAG, saveFrontAPPContent.toString());
//                }
                    downPosition = newPosition;
//                recyclerViewAPPAdapter.isSetFrameVisible(true);
//                recyclerViewAPPAdapter.notifyDataSetChanged();
                }

                @Override
                public void onMove(int x, int y, View v, boolean isMove) {

                    Log.d("TAG", "onMove" + isMove);
                    int widthPixels = getResources().getDisplayMetrics().widthPixels;

                    Log.d("TAG", "widthPixels:" + widthPixels + ",x:" + x);
                    Log.d("TAG", "childCount" + gridView.getChildCount());
                    if (x >= (widthPixels - 200)) {
//                    rvAPPList.scrollToPosition((scrollHelper.getPageIndex() + 1) * 18 + 17);
//                    scrollHelper.scrollToPosition(scrollHelper.getPageIndex() + 1);
                        Log.e(TAG, savePosition + ":savePosition");
                        handler.postDelayed(nextRunnable, 1500);
//                            viewPager.requestLayout();
                    } else if (x <= 200) {
//                    rvAPPList.scrollToPosition((scrollHelper.getPageIndex() - 1) * 18);
//                    scrollHelper.scrollToPosition(scrollHelper.getPageIndex() - 1);
//                            --savePosition;
//                            viewPager.setCurrentItem(savePosition , true);
                        handler.postDelayed(previousRunnable, 1500);
                    } else {
                        handler.removeCallbacks(nextRunnable);
                        handler.removeCallbacks(previousRunnable);
                    }
                    if (isMove && !isScale) {



                        isDown = true;
                        isScale = true;
                        mViewPager.setAlpha(0.8f);
                        scaleAnimation = new ScaleAnimation(1.0f, 0.8f, 1.0f, 0.8f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                        scaleAnimation.setDuration(100);//设置动画持续时间
                        mViewPager.startAnimation(scaleAnimation);
                        scaleAnimation.setFillAfter(true);
                        for (int i = 0; i < mPageView.size(); i++) {
                            if (i > 1) {
                                DragGridView view2 = (DragGridView) mPageView.get(i);
                                view2.setBackground(getResources().getDrawable(R.drawable.selector_recyclerview_bg));
                            }
                        }
//                            viewPager.setPadding(150, 0, 150, 0);
//                            viewPager.setClipChildren(false);

//                            gridView.setBackground(getResources().getDrawable(R.drawable.selector_recyclerview_bg));
//                            LinearLayout.LayoutParams marginLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//                            marginLayoutParams.setMargins(350, 0, 350, 0);
//                            viewPager.setLayoutParams(marginLayoutParams);
//                            viewPager.setClipChildren(false);
                    }
//                Log.e(TAG, "isCanDrag:" + isCanDrag);
//                int movePosition = CustomUtil.findItem(rvAPPList, x, y);
//                if (movePosition != -1 && movePosition != downPosition) {
//                    appBeanList.set(downPosition, appBeanList.get(movePosition));
//                }
//                else {
//                    appBeanList.set(downPosition, saveFrontAPPContent);
//                    recyclerViewAPPAdapter.notifyDataSetChanged();
//                }
                }

                @Override
                public void onUp(int p) {
                    Log.e(TAG, "1111111111111111111111111111111111111111:" + savePosition);
                    int newPosition = p + savePosition * mPageSize;
                    Log.d("TAG", "onUp:downPosition:" + downPosition + ",position:" + newPosition + ",saveFrontAPPContent:" + saveFrontAPPContent + ",moveData:" + appBeanList.get(newPosition));
                    if (isScale) {
                        isScale = false;
//                            gridView.setBackgroundColor(getResources().getColor(R.color.transparent));
//                        mViewPager.setAlpha(1.0f);
//                        scaleAnimation = new ScaleAnimation(0.8f, 1.0f, 0.8f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
//                        scaleAnimation.setDuration(300);//设置动画持续时间
//                        mViewPager.startAnimation(scaleAnimation);
//                        scaleAnimation.setFillAfter(true);
                        handler.postDelayed(clearAnimationRunnable, 1000);
//                            viewPager.setClipToPadding(true);
//                            LinearLayout.LayoutParams marginLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//                            marginLayoutParams.setMargins(0, 0, 0, 0);
//                            viewPager.setLayoutParams(marginLayoutParams);
                    }
//                Log.e(TAG, "getStartPageIndex:" + scrollHelper.getStartPageIndex());
//                scrollHelper.scrollToPosition(scrollHelper.getStartPageIndex() + 1);
                    if (newPosition != downPosition && gridView.isDrag()) {
                        Log.e("TAG", "我进来了");
                        appBeanList.set(downPosition, appBeanList.get(newPosition));
                        appBeanList.set(newPosition, saveFrontAPPContent);
                        pagerAdapter.notifyDataSetChanged();
                    }
//                    viewPager.getAdapter().notifyDataSetChanged();

//                    adapter.notifyDataSetChanged();
                    if (isDown) {
                        isDown = false;
                    }
//                Log.d(TAG, "onUp====>" + position + "");
//                if (saveFrontAPPContent != null) {
//                    Log.e(TAG, "我进来了");
//                    appBeanList.set(position, saveFrontAPPContent);
//                    recyclerViewAPPAdapter.notifyDataSetChanged();
//                }
//                appBeanList.set(position, appBeanList.get(movePosition));
//                appBeanList.set(movePosition, appBeanList.get(position));
//                recyclerViewAPPAdapter.notifyDataSetChanged();
                }

                @Override
                public void onItemClick(int p) {
                    Log.e(TAG, "onItemClick: p:" + p + ",savePosition:" + savePosition + ",mPageSize:" + mPageSize);
                    int newPosition = p + savePosition * mPageSize;
                    if (appBeanList.get(newPosition).getPackageName().length() == 0 || p == -1) {
                        return;
                    }
                    Log.d("TAG", "onItemClick====>" + newPosition + "");
//                    Log.d("TAG", "click recyclerview item " + newPosition);
                    //查询这个应用程序的入口activity。把他开启起来
                    try {
                        PackageManager pm = getPackageManager();
                        Intent intent = pm.getLaunchIntentForPackage(appBeanList.get(newPosition).getPackageName());
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            DragGridView dragGridView = (DragGridView) mPageView.get(copyPageSelectedPosition);
            dragGridView.setOnUninstallClick(new DragGridView.OnUninstallClick() {
                @Override
                public void OnClick() {
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.DELETE");
                    intent.setData(Uri.parse("package:" + appBeanList.get(downPosition).getPackageName()));
                    Toast.makeText(MainActivity.this, "卸载" + appBeanList.get(downPosition).getAppName(), Toast.LENGTH_LONG).show();
                    startActivity(intent);
                }
            });

            dragGridView.setOnInformationClick(new DragGridView.OnInformationClick() {
                @Override
                public void OnClick() {
                    Log.e("onContextItemSelected", appBeanList.get(downPosition).getPackageName());
                    gotoAppDetailIntent(MainActivity.this, appBeanList.get(downPosition).getPackageName());
                }
            });
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    Handler handler = new Handler(Looper.myLooper()) {

        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void dispatchMessage(@NonNull Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what) {
                case HandlerManager.GET_APP_LIST:
                    Bundle data = msg.getData();
                    byte[] appIcons = data.getByteArray("appIcon");
                    String appName = data.getString("appName");
                    String packageName = data.getString("packageName");
                    Bitmap bitmap = BitmapFactory.decodeByteArray(appIcons, 0, appIcons.length);
                    Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                    appBeanList.add(new APPBean(appName, bitmap, packageName));
                    break;
                case HandlerManager.LOAD_APP_FINISH:
                    while (appBeanList.size() % 18 != 0) {
                        appBeanList.add(new APPBean());
                    }
                    Log.e(TAG, "加载应用完成" + appBeanList.size());

                    //add app list gridview
                    //总的页数，取整（这里有三种类型：Math.ceil(3.5)=4:向上取整，只要有小数都+1  Math.floor(3.5)=3：向下取整  Math.round(3.5)=4:四舍五入）
                    totalPage = (int) Math.ceil(appBeanList.size() * 1.0 / mPageSize);
                    LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                    for(int i = 0; i < totalPage; i++){
                        //每个页面都是inflate出一个新实例
                        DragGridView gridView = (DragGridView) inflater.inflate(R.layout.grid_view_app_list, null);
                        MyGridViewAdapter myGridViewAdapter = new MyGridViewAdapter(MainActivity.this, appBeanList, i, mPageSize);
                        gridView.setAdapter(myGridViewAdapter);
                        //每一个GridView作为一个View对象添加到ViewPager集合中
                        mPageView.add(gridView);
                        pagerAdapter.notifyDataSetChanged();
                    }
                    break;
                case HandlerManager.GET_SYSTEM_TIME:
                    handler.postDelayed(updateTimeThread, 1000);
                    break;
                case HandlerManager.HOME_LONG_CLICK :
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                    alertDialog.setMessage("是否设置壁纸")
                            .setPositiveButton("设置壁纸", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    onSetWallpaper();
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .show();
                    break;
                case HandlerManager.MUSIC_PLAY_UI :
                    btnControl.setBackground(getResources().getDrawable(R.drawable.start_music));
                    break;
                case HandlerManager.MUSIC_PAUSE_UI :
                    btnControl.setBackground(getResources().getDrawable(R.drawable.pause_music));
                    break;
                case HandlerManager.REMOVED_APP_SUCCESS:
                    try {
                        int i = -1;
                        boolean isLastALLNone = true;
                        String obj = (String) msg.obj;
                        String removePkg = obj.split(":")[1];
                        for (APPBean appBean : appBeanList) {
//                        Log.d(TAG, "removePkg:" + removePkg + ",appBean.getPackageName():" + appBean.getPackageName());
                            if (removePkg.equals(appBean.getPackageName())) {
                                i = appBeanList.indexOf(appBean);
                                Log.d(TAG, "removePkg:" + removePkg + ",i:" + i);
                            }
                        }
                        if (i != -1) {
                            appBeanList.set(i, new APPBean());
                        }
                        for (int j = appBeanList.size() - 18; j < appBeanList.size(); j++) {
                            if(appBeanList.get(j).getPackageName().length() != 0) {
                                isLastALLNone = false;
                            }
                        }
                        Log.e(TAG, "remove:" + appBeanList.size() + ",isLastALLNone:" + isLastALLNone);
                        if (isLastALLNone) {
                            int totalSize = appBeanList.size() - 18;
                            for (int j = appBeanList.size() - 1; j >= totalSize; j--) {
                                Log.e(TAG, "j: " + j);
                                appBeanList.remove(j);
                            }
//                            mViewPager.removeView(mPageView.get(copyPageSelectedPosition));
                            mPageView.remove(copyPageSelectedPosition);
                        }
                        Log.e(TAG, "mPageRemove:" + mPageView.size());
//                        onPageChangeListener.onPageSelected(copyPageSelectedPosition);
                        pagerAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case HandlerManager.INSTALL_APP_SUCCESS:
                    try {
                        Bitmap icon = null;
                        String name = null;
                        String obj1 = (String) msg.obj;
                        String installPkg = obj1.split(":")[1];
                        int firstNullPosition = -1;
                        Log.e(TAG, installPkg);
                        Map<Drawable, String> iconANDAppName = CustomUtil.getIconANDAppName(installPkg);
                        if (iconANDAppName != null) {
                            Set<Map.Entry<Drawable, String>> entries = iconANDAppName.entrySet();
                            for (Map.Entry<Drawable, String> entry : entries) {
                                Drawable key = entry.getKey();
                                name = entry.getValue();
                                Log.d(TAG, "appName:" + name);
                                icon = CustomUtil.drawableToBitmap(key);
                            }
                        }
                        APPBean installAPPBean = new APPBean(name, icon, installPkg);
                        boolean isAdd = appBeanList.contains(installAPPBean);
                        for (int i = 0; i < appBeanList.size(); i++) {
                            if (appBeanList.get(i).getPackageName().length() == 0) {
                                firstNullPosition = i;
                                break;
                            }
                        }
                        Log.e(TAG, "isADD" + isAdd);
                        if (!isAdd) {
                            if (firstNullPosition != -1) {
                                appBeanList.set(firstNullPosition, installAPPBean);
                            } else {
                                appBeanList.add(installAPPBean);
                            }
                            Toast.makeText(MainActivity.this, name + " 安装成功", Toast.LENGTH_SHORT).show();
                        }
                        while (appBeanList.size() % 18 != 0) {
                            appBeanList.add(new APPBean());
                        }
                        if (firstNullPosition == -1) {
                            Log.e(TAG, "appList:" + appBeanList.size());
                            totalPage = (int) Math.ceil(appBeanList.size() * 1.0 / mPageSize);
                            DragGridView gridView = (DragGridView) LayoutInflater.from(MainActivity.this).inflate(R.layout.grid_view_app_list, null);
                            MyGridViewAdapter myGridViewAdapter = new MyGridViewAdapter(MainActivity.this, appBeanList, totalPage - 1, mPageSize);
                            gridView.setAdapter(myGridViewAdapter);
                            //每一个GridView作为一个View对象添加到ViewPager集合中
                            mPageView.add(gridView);
                        }
                        pagerAdapter.notifyDataSetChanged();
                        mViewPager.setCurrentItem(mPageView.size() - 1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case HandlerManager.CLEAR_RECYCLER_ANIMATION:
                    for (int i = 0; i < mPageView.size(); i++) {
                        if (i > 1) {
                            DragGridView view2 = (DragGridView) mPageView.get(i);
                            view2.setBackgroundColor(getResources().getColor(R.color.transparent));
                        }
                    }
                    scaleAnimation = new ScaleAnimation(0.8f, 1.0f, 0.8f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    scaleAnimation.setDuration(100);//设置动画持续时间
                    scaleAnimation.setFillAfter(true);
                    mViewPager.startAnimation(scaleAnimation);
                    mViewPager.setAlpha(1.0f);
                    break;
                case HandlerManager.SKIP_NEXT_PAGE:
                    mViewPager.setCurrentItem(copyPageSelectedPosition + 1, true);
                    handler.removeCallbacks(nextRunnable);
                    break;
                case HandlerManager.SKIP_PREVIOUS_PAGE:
                    if (copyPageSelectedPosition > 2) {
                        mViewPager.setCurrentItem(copyPageSelectedPosition - 1, true);
                        handler.removeCallbacks(previousRunnable);
                    }
                    break;
                default:
                    Log.e(TAG, "It's not send handler message.");
                    break;
            }
        }
    };

    Runnable updateTimeThread = new Runnable() {

        public void run() {
            // TODO Auto-generated method stub
            SimpleDateFormat df = new SimpleDateFormat("HH:mm");
            date = df.format(new java.util.Date());
            tvTime.setText(date);
            calendar = DataUtil.StringData();
            tvCalendar.setText(calendar);
        }
    };

    Runnable updateWallpaper = new Runnable() {

        public void run() {
            handler.sendEmptyMessageAtTime(HandlerManager.HOME_LONG_CLICK, 500);
        }
    };

    Runnable clearAnimationRunnable = new Runnable() {

        public void run() {
            handler.sendEmptyMessageAtTime(HandlerManager.CLEAR_RECYCLER_ANIMATION, 100);
        }
    };

    Runnable nextRunnable = new Runnable() {
        @Override
        public void run() {
            handler.sendEmptyMessageAtTime(HandlerManager.SKIP_NEXT_PAGE, 100);
        }
    };

    Runnable previousRunnable = new Runnable() {
        @Override
        public void run() {
            handler.sendEmptyMessageAtTime(HandlerManager.SKIP_PREVIOUS_PAGE, 100);
        }
    };

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState");
        outState.putString("musicName", musicName);
        outState.putString("date", date);
        outState.putString("calendar", calendar);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.e(TAG, "onConfigurationChanged");
        LayoutInflater layoutInflater = getLayoutInflater();
        Configuration mConfiguration = this.getResources().getConfiguration(); //获取设置的配置信息
        int ori = mConfiguration.orientation; //获取屏幕方向
        if (ori == Configuration.ORIENTATION_LANDSCAPE) {
//            //横屏
            view1 = layoutInflater.inflate(R.layout.one_view_pager_landscape, null);
            view2 = layoutInflater.inflate(R.layout.two_view_pager_landscape, null);
        } else if (ori == Configuration.ORIENTATION_PORTRAIT) {
//            //竖屏
            view1 = layoutInflater.inflate(R.layout.one_view_pager_portrait, null);
            view2 = layoutInflater.inflate(R.layout.two_view_pager_portrait, null);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //隐藏底部导航栏
//        View decorView = getWindow().getDecorView();
//        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
//        decorView.setSystemUiVisibility(uiOptions);
        CustomUtil.hideBottomUIMenu(this);
        setContentView(R.layout.activity_main);
        HandlerManager.putHandler(handler);
        sp = getSharedPreferences("home_save_data", Activity.MODE_PRIVATE);
        initData();
        if (savedInstanceState != null) {
            String musicNameSave = savedInstanceState.getString("musicName");
            String dateSave = savedInstanceState.getString("date");
            String calendarSave = savedInstanceState.getString("calendar");
            musicName = musicNameSave;
            date = dateSave;
            calendar = calendarSave;
            tvMusicName.setText(musicNameSave);
            tvTime.setText(dateSave);
            tvCalendar.setText(calendarSave);
        }
        spMusicName = sp.getString("musicName", "暂无歌曲信息");
        tvMusicName.setText(spMusicName);
        setBackground();

    }

    private void initData() {

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anmi_rotate_view);
        appBeanList = new ArrayList<>();
        llBgHome = findViewById(R.id.ll_bg_home);
        mViewPager = findViewById(R.id.view_pager);
        LayoutInflater layoutInflater = getLayoutInflater();

        Configuration mConfiguration = this.getResources().getConfiguration(); //获取设置的配置信息
        int ori = mConfiguration.orientation; //获取屏幕方向
        if (ori == mConfiguration.ORIENTATION_LANDSCAPE) {
            //横屏
            view1 = layoutInflater.inflate(R.layout.one_view_pager_landscape, null);
            view2 = layoutInflater.inflate(R.layout.two_view_pager_landscape, null);
        } else if (ori == mConfiguration.ORIENTATION_PORTRAIT) {
            //竖屏
            view1 = layoutInflater.inflate(R.layout.one_view_pager_portrait, null);
            view2 = layoutInflater.inflate(R.layout.two_view_pager_portrait, null);
        }

        tvTime = view1.findViewById(R.id.tv_time);
        tvCalendar = view1.findViewById(R.id.tv_calendar);
        handler.sendEmptyMessageAtTime(HandlerManager.GET_SYSTEM_TIME, 1000);
        ivPicture = view1.findViewById(R.id.iv_picture);
        etSource = view1.findViewById(R.id.et_source);
        rlMusic = view1.findViewById(R.id.rl_music);
        ivMusic = view1.findViewById(R.id.iv_music);
        tvMusicName = view1.findViewById(R.id.tv_music_name);
        btnControl = view1.findViewById(R.id.btn_control);
        btnPrevious = view1.findViewById(R.id.btn_previous);
        btnNext = view1.findViewById(R.id.btn_next);
        btnPicture = view1.findViewById(R.id.btn_picture);
        btnProjectionScreen = view1.findViewById(R.id.btn_projection_screen);
        btnAlarm = view1.findViewById(R.id.btn_alarm);
        btnBrowser = view1.findViewById(R.id.btn_browser);
        ibVideo = view2.findViewById(R.id.ib_video);
        ibMusic = view2.findViewById(R.id.ib_music);
        ibTiktok = view2.findViewById(R.id.ib_tiktok);
        ibOffice = view2.findViewById(R.id.ib_office);
        initClickListener();
        if (ori == mConfiguration.ORIENTATION_LANDSCAPE) {
//            //横屏
        } else if (ori == mConfiguration.ORIENTATION_PORTRAIT) {
//            //竖屏
        }

        new Thread() {
            @Override
            public void run() {
                super.run();
                getAllAppNames();
            }
        }.start();

        mPageView = new ArrayList<>();
        mPageView.add(view1);
        mPageView.add(view2);

        //app gridview
//        initGridView();

//        mPageView.add(view3);

        pagerAdapter = new PagerAdapter() {

            //获取当前窗体界面数
            @Override
            public int getCount() {
                return mPageView.size();
            }

            //判断是否由对象生成界面
            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object;
            }

            //使从ViewGroup移除当前View
            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
//                Log.e(TAG, "destroyItem:" + position);
                container.removeView((View) object);
            }

            //返回一个对象，这个对象表明了PagerAdapter适配器选择哪个对象放在当前的ViewPager中
            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                container.addView(mPageView.get(position));
                return mPageView.get(position);
            }

            @Override
            public int getItemPosition(Object object) {
                return POSITION_NONE;
            }
        };

        mViewPager.setPageMargin(15);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.addOnPageChangeListener(onPageChangeListener);
        onPageChangeListener.onPageSelected(0);
    }

//    private void initGridView() {
//
//    }

    @SuppressLint("ClickableViewAccessibility")
    private void initClickListener() {
        //View1 listener
        ivPicture.setOnClickListener(view -> {
            Log.e("ivPicture=====>", "onClick");
//                onSetWallpaper();
            try {
                Intent intent = new Intent();
                ComponentName componentNameGallery = new ComponentName("com.android.gallery3d", "com.android.gallery3d.app.GalleryActivity");
                intent.setComponent(componentNameGallery);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "系统中暂无该应用", Toast.LENGTH_SHORT).show();
            }
        });

        etSource.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (i == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    String sourceText = etSource.getText().toString().trim();
                    Uri uri = null;
                    try {
                        uri = Uri.parse("http://www.baidu.com/s?&ie=utf-8&oe=UTF-8&wd=" + URLEncoder.encode(sourceText, "UTF-8"));
                        Log.d(TAG, "source content is" + sourceText);
                    } catch (UnsupportedEncodingException e1) {
                        e1.printStackTrace();
                    }
                    final Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    intent.setData(uri);
                    startActivity(intent);
                }
                return true;
            }
        });

        btnPicture.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_btn_small);
                btnPicture.startAnimation(animation);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                btnPicture.clearAnimation();
                try {
                    Intent intent = new Intent();
                    ComponentName componentNameGallery = new ComponentName("com.android.gallery3d", "com.android.gallery3d.app.GalleryActivity");
                    intent.setComponent(componentNameGallery);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "系统中暂无该应用", Toast.LENGTH_SHORT).show();
                }
            } else if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                btnPicture.clearAnimation();
            }
            return true;
        });
        rlMusic.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_btn_small);
                rlMusic.startAnimation(animation);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                rlMusic.clearAnimation();
                try {
                    Intent intent = new Intent();
                    ComponentName componentNameGallery = new ComponentName("com.tencent.qqmusic", "com.tencent.qqmusic.activity.AppStarterActivity");
                    intent.setComponent(componentNameGallery);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "系统中暂无该应用", Toast.LENGTH_SHORT).show();
                }
            } else if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                rlMusic.clearAnimation();
            }
            return true;
        });
        btnProjectionScreen.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_btn_small);
                btnProjectionScreen.startAnimation(animation);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                btnProjectionScreen.clearAnimation();
                try {
                    Intent intent = new Intent();
                    ComponentName componentNameGallery = new ComponentName("com.hpplay.happyplay.aw", "com.hpplay.happyplay.main.app.MainActivity");
                    intent.setComponent(componentNameGallery);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "系统中暂无该应用", Toast.LENGTH_SHORT).show();
                }
            } else if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                btnProjectionScreen.clearAnimation();
            }
            return true;
        });
        btnAlarm.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_btn_small);
                btnAlarm.startAnimation(animation);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                btnAlarm.clearAnimation();
                try {
                    Intent intent = new Intent();
                    ComponentName componentNameGallery = new ComponentName("com.android.deskclock", "com.android.deskclock.DeskClock");
                    intent.setComponent(componentNameGallery);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "系统中暂无该应用", Toast.LENGTH_SHORT).show();
                }
            } else if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                btnAlarm.clearAnimation();
            }
            return true;
        });
        btnBrowser.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_btn_small);
                btnBrowser.startAnimation(animation);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                btnBrowser.clearAnimation();
                try {
                    Intent intent = new Intent();
                    ComponentName componentNameGallery = new ComponentName("com.android.browser", "com.android.browser.BrowserActivity");
                    intent.setComponent(componentNameGallery);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "系统中暂无该应用", Toast.LENGTH_SHORT).show();
                }
            } else if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                btnBrowser.clearAnimation();
            }
            return true;
        });

        btnControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        if (audioManager.isMusicActive()) {
                            Instrumentation mInst = new Instrumentation();
                            mInst.sendKeyDownUpSync(KeyEvent.KEYCODE_MEDIA_PAUSE);
                            handler.sendEmptyMessageAtTime(HandlerManager.MUSIC_PLAY_UI, 100);
                            ivMusic.clearAnimation();
                        } else {
                            Instrumentation mInst = new Instrumentation();
                            mInst.sendKeyDownUpSync(KeyEvent.KEYCODE_MEDIA_PLAY);
                            handler.sendEmptyMessageAtTime(HandlerManager.MUSIC_PAUSE_UI, 100);
                            ivMusic.startAnimation(animation);//開始动画
                        }
                    }
                }.start();
            }
        });

        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        Instrumentation mInst = new Instrumentation();
                        mInst.sendKeyDownUpSync(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
                        handler.sendEmptyMessageAtTime(HandlerManager.MUSIC_PAUSE_UI, 100);
                        ivMusic.startAnimation(animation);//開始动画
                    }
                }.start();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        Instrumentation mInst = new Instrumentation();
                        mInst.sendKeyDownUpSync(KeyEvent.KEYCODE_MEDIA_NEXT);
                        handler.sendEmptyMessageAtTime(HandlerManager.MUSIC_PAUSE_UI, 100);
                        ivMusic.startAnimation(animation);//開始动画
                    }
                }.start();
            }
        });

        ivMusic.setStrokeWidth(20);
        ivMusic.setStrokeColor(Color.BLACK);

        //View2 listener
        ibVideo.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_btn_small);
                ibVideo.startAnimation(animation);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                ibVideo.clearAnimation();
                try {
                    Intent intent = new Intent();
                    ComponentName componentNameGallery = new ComponentName("com.qiyi.video", "com.qiyi.video.WelcomeActivity");
                    intent.setComponent(componentNameGallery);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "系统中暂无该应用", Toast.LENGTH_SHORT).show();
                }
            } else if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                ibVideo.clearAnimation();
            }
            return true;
        });

        ibMusic.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_btn_small);
                ibMusic.startAnimation(animation);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                ibMusic.clearAnimation();
                try {
                    Intent intent = new Intent();
                    ComponentName componentNameGallery = new ComponentName("com.tencent.qqmusic", "com.tencent.qqmusic.activity.AppStarterActivity");
                    intent.setComponent(componentNameGallery);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "系统中暂无该应用", Toast.LENGTH_SHORT).show();
                }
            } else if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                ibMusic.clearAnimation();
            }
            return true;
        });

        ibTiktok.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_btn_small);
                ibTiktok.startAnimation(animation);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                ibTiktok.clearAnimation();
                try {
                    Intent intent = new Intent();
                    ComponentName componentNameGallery = new ComponentName("com.ss.android.ugc.aweme", "com.ss.android.ugc.aweme.splash.SplashActivity");
                    intent.setComponent(componentNameGallery);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "系统中暂无该应用", Toast.LENGTH_SHORT).show();
                }
            } else if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                ibTiktok.clearAnimation();
            }
            return true;
        });

        ibOffice.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_btn_small);
                ibOffice.startAnimation(animation);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                ibOffice.clearAnimation();
                try {
                    Intent intent = new Intent();
                    ComponentName componentNameGallery = new ComponentName("com.tencent.android.qqdownloader", "com.tencent.assistantv2.activity.MainActivity");
                    intent.setComponent(componentNameGallery);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "系统中暂无该应用", Toast.LENGTH_SHORT).show();
                }
            } else if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                ibOffice.clearAnimation();
            }
            return true;
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        etSource.setText("");
//        registerForContextMenu(rvAPPList);//为RecyclerviewView注册上下文菜单
        receive();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (audioManager.isMusicActive()) {
            handler.sendEmptyMessageAtTime(HandlerManager.MUSIC_PAUSE_UI, 100);
            ivMusic.startAnimation(animation);//開始动画
        } else {
            handler.sendEmptyMessageAtTime(HandlerManager.MUSIC_PLAY_UI, 100);
            ivMusic.clearAnimation();
        }
    }

    /**
     * 跳转到应用详情界面
     */
    public static void gotoAppDetailIntent(Activity activity, String packageName) {
        Intent intent = new Intent();
        intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + packageName));
        activity.startActivity(intent);
    }

    private void receive(){
        myReceiver = new MusicReceiver(new MusicReceiver.DataCallBack() {
            @Override
            public void onDataChanged(String trackName, String artist, String albumName) {
                musicName = trackName + " - " + artist;
                tvMusicName.setText(musicName);
                editor = sp.edit();
                editor.putString("musicName", musicName);
                editor.commit();
            }
        });
        IntentFilter iF = new IntentFilter();
        iF.addAction("com.android.music.metachanged");
        registerReceiver(myReceiver, iF);

        //动态注册应用安装和卸载广播
        myInstalledReceiver = new MyInstalledReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addDataScheme("package");
        intentFilter.addAction("android.intent.action.PACKAGE_ADDED");
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        registerReceiver(myInstalledReceiver, intentFilter);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 使editText点击外部时候失去焦点
     *
     * @param ev 触屏事件
     * @return 事件是否被消费
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            int rawX = (int) ev.getRawX();
            int rawY = (int) ev.getRawY();
            View v = getCurrentFocus();

//            if (CustomUtil.isTouchPointInView(rvAPPList, rawX, rawY) && appBeanList.get(CustomUtil.findItem(rvAPPList, rawX, rawY)).getPackageName().length() == 0) {
//                return true;
//            }

            Log.e(TAG, "rawX " + rawX + "rawY " + rawY);
//            if (CustomUtil.isTouchPointInView(rvAPPList, rawX, rawY)) {
//                rvAPPList.removeLongClick();
//            }

            Log.e(TAG, "copyPageSelectedPosition:dispatch:" + copyPageSelectedPosition);
            if (copyPageSelectedPosition > 1) {
                GridView gridView = (GridView) mPageView.get(copyPageSelectedPosition);
                isGridView = CustomUtil.isTouchPointInView(gridView, rawX, rawY);
                Log.e(TAG, "gridview:" + isGridView);
            }
            if (!(CustomUtil.isTouchPointInView(etSource, rawX, rawY) || CustomUtil.isTouchPointInView(rlMusic, rawX, rawY)
                    || CustomUtil.isTouchPointInView(tvTime, rawX, rawY)
                    || CustomUtil.isTouchPointInView(tvCalendar, rawX, rawY)
                    || CustomUtil.isTouchPointInView(ivPicture, rawX, rawY)
                    || CustomUtil.isTouchPointInView(btnPicture, rawX, rawY)
                    || CustomUtil.isTouchPointInView(btnBrowser, rawX, rawY)
                    || CustomUtil.isTouchPointInView(btnAlarm, rawX, rawY)
                    || CustomUtil.isTouchPointInView(btnProjectionScreen, rawX, rawY)
                    || CustomUtil.isTouchPointInView(ibMusic, rawX, rawY)
                    || CustomUtil.isTouchPointInView(ibOffice, rawX, rawY)
                    || CustomUtil.isTouchPointInView(ibTiktok, rawX, rawY)
                    || CustomUtil.isTouchPointInView(ibVideo, rawX, rawY)
                    || isGridView)) {
                handler.postDelayed(updateWallpaper, 500);
            }
            if (CustomUtil.isShouldHideInput(v, ev)) {
                //点击editText控件外部
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    assert v != null;
                    //软键盘工具类关闭软键盘
                    CustomUtil.hideKeyBoard(MainActivity.this);
                    //使输入框失去焦点
                    v.clearFocus();
                }
            }
            return super.dispatchTouchEvent(ev);
        } else if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL) {
             handler.removeCallbacks(updateWallpaper);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        return getWindow().superDispatchTouchEvent(ev) || onTouchEvent(ev);
    }

    public void getAllAppNames() {
        PackageManager pm = getPackageManager();
        //获取到所有安装了的应用程序的信息，包括那些卸载了的，但没有清除数据的应用程序
        List<PackageInfo> list2 = pm.getInstalledPackages(GET_URI_PERMISSION_PATTERNS);

        int j = 0;

        for (PackageInfo packageInfo : list2) {

            if (packageInfo.versionName == null) {
                continue;
            }
            //过滤系统APP
//            ApplicationInfo itemInfo = packageInfo.applicationInfo;
//            if  ((itemInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
//                continue;

            //得到手机上已经安装的应用的名字,即在AndroidManifest.xml中的app_name。
            String appName = packageInfo.applicationInfo.loadLabel(getPackageManager()).toString();
            //得到手机上已经安装的应用的图标,即在AndroidManifest.xml中的icon。
            Drawable drawable = packageInfo.applicationInfo.loadIcon(getPackageManager());
            //得到应用所在包的名字,即在AndroidManifest.xml中的package的值。
            String packageName = packageInfo.packageName;
//            Log.e("=======aaa", "应用的名字:" + appName);
//            Log.e("=======bbb", "应用的包名字:" + packageName);
//            APPBean appBean = new APPBean(packageName, drawable);
            byte[] appIconBytes = CustomUtil.bitmap2Bytes(CustomUtil.drawableToBitmap(drawable));

            if (CustomUtil.NotActiveApp(this, packageName)) {
                continue;
            }

            Message message = new Message();
            Bundle bundle = new Bundle();
            bundle.putByteArray("appIcon", appIconBytes);
            bundle.putString("appName", appName);
            bundle.putString("packageName", packageName);
            message.setData(bundle);
            message.what = HandlerManager.GET_APP_LIST;
            handler.sendMessageAtTime(message, 10);
            j++;
        }
        Log.e("========ccc", "应用的总个数:" + j);
        Message message = new Message();
        message.what = HandlerManager.LOAD_APP_FINISH;
        message.obj = j;
        handler.sendMessageAtTime(message, 100);
    }

//    /**
//     * 设置菜单列表项
//     * @param menu
//     * @param v
//     * @param menuInfo
//     */
//    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//        super.onCreateContextMenu(menu, v, menuInfo);
//        setIconEnable(menu, true);
//        getMenuInflater().inflate(R.menu.rv_content_menu, menu);
//    }

//    /**
//     * 处理不同的菜单项
//     * @param item
//     * @return
//     */
//    @SuppressLint("NonConstantResourceId")
//    public boolean onContextItemSelected(MenuItem item) {
//        //获取被选择的菜单位置
//        int position = recyclerViewAPPAdapter.getPosition();
//        Log.e("position", position + "");
//
//        switch (item.getItemId()) {
//            case R.id.menu_uninstall://卸载选中项
//                //TODO
//                Intent intent = new Intent();
//                intent.setAction("android.intent.action.DELETE");
//                intent.setData(Uri.parse("package:" + appBeanList.get(position).getPackageName()));
//                Toast.makeText(MainActivity.this, "卸载" + appBeanList.get(position).getAppName(), Toast.LENGTH_LONG).show();
//                savePosition = position;
//                startActivity(intent);
////                startActivity(intent);
////                appBeanList.remove(position);
////                recyclerViewAPPAdapter.notifyDataSetChanged();
//                break;
//            case R.id.menu_information://应用信息选中项
//                //TODO
//                Log.e("onContextItemSelected", appBeanList.get(position).getPackageName());
//                gotoAppDetailIntent(MainActivity.this, appBeanList.get(position).getPackageName());
//                break;
//            default:
//                break;
//        }
//        return super.onContextItemSelected(item);
//    }

    public void onSetWallpaper() {
        //生成一个设置壁纸的请求
        try {
            Intent intent = new Intent();
            ComponentName componentName = new ComponentName("com.android.wallpaper", "com.android.wallpaper.picker.TopLevelPickerActivity");
            intent.setComponent(componentName);
            //发送设置壁纸的请求
            startActivityForResult(intent, 1001);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1001 :
                    setBackground();
                    break;
            }
        }
    }

    //使用WallpaperManager类
    @TargetApi(Build.VERSION_CODES.N)
    private Bitmap getLockWallpaper() {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);//获取WallpaperManager实例
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

        }
        ParcelFileDescriptor mParcelFileDescriptor = wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_SYSTEM);//获取桌面壁纸
        FileDescriptor fileDescriptor = mParcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);//获取Bitmap类型返回值
        try {
            mParcelFileDescriptor.close();
        } catch(Exception e) {
//            android.util.Log.d(TAG,"mParcelFileDescriptor.close() error");
        }
        return image;
    }

    private void setBackground(){ //获取壁纸后设置为view的背景
        try {
            Drawable drawable =new BitmapDrawable(getLockWallpaper());//将Bitmap类型转换为Drawable类型
            llBgHome.setBackgroundDrawable(drawable);//设置背景
//            drawable.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);//设置背景灰度
        } catch(Exception e) {
//            android.util.Log.d(TAG,"set Background fail");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(myReceiver);
        unregisterReceiver(myInstalledReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        appBeanList.clear();
    }
}