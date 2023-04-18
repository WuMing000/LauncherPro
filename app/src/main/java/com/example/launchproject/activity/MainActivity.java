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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
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
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.launchproject.R;
import com.example.launchproject.adapter.RecyclerViewAPPAdapter;
import com.example.launchproject.bean.APPBean;
import com.example.launchproject.helper.PagingScrollHelper;
import com.example.launchproject.manager.HandlerManager;
import com.example.launchproject.manager.HorizontalPageLayoutManager;
import com.example.launchproject.recevier.MusicReceiver;
import com.example.launchproject.recevier.MyInstalledReceiver;
import com.example.launchproject.utils.CustomUtil;
import com.example.launchproject.utils.DataUtil;
import com.example.launchproject.view.DragRecyclerView;
import com.example.launchproject.view.OvalImageView;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
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
public class MainActivity extends BaseActivity implements PagingScrollHelper.onPageChangeListener {

    private static final String TAG = "MainActivity==============>";

    private ViewPager mViewPager;
    private ArrayList<View> mPageView;
    private ImageView ivFirst, ivSecond, ivThird;
    private LinearLayout mImgLayout, llBgHome;
    //    private ListView listViewAPP;
    private DragRecyclerView rvAPPList, rvAPPList2;
    //    private ListViewAPPAdapter adapter;
    private RecyclerViewAPPAdapter recyclerViewAPPAdapter;
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

    MusicReceiver myReceiver;
    private String musicName, date, calendar;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private String spMusicName;
    private int savePosition = -1;
    private APPBean saveFrontAPPContent;
    private APPBean saveMoveAPPContent;
    private int downPosition;

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
                    mImgLayout.removeAllViews();
                    if (appBeanList.size() % 18 == 0) {
                        setIndicatorLayout(appBeanList.size() / 18);
                    } else {
                        setIndicatorLayout((appBeanList.size() / 18) + 1);
                    }
                    recyclerViewAPPAdapter.notifyDataSetChanged();
//                    adapter.notifyDataSetChanged();
                    break;
                case HandlerManager.LOAD_APP_FINISH:
//                    int allAPPNum = (int) msg.obj;
                    while (appBeanList.size() % 18 != 0) {
                        appBeanList.add(new APPBean());
                    }
                    Log.e(TAG, "加载应用完成" + appBeanList.size());
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
//                            appBeanList.remove(i);
                            appBeanList.set(i, new APPBean());
                        }
//                        if ((appBeanList.size() + 1) % 18 == 0) {
//                            for (APPBean appBean : appBeanList) {
//                                if (appBean.getPackageName().length() == 0) {
//                                    appBeanList.remove(appBean);
//                                }
//                            }
//                        }
                        recyclerViewAPPAdapter.notifyDataSetChanged();
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
                                icon = drawableToBitmap(key);
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
                        recyclerViewAPPAdapter.notifyDataSetChanged();
//                        Toast.makeText(MainActivity.this, name + " 安装成功", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //隐藏底部导航栏
//        View decorView = getWindow().getDecorView();
//        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
//        decorView.setSystemUiVisibility(uiOptions);
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
        PagerAdapter pagerAdapter = new PagerAdapter() {

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
                container.removeView(mPageView.get(position));
            }

            //返回一个对象，这个对象表明了PagerAdapter适配器选择哪个对象放在当前的ViewPager中
            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                container.addView(mPageView.get(position));
                return mPageView.get(position);
            }
        };

        mViewPager.setAdapter(pagerAdapter);
        mViewPager.setCurrentItem(0);
        mViewPager.addOnPageChangeListener(new MyOnPageChangeListener());
    }

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
        LayoutInflater layoutInflater = getLayoutInflater();
        Configuration mConfiguration = this.getResources().getConfiguration(); //获取设置的配置信息
        HorizontalPageLayoutManager horizontalPageLayoutManager = null;
        int ori = mConfiguration.orientation; //获取屏幕方向
        if (ori == Configuration.ORIENTATION_LANDSCAPE) {
            //横屏
            view1 = layoutInflater.inflate(R.layout.one_view_pager_landscape, null);
            view2 = layoutInflater.inflate(R.layout.two_view_pager_landscape, null);
            horizontalPageLayoutManager = new HorizontalPageLayoutManager(3, 6);
        } else if (ori == Configuration.ORIENTATION_PORTRAIT) {
            //竖屏
            view1 = layoutInflater.inflate(R.layout.one_view_pager_portrait, null);
            view2 = layoutInflater.inflate(R.layout.two_view_pager_portrait, null);
            horizontalPageLayoutManager = new HorizontalPageLayoutManager(6, 3);
        }
        rvAPPList.setLayoutManager(horizontalPageLayoutManager);
        recyclerViewAPPAdapter.notifyDataSetChanged();
    }

    private void initData() {

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anmi_rotate_view);
        appBeanList = new ArrayList<>();
        llBgHome = findViewById(R.id.ll_bg_home);
        mViewPager = findViewById(R.id.view_pager);
        ivFirst = findViewById(R.id.iv_first);
        ivSecond = findViewById(R.id.iv_second);
        ivThird = findViewById(R.id.iv_third);
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
        initFirstView();
        View view3 = layoutInflater.inflate(R.layout.three_view_pager, null);
//        listViewAPP = view1.findViewById(R.id.list_view_app);
        rvAPPList = view3.findViewById(R.id.rv_app_list);
        rvAPPList2 = view3.findViewById(R.id.rv_app_list_2);
        mImgLayout = view3.findViewById(R.id.indicator_layout);
//        adapter = new ListViewAPPAdapter(this, appBeanList);
        recyclerViewAPPAdapter = new RecyclerViewAPPAdapter(this, appBeanList);
//        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 6, RecyclerView.VERTICAL, false);
        PagingScrollHelper scrollHelper = new PagingScrollHelper();//初始化横向管理器
        scrollHelper.setUpRecycleView(rvAPPList);//将横向布局管理器和recycler view绑定到一起
        HorizontalPageLayoutManager horizontalPageLayoutManager = null;
        if (ori == mConfiguration.ORIENTATION_LANDSCAPE) {
            //横屏
            horizontalPageLayoutManager = new HorizontalPageLayoutManager(3, 6);
        } else if (ori == mConfiguration.ORIENTATION_PORTRAIT) {
            //竖屏
            horizontalPageLayoutManager = new HorizontalPageLayoutManager(6, 3);
        }
        HorizontalPageLayoutManager horizontalPageLayoutManager2 = null;
        if (ori == mConfiguration.ORIENTATION_LANDSCAPE) {
            //横屏
            horizontalPageLayoutManager2 = new HorizontalPageLayoutManager(3, 6);
        } else if (ori == mConfiguration.ORIENTATION_PORTRAIT) {
            //竖屏
            horizontalPageLayoutManager2 = new HorizontalPageLayoutManager(6, 3);
        }
        rvAPPList.setLayoutManager(horizontalPageLayoutManager);
        rvAPPList2.setLayoutManager(horizontalPageLayoutManager2);
        scrollHelper.updateLayoutManger();
        scrollHelper.scrollToPosition(0);//默认滑动到第一页
        rvAPPList.setHorizontalScrollBarEnabled(true);
        rvAPPList2.setHorizontalScrollBarEnabled(true);
        scrollHelper.setOnPageChangeListener(this);//设置滑动监听
        rvAPPList.setAdapter(recyclerViewAPPAdapter);
        rvAPPList2.setAdapter(recyclerViewAPPAdapter);
//        listViewAPP.setAdapter(adapter);
        mPageView = new ArrayList<>();
        mPageView.add(view1);
        mPageView.add(view2);
        mPageView.add(view3);
        new Thread() {
            @Override
            public void run() {
                super.run();
                getAllAppNames();
            }
        }.start();
//        recyclerViewAPPAdapter.setOnClickListener(new RecyclerViewAPPAdapter.OnClickListener() {
//            @Override
//            public void OnClick(int position) {
//                Log.d(TAG, "click recyclerview item " + position);
//                //查询这个应用程序的入口activity。把他开启起来
//                PackageManager pm = getPackageManager();
//                Intent intent = pm.getLaunchIntentForPackage(appBeanList.get(position).getPackageName());
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
//            }
//        });
//        recyclerViewAPPAdapter.setOnTouchListener(new RecyclerViewAPPAdapter.OnTouchListener() {
//            @Override
//            public void OnTouch(View view, MotionEvent motionEvent, int position) {
//                firstPosition = position;
//            }
//        });

        rvAPPList.setOnItemMoveListener(new DragRecyclerView.OnItemMoveListener() {
            @Override
            public void onDown(int position, Handler handler, Runnable runnable) {
                if (appBeanList.get(position).getPackageName().length() == 0) {
                    handler.removeCallbacks(runnable);
                }
                rvAPPList.setVisibility(View.GONE);
                rvAPPList2.setVisibility(View.VISIBLE);
                Log.d(TAG, "onDown====>" + position + "");
//                if (rvAPPList.isDrag()) {
                saveFrontAPPContent = new APPBean(appBeanList.get(position).getAppName(), appBeanList.get(position).getAppIcon(), appBeanList.get(position).getPackageName());
//                    Log.e(TAG, saveFrontAPPContent.toString());
//                }
                downPosition = position;
            }

            @Override
            public void onMove(int x, int y, View v, boolean isCanDrag) {
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
            public void onUp(int position) {
                if (position != downPosition && rvAPPList.isDrag()) {
                    appBeanList.set(downPosition, appBeanList.get(position));
                    appBeanList.set(position, saveFrontAPPContent);
                }
                recyclerViewAPPAdapter.notifyDataSetChanged();
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
            public void onItemClick(int position) {
                if (appBeanList.get(position).getPackageName().length() == 0) {
                    return;
                }
                Log.d(TAG, "onItemClick====>" + position + "");
                Log.d(TAG, "click recyclerview item " + position);
                //查询这个应用程序的入口activity。把他开启起来
                PackageManager pm = getPackageManager();
                Intent intent = pm.getLaunchIntentForPackage(appBeanList.get(position).getPackageName());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initFirstView() {
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

//        llBgHome.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
//                    handler.postDelayed(updateWallpaper, 500);
//                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
//                    handler.removeCallbacks(updateWallpaper);
//                }
//                return true;
//            }
//        });

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

        rvAPPList.setOnUninstallClick(new DragRecyclerView.OnUninstallClick() {
            @Override
            public void OnClick() {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.DELETE");
                intent.setData(Uri.parse("package:" + appBeanList.get(downPosition).getPackageName()));
                Toast.makeText(MainActivity.this, "卸载" + appBeanList.get(downPosition).getAppName(), Toast.LENGTH_LONG).show();
                savePosition = downPosition;
                startActivity(intent);
            }
        });

        rvAPPList.setOnInformationClick(new DragRecyclerView.OnInformationClick() {
            @Override
            public void OnClick() {
                Log.e("onContextItemSelected", appBeanList.get(downPosition).getPackageName());
                gotoAppDetailIntent(MainActivity.this, appBeanList.get(downPosition).getPackageName());
            }
        });

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
        MyInstalledReceiver myInstalledReceiver = new MyInstalledReceiver();
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
            if (CustomUtil.isTouchPointInView(rvAPPList, rawX, rawY)) {
                rvAPPList.removeLongClick();
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
                    || CustomUtil.isTouchPointInView(rvAPPList, rawX, rawY))) {
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
//        runCommand();
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
            byte[] appIconBytes = bitmap2Bytes(drawableToBitmap(drawable));

            if (NotActiveApp(this, packageName)) {
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

    @Override
    public void onPageChange(int index) {
        //这里是配合圆点指示器实现的，可以忽略
        for (int i = 0; i < dotViews.length; i++) {
            if (index == i) {
                dotViews[i].setSelected(true);
                dotViews[i].setImageResource(R.drawable.light_oval);
            } else {
                dotViews[i].setSelected(false);
                dotViews[i].setImageResource(R.drawable.oval);
            }
        }

    }

    private ImageView[] dotViews;//创建图片集合

    /*
        page 是页数 需要自己算 比如我是三行四列 就是每页12个item
        用我们数据的size%12 如不不等于0 就是 （size/12）+1
        如果等于0 就是 size/12
     */
    private void setIndicatorLayout(int page) {
        //生成相应数量的导航小圆点
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        //设置小圆点左右之间的间隔
        params.setMargins(10, 0, 10, 0);
        //得到页面个数
        dotViews = new ImageView[page];//此处传进来的6是页面数，通常要遍历得到页面的数量来创建图片集合
        for (int i = 0; i < page; i++) {//这里也是循环页面数量
            imageView = new ImageView(this);
            imageView.setLayoutParams(params);
            imageView.setImageResource(R.drawable.oval);//设置默认没有选中时所有图片为灰色
            if (i == 0) {
                //默认启动时，选中第一个小圆点
                imageView.setSelected(true);
            } else {
                imageView.setSelected(false);
            }
            //得到每个小圆点的引用，用于滑动页面时，更改它们的状态。
            dotViews[i] = imageView;
            dotViews[0].setImageResource(R.drawable.light_oval);//设置第一个页面选择为黑色
            //添加到布局里面显示
            mImgLayout.addView(imageView);
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    private class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            switch (position) {
                case 0:
                    ivFirst.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.light_oval));
                    ivSecond.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.oval));
                    ivThird.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.oval));
                    break;
                case 1:
                    ivFirst.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.oval));
                    ivSecond.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.light_oval));
                    ivThird.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.oval));
                    break;
                case 2:
                    ivFirst.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.oval));
                    ivSecond.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.oval));
                    ivThird.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.light_oval));
                    break;
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    /**
     * 设置菜单列表项
     * @param menu
     * @param v
     * @param menuInfo
     */
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        setIconEnable(menu, true);
        getMenuInflater().inflate(R.menu.rv_content_menu, menu);
    }

    /**
     * 处理不同的菜单项
     * @param item
     * @return
     */
    @SuppressLint("NonConstantResourceId")
    public boolean onContextItemSelected(MenuItem item) {
        //获取被选择的菜单位置
        int position = recyclerViewAPPAdapter.getPosition();
        Log.e("position", position + "");

        switch (item.getItemId()) {
            case R.id.menu_uninstall://卸载选中项
                //TODO
                Intent intent = new Intent();
                intent.setAction("android.intent.action.DELETE");
                intent.setData(Uri.parse("package:" + appBeanList.get(position).getPackageName()));
                Toast.makeText(MainActivity.this, "卸载" + appBeanList.get(position).getAppName(), Toast.LENGTH_LONG).show();
                savePosition = position;
                startActivity(intent);
//                startActivity(intent);
//                appBeanList.remove(position);
//                recyclerViewAPPAdapter.notifyDataSetChanged();
                break;
            case R.id.menu_information://应用信息选中项
                //TODO
                Log.e("onContextItemSelected", appBeanList.get(position).getPackageName());
                gotoAppDetailIntent(MainActivity.this, appBeanList.get(position).getPackageName());
                break;
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }

    //enable为true时，菜单添加图标有效，enable为false时无效。4.0系统默认无效
    private void setIconEnable(Menu menu, boolean enable) {

        try {
            Class<?> clazz = Class.forName("com.android.internal.view.menu.MenuBuilder");
            Method m = clazz.getDeclaredMethod("setOptionalIconsVisible", boolean.class);
            m.setAccessible(true);
            //MenuBuilder实现Menu接口，创建菜单时，传进来的menu其实就是MenuBuilder对象(java的多态特征)
            m.invoke(menu, enable);
        } catch (Exception e) {
            e.printStackTrace();
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

    public void onSetWallpaper() {
        //生成一个设置壁纸的请求
//        final Intent pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER);
//        Intent chooser = Intent.createChooser(pickWallpaper, "chooser_wallpaper");
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
                    // Log.e("1111111111111", data.getData() + "");
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


    private void runCommand() {
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
        if (intent == null)
            return true;
        return false;
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
        //注意，下面三行代码要用到，否在在View或者surfaceView里的canvas.drawBitmap会看不到图
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

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(myReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        appBeanList.clear();
    }
}