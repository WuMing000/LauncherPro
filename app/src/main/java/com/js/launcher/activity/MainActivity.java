package com.js.launcher.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Instrumentation;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.js.launcher.MyApplication;
import com.js.launcher.R;
import com.js.launcher.adapter.MyGridViewAdapter;
import com.js.launcher.bean.APPBean;
import com.js.launcher.bean.DownBean;
import com.js.launcher.bean.DownProgressBean;
import com.js.launcher.manager.Contact;
import com.js.launcher.manager.HandlerManager;
import com.js.launcher.utils.APPListDataSaveUtils;
import com.js.launcher.utils.CustomUtil;
import com.js.launcher.utils.DataUtil;
import com.js.launcher.view.DragGridView;
import com.js.launcher.view.UpdateDialog;

import java.io.FileDescriptor;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import static android.content.pm.PackageManager.GET_URI_PERMISSION_PATTERNS;

@SuppressLint("LongLogTag")
public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity==============>";

    // 滑动控件
    private ViewPager mViewPager;
    // 用于保存viewPager拥有的子view
    private ArrayList<View> mPageView;
    // 用于更新主背景，实现壁纸功能
    private RelativeLayout llBgHome;
    // 保存APP列表
    private List<APPBean> appBeanList;
    // launcher前两页内容
    View view1, view2;
    //View1 control
    private TextView tvTime, tvCalendar, tvWeek;
    private ImageView ivPicture;
    private EditText etSource;
    private RelativeLayout rlMusic;
    private ImageView ivMusic;
    private TextView tvMusicName, tvMusicSinger;
    private ImageView btnControl, btnPrevious, btnNext;
    private LinearLayout btnPicture, btnProjectionScreen;
    private RelativeLayout btnBrowser, btnAlarm;

    //View2 control
    private RelativeLayout ibVideo, ibMusic, ibTiktok, ibOffice;

    // 用于获取音乐是否正在播放
    private AudioManager audioManager;
    // 用于实现音乐唱片旋转动画
    private Animation animation;

    private String musicName, musicSinger, date, calendar, week;

    private SharedPreferences sp;
    private String spMusicName, spMusicSinger;
    private SharedPreferences.Editor editor;
    // viewPager适配器
    private PagerAdapter pagerAdapter;

    //app gridview
    private int totalPage;//总的页数(仅代表app加载的页数)
    private int mPageSize = 24;//每页显示的最大数量
    // 记录按下的APP信息
    private APPBean saveFrontAPPContent;
    // 记录按下的position
    private int downPosition = -1;
    // 记录是否缩放动画
    private boolean isScale;
    // 记录去除前两页的当前页数
    private static int savePosition = 0;
    // 记录当前页数
    private int copyPageSelectedPosition = -1;
    // 记录是否是gridview
    private boolean isGridView;
    // 数据库工具类，用于保存和获取APP列表
    private APPListDataSaveUtils appListDataSaveUtils;
    // 记录获取数据库的APP列表
    private List<APPBean> appList;

    // 设置底部圆点
    private LinearLayout llPoint;
    private List<View> pointViews;

    // 用于清除壁纸
    private int parentDownX, parentDownY, parentMoveX, parentMoveY;
    // 获取按下的页数
    private int onDownGridPage;

    // 用于设置判断拖动时背景
    private boolean isMoving = true;

    // 记录是否变色
    private boolean isRemoveBg = false;
    // 记录变色背景
    private View removeBgView;

    // 初始化position，用于管理清除平移动画；拖拽的item的position
    private int firstMovePosition, mDragPosition;
    // 初始化view，用于管理清除平移动画；拖拽的item对应的View
    private View mDragView, firstMoveView;
    //平移动画
    private Animation translateAnimation;
    // 用于记录移动position是否变化
    private int moveBgPosition;
    // 用于记录变色的ImageView
    private ImageView saveMoveBgImageView, moveBgImageView;

    private Timer timer;
    private UpdateDialog updateDialog;
    private AlertDialog wallDialog;

    private ProgressBar pbLoading;
    private TextView tvLoading;

    // viewPager页面滑动时回调
    private final ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            Log.e(TAG, ":::" + position);
            try {
                // 页面移动时，底部圆点跟着切换
                if (copyPageSelectedPosition != -1 && llPoint != null) {
                    llPoint.getChildAt(copyPageSelectedPosition).setEnabled(false);
                    llPoint.getChildAt(position).setEnabled(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 记录当前页数
            copyPageSelectedPosition = position;

            // 避免viewPager移动到最后一页，再移动回第一页时，唱片动画消失
            if (position == 0) {
                if (audioManager.isMusicActive()) {
                    handler.sendEmptyMessageAtTime(HandlerManager.MUSIC_PAUSE_UI, 100);
                    ivMusic.startAnimation(animation);//開始动画
                } else {
                    handler.sendEmptyMessageAtTime(HandlerManager.MUSIC_PLAY_UI, 100);
                    ivMusic.clearAnimation();
                }
            }

            // 当是第一页和第二页时，不执行下面内容
            if (position == 0 || position == 1) {
                downPosition = -1;
                return;
            }

            Log.e(TAG, position + ":onPageSelected");
            // 赋值去除第一页和第二页的页数
            savePosition = position - 2;
            // 获取当前gridview
            View view = mPageView.get(position);
            DragGridView gridView = (DragGridView) view;
            // 回调按下、移动、抬起等方法
            gridView.setOnItemMoveListener(new DragGridView.OnItemMoveListener() {
                @Override
                public void onDown(int x, int y, int p, View downView, Handler handler, Runnable runnable) {
                    MyGridViewAdapter myGridViewAdapter = (MyGridViewAdapter) gridView.getAdapter();
                    myGridViewAdapter.setOnImgItemLongClickListener(new MyGridViewAdapter.OnImgItemLongClickListener() {
                        @Override
                        public void onClick(int position) {
                            gridView.createLongClick();
                        }
                    });
                    mDragPosition = p;
                    mDragView = downView;
//                    if (!isCreateLastView) {
//                        for (int i = 0; i < mPageSize; i++) {
//                            appBeanList.add(new APPBean());
//                        }
//                        totalPage = (int) Math.ceil(appBeanList.size() * 1.0 / mPageSize);
//                        DragGridView gridView = (DragGridView) LayoutInflater.from(MainActivity.this).inflate(R.layout.grid_view_app_list, null);
//                        MyGridViewAdapter myGridViewAdapter = new MyGridViewAdapter(MainActivity.this, appBeanList, totalPage - 1, mPageSize);
//                        gridView.setAdapter(myGridViewAdapter);
//                        //每一个GridView作为一个View对象添加到ViewPager集合中
//                        mPageView.add(gridView);
//                        pagerAdapter.notifyDataSetChanged();
//                        isCreateLastView = true;
//                    }
                    // 重新赋值position，因为固定了一个gridview显示18个条目，需要结合页数进行更新
                    int newPosition = p + savePosition * mPageSize;
                    moveBgPosition = newPosition;
                    Log.d("TAG", "onDown:" + newPosition);
                    // 包名为空的位置，不展示长按窗口
                    if (appBeanList.get(newPosition).getPackageName().length() == 0) {
                        handler.removeCallbacks(runnable);
                    }
                    // 包名不为空时，判断是否是系统应用
                    if (appBeanList.get(newPosition).getPackageName().length() != 0) {
                        gridView.isUninstallVisible(CustomUtil.isSystemApplication(MainActivity.this, appBeanList.get(newPosition).getPackageName()));
                    }
                    // 保存按下的APP信息
                    saveFrontAPPContent = new APPBean(appBeanList.get(newPosition).getAppName(), appBeanList.get(newPosition).getAppIconBytes(), appBeanList.get(newPosition).getPackageName());
                    // 保存按下的position
                    downPosition = newPosition;
                    // 保存按下的页数
                    onDownGridPage = savePosition;

                    Log.e(TAG, "===========" + appBeanList.get(downPosition).getPackageName().length());
                    if (appBeanList.get(downPosition).getPackageName().length() == 0) {
                        timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                handler.postDelayed(updateWallpaper2, 100);
                            }
                        }, 800);
                    }
                }

                @Override
                public void isMove(boolean isFingerMove) {
                    Log.e(TAG, isFingerMove + "===========");
                    if (isFingerMove) {
                        // 移动时，取消发送设置壁纸runnable
                        if (timer != null) {
                            timer.cancel();
                        }
                    }
                }

                @Override
                public void onMove(int x, int y, boolean isMove, int p, View moveView) {

//                    Log.d("TAG", "onMove" + isMove);
                    // 重新赋值position，因为固定了一个gridview显示18个条目，需要结合页数进行更新
                    int newPosition = p + savePosition * mPageSize;
                    // 获取屏幕宽度
                    int widthPixels = getResources().getDisplayMetrics().widthPixels;

//                    Log.d("TAG", "widthPixels:" + widthPixels + ",x:" + x);
//                    Log.d("TAG", "childCount：" + gridView.getChildCount());
                    if (x >= (widthPixels - 80)) {
//                        Log.e(TAG, savePosition + ":savePosition");
                        // 手指移动到右边缘，右边缘窗口设置灰色背景
                        if (copyPageSelectedPosition < mPageView.size() - 1) {
                            mPageView.get(copyPageSelectedPosition + 1).setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.selector_gridview_bg_solid));
                            // 记录变色背景
                            removeBgView = mPageView.get(copyPageSelectedPosition + 1);
                        }
                        // 记录是否变色
                        isRemoveBg = true;
                        // 1s后发送切换下一页handler
                        handler.postDelayed(nextRunnable, 500);
                    } else if (x <= 80) {
                        // 手指移动到左边缘，左边缘窗口设置灰色背景
                        if (copyPageSelectedPosition > 2) {
                            mPageView.get(copyPageSelectedPosition - 1).setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.selector_gridview_bg_solid));
                            // 记录变色背景
                            removeBgView = mPageView.get(copyPageSelectedPosition - 1);
                        }
                        // 记录是否变色
                        isRemoveBg = true;
                        // 1s后发送切换上一页handler
                        handler.postDelayed(previousRunnable, 500);
                    } else {
                        if (isRemoveBg && removeBgView != null) {
                            // 还原背景，手指不在边缘时，去除跳转下一页或者上一页
                            removeBgView.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.selector_gridview_bg_stroke));
                            isRemoveBg = false;
                            handler.removeCallbacks(nextRunnable);
                            handler.removeCallbacks(previousRunnable);
                        }
                    }

                    if (p == mDragPosition) {
                        // 初始化
                        firstMovePosition = 0;
                    }
                    if (p != firstMovePosition && firstMoveView != null) {
                        // 移动到新的position，去除上一个平移动画
                        firstMoveView.clearAnimation();
                    }

                    // 按下和移动两个position不一致才执行平移动画
                    if (p != mDragPosition && p != firstMovePosition && moveView != null && gridView.isDrag() && appBeanList.get(newPosition).getPackageName().length() != 0) {
                        // 每次平移重新赋值
                        firstMovePosition = p;
                        firstMoveView = moveView;
//                    Log.d(TAG, "update:moveX " + mMoveView.getX() + ", moveY " + mMoveView.getY() + "frontX " + mDragView.getX() + ", frontY " + mDragView.getY());
                        if (moveView.getX() == mDragView.getX()) {
                            translateAnimation = new TranslateAnimation(0, 0, 0, mDragView.getY() - moveView.getY());//Y平移动画
                            translateAnimation.setDuration(300);
                            translateAnimation.setFillAfter(true);
                            firstMoveView.startAnimation(translateAnimation);//给imageView添加的动画效果
                        } else if (moveView.getY() == mDragView.getY()) {
                            translateAnimation = new TranslateAnimation(0, mDragView.getX() - moveView.getX(), 0, 0);//X平移动画
                            translateAnimation.setDuration(300);
                            translateAnimation.setFillAfter(true);
                            firstMoveView.startAnimation(translateAnimation);//给imageView添加的动画效果
                        } else {
                            translateAnimation = new TranslateAnimation(0, mDragView.getX() - moveView.getX(), 0, mDragView.getY() - moveView.getY());//X、Y平移动画
                            translateAnimation.setDuration(300);
                            translateAnimation.setFillAfter(true);
                            firstMoveView.startAnimation(translateAnimation);//给imageView添加的动画效果
                        }
                    }

                    // 增加选中效果
                    // 获取移动选中的图标
                    DragGridView view3 = (DragGridView) mPageView.get(copyPageSelectedPosition);
                    LinearLayout childAt = (LinearLayout) view3.getChildAt(p);
                    moveBgImageView = childAt.findViewById(R.id.iv_app_icon);
//                    Log.e(TAG, "moveBgPosition:" + moveBgPosition + ",newPosition:" + newPosition);
                    // 移动到的position发生变更
                    if (moveBgPosition != newPosition) {
                        // 更新移动position
                        moveBgPosition = newPosition;
                        if (appBeanList.get(newPosition).getPackageName().length() != 0 && onDownGridPage != savePosition) {
                            // 移动到的位置有数据，且不在当前页设置黑度
                            moveBgImageView.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.picture_color_black_80), PorterDuff.Mode.SRC_ATOP);
                        } else if (appBeanList.get(newPosition).getPackageName().length() == 0){
                            // 移动到的位置没有数据，设置背景和透明度
                            moveBgImageView.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.selector_gridview_bg_solid));
                            moveBgImageView.setAlpha(0.3f);
                        }
                        if (saveMoveBgImageView != null) {
                            // position变化时，还原变色的图标
                            saveMoveBgImageView.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.transparent));
                            saveMoveBgImageView.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.picture_color_black_20), PorterDuff.Mode.SRC_ATOP);
                        }
                        // 随着移动变更
                        saveMoveBgImageView = moveBgImageView;
                    }

                    if (isMove && !isScale && isMoving) {
                        // 设置viewPager透明度80%
                        mViewPager.setAlpha(0.8f);

//                        for (int i = 0; i < mPageView.size(); i++) {
//                            if (i > 1) {
//                                DragGridView view2 = (DragGridView) mPageView.get(i);
//                                view2.setBackground(getResources().getDrawable(R.drawable.selector_gridview_bg_stroke));
//                            }
//                        }

                        // 优化设置背景
                        if (copyPageSelectedPosition > 2 && copyPageSelectedPosition < mPageView.size() - 1) {
                            mPageView.get(copyPageSelectedPosition - 1).setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.selector_gridview_bg_stroke));
                            mPageView.get(copyPageSelectedPosition).setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.selector_gridview_bg_stroke));
                            mPageView.get(copyPageSelectedPosition + 1).setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.selector_gridview_bg_stroke));
                        } else if (copyPageSelectedPosition == 2) {
                            mPageView.get(copyPageSelectedPosition).setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.selector_gridview_bg_stroke));
                            mPageView.get(copyPageSelectedPosition + 1).setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.selector_gridview_bg_stroke));
                        } else if (copyPageSelectedPosition == mPageView.size() - 1) {
                            mPageView.get(copyPageSelectedPosition - 1).setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.selector_gridview_bg_stroke));
                            mPageView.get(copyPageSelectedPosition).setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.selector_gridview_bg_stroke));
                        }

                        // 设置viewPager外边距
                        RelativeLayout.LayoutParams marginLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                        marginLayoutParams.setMargins(0, 100, 0, 100);
                        mViewPager.setLayoutParams(marginLayoutParams);
                        // 设置允许子view跨越另一个子view
                        mViewPager.setClipToPadding(false);

                        // 标记动画，拖动时只执行一次
                        isScale = true;
                        isMoving = false;
                    }
                }

                @Override
                public void onCancel() {

                    // 移动时，取消发送设置壁纸runnable
                    if (timer != null) {
                        timer.cancel();
                    }

                    // 拖动异常时，还原背景
                    if (isScale && !isMoving) {
                        for (int i = 0; i < mPageView.size(); i++) {
                            if (i > 1) {
                                Log.e(TAG, "i:" + i);
                                DragGridView view2 = (DragGridView) mPageView.get(i);
                                view2.setBackgroundColor(getResources().getColor(R.color.transparent));
                            }
                        }
                        isScale = false;
                        isMoving = true;
                        handler.postDelayed(clearAnimationRunnable, 500);
                        mViewPager.setClipToPadding(true);
                    }
                }

                @Override
                public void onUp(int p, Animation transAnimation) {

                    // 移动时，取消发送设置壁纸runnable
                    if (timer != null) {
                        timer.cancel();
                    }

                    // 手指抬起时，还原移动到ImageView的背景
                    if (moveBgImageView != null) {
                        moveBgImageView.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.transparent));
                        moveBgImageView.setAlpha(1.0f);
                        moveBgImageView.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.picture_color_black_20), PorterDuff.Mode.SRC_ATOP);
                    }
                    // 手指抬起时，去除平移动画
                    if (translateAnimation != null) {
                        translateAnimation.cancel();
                    }
                    // 更新手指抬起时position，根据页数和页个数更新
                    int newPosition = p + savePosition * mPageSize;
                    Log.d("TAG", "onUp:downPosition:" + downPosition + ",position:" + newPosition + ",saveFrontAPPContent:" + saveFrontAPPContent + ",moveData:" + appBeanList.get(newPosition));
                    // 标记当前页APP个数是否已满
                    boolean isScreenAPPFull = true;
                    Log.d(TAG, "=================newPosition:" + newPosition + ",downPosition:" + downPosition);
                    // 点击和抬起position不一致和已拖动执行
                    if (newPosition != downPosition && gridView.isMove()) {
                        if (onDownGridPage == savePosition) {
                            // 按下和抬起页数一致情况

                            // item位置互换
                            appBeanList.set(downPosition, appBeanList.get(newPosition));
                            appBeanList.set(newPosition, saveFrontAPPContent);
                            // 刷新界面
                            pagerAdapter.notifyDataSetChanged();
                            new Thread() {
                                @Override
                                public void run() {
                                    super.run();
                                    // 更新数据库
                                    appListDataSaveUtils.setDataList("appList", appBeanList);
                                }
                            }.start();
                        } else {
                            // 按下和抬起页数不一致情况

                            // 遍历当前页APP个数是否已满
                            for (int j = savePosition * mPageSize; j < (savePosition + 1) * mPageSize; j++) {
                                if (appBeanList.get(j).getPackageName().length() == 0) {
                                    // 不满设置false
                                    isScreenAPPFull = false;
                                }
                            }
                            if (isScreenAPPFull) {
                                // 当前页APP个数已满
                                Toast.makeText(MainActivity.this, "当前屏幕APP个数已满", Toast.LENGTH_SHORT).show();
                                // 把点击时保存的APP信息设置回原来位置
                                appBeanList.set(downPosition, saveFrontAPPContent);
                            } else {
                                if (appBeanList.get(newPosition).getPackageName().length() != 0) {
                                    // 替换的item位置数据不为空
                                    // 用弹出框提示是否替换
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                    builder.setMessage(saveFrontAPPContent.getAppName() + " 将和 " + appBeanList.get(newPosition).getAppName() + " 位置进行对换，是否继续？")
                                            .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    // item位置互换
                                                    appBeanList.set(downPosition, appBeanList.get(newPosition));
                                                    appBeanList.set(newPosition, saveFrontAPPContent);
                                                    // 刷新页面
                                                    pagerAdapter.notifyDataSetChanged();
                                                    new Thread() {
                                                        @Override
                                                        public void run() {
                                                            super.run();
                                                            // 更新数据库
                                                            appListDataSaveUtils.setDataList("appList", appBeanList);
                                                        }
                                                    }.start();
                                                }
                                            })
                                            .setNegativeButton("否", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    // 取消弹出框
                                                    dialogInterface.dismiss();
                                                }
                                            }).show();
                                } else {
                                    // 替换的item位置数据为空，直接替换
                                    // item位置互换
                                    appBeanList.set(downPosition, appBeanList.get(newPosition));
                                    appBeanList.set(newPosition, saveFrontAPPContent);
                                    // 刷新页面
                                    pagerAdapter.notifyDataSetChanged();
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            super.run();
                                            // 更新数据库
                                            appListDataSaveUtils.setDataList("appList", appBeanList);
                                        }
                                    }.start();
                                }
                            }
                        }
                    }
                    // 抬起时发现已有动画变化
                    if (isScale) {
                        // remove下一页和上一页的runnable，避免抬起时还是会触发跳转
                        handler.removeCallbacks(nextRunnable);
                        handler.removeCallbacks(previousRunnable);
                        // 把背景还原
                        for (int i = 0; i < mPageView.size(); i++) {
                            if (i > 1) {
                                DragGridView view2 = (DragGridView) mPageView.get(i);
                                view2.setBackgroundColor(getResources().getColor(R.color.transparent));
                            }
                        }
                        // 发送handler，清除动画
                        handler.postDelayed(clearAnimationRunnable, 500);
                        // 不允许子view跨越
                        mViewPager.setClipToPadding(true);
                        // 标记没发生动画，给下次拖动继续沿用
                        isScale = false;
                        isMoving = true;
                    }
                }

                @Override
                public void onItemClick(int p) {
                    // 更新手指点击时position，根据页数和页个数更新
//                    int newPosition = p + savePosition * mPageSize;
//                    // 当position为-1或者item中无数据不执行下面内容
//                    if (p == -1 || appBeanList.get(newPosition).getPackageName().length() == 0) {
//                        return;
//                    }
//                    Log.d("TAG", "onItemClick====>" + newPosition + "");
//                    //查询这个应用程序的入口activity。把他开启起来
//                    try {
//                        PackageManager pm = getPackageManager();
//                        Intent intent = pm.getLaunchIntentForPackage(appBeanList.get(newPosition).getPackageName());
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        startActivity(intent);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                }
            });

            // 根据当前页获取当前gridview
            DragGridView dragGridView = (DragGridView) mPageView.get(copyPageSelectedPosition);
            // 卸载点击事件
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
            // 应用信息点击事件
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
                    // 首次启动launcher，加载数据
                    if (appList.size() == 0) {
                        Bundle data = msg.getData();
                        byte[] appIcons = data.getByteArray("appIcon");
                        String appName = data.getString("appName");
                        String packageName = data.getString("packageName");
                        Bitmap bitmap = BitmapFactory.decodeByteArray(appIcons, 0, appIcons.length);
//                        Drawable drawable = new BitmapDrawable(getResources(), bitmap);
//                        appBeanList.add(new APPBean(appName, bitmap, packageName));
                        // 把获取到的APP数据添加进集合
                        appBeanList.add(new APPBean(appName, appIcons, packageName));
                    }
                    break;
                case HandlerManager.LOAD_APP_FINISH:
                    // 首次启动launcher，加载数据
                    if (appList.size() == 0) {
                        // 最后页不满18个数，增加空数据填充，用于应用移动
                        while (appBeanList.size() % 24 != 0) {
                            appBeanList.add(new APPBean());
                        }
                        Log.d(TAG, "加载应用完成" + appBeanList.size());
                        handler.sendEmptyMessageAtTime(HandlerManager.SHOW_APP_LIST, 100);
                        // 把所有APP添加进数据库
                        new Thread() {
                            @Override
                            public void run() {
                                super.run();
                                // 更新数据库
                                appListDataSaveUtils.setDataList("appList", appBeanList);
                            }
                        }.start();
                    }
                    break;
                case HandlerManager.SHOW_APP_LIST:
                    // 数据库有值时，直接赋值
                    if (appList.size() != 0) {
                        appBeanList = appList;
                    }
                    // add app list gridview
                    // 总的页数，取整（这里有三种类型：Math.ceil(3.5)=4:向上取整，只要有小数都+1  Math.floor(3.5)=3：向下取整  Math.round(3.5)=4:四舍五入）
                    totalPage = (int) Math.ceil(appBeanList.size() * 1.0 / mPageSize);
                    LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                    for(int i = 0; i < totalPage; i++){
                        // 每个页面都是inflate出一个新实例
                        // 将集合数据分在gridview上，每个gridview显示18条数据
                        DragGridView gridView = (DragGridView) inflater.inflate(R.layout.grid_view_app_list, null);
                        MyGridViewAdapter myGridViewAdapter = new MyGridViewAdapter(MainActivity.this, appBeanList, i, mPageSize);
                        Configuration mConfiguration = MyApplication.getInstance().getContext().getResources().getConfiguration(); //获取设置的配置信息
                        int ori = mConfiguration.orientation; //获取屏幕方向
                        if (ori == Configuration.ORIENTATION_LANDSCAPE) {
                            // 横屏时，设置六列
                            gridView.setNumColumns(6);
                        } else if (ori == Configuration.ORIENTATION_PORTRAIT) {
                            // 竖屏时，设置三列
                            gridView.setNumColumns(4);
                        }
                        // 设置gridview适配器
                        gridView.setAdapter(myGridViewAdapter);
                        //每一个GridView作为一个View对象添加到ViewPager集合中
                        mPageView.add(gridView);
                        // 刷新viewPager
                        pagerAdapter.notifyDataSetChanged();
                        pbLoading.setVisibility(View.GONE);
                        tvLoading.setVisibility(View.GONE);
                    }
                    // 获取底部圆点数据
                    getPointData();
                    //第一次显示小白点
                    llPoint.getChildAt(0).setEnabled(true);
                    // 设置默认显示第一页
                    mViewPager.setCurrentItem(0);
                    break;
                case HandlerManager.GET_SYSTEM_TIME:
                    // 更新时间，一秒发送
                    handler.postDelayed(updateTimeThread, 1000);
                    break;
                case HandlerManager.HOME_LONG_CLICK :
                    // 设置壁纸
                    wallDialog = new AlertDialog.Builder(MainActivity.this)
                            .setMessage("是否设置壁纸")
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
                                    handler.removeCallbacks(updateWallpaper);
                                }
                            })
                            .show();
                    break;
                case HandlerManager.MUSIC_PLAY_UI :
                    // 更新暂停按钮
                    btnControl.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.btn_player_play_normal));
                    break;
                case HandlerManager.MUSIC_PAUSE_UI :
                    // 更新播放按钮
                    btnControl.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.btn_player_pause_normal));
                    break;
                case HandlerManager.MUSIC_INFORMATION_UPDATE:
                    Bundle bundle = (Bundle) msg.obj;
                    String trackName = bundle.getString("trackName");
                    String artist = bundle.getString("artist");
                    musicName = trackName;
                    musicSinger = artist;
                    // 设置歌名和歌手
                    tvMusicName.setText(musicName);
                    tvMusicSinger.setText(artist);
                    // 保存到数据库
                    editor = sp.edit();
                    editor.putString("musicName", musicName);
                    editor.putString("musicSinger", musicSinger);
                    editor.commit();
                    break;
                case HandlerManager.REMOVED_APP_SUCCESS:
                    // 卸载应用
                    try {
                        int i = -1;
                        boolean isLastALLNone = true;
                        // 获取卸载应用的包名
                        String obj = (String) msg.obj;
                        String removePkg = obj.split(":")[1];
                        // 获取卸载的包名位于集合哪个位置
                        for (APPBean appBean : appBeanList) {
                            if (removePkg.equals(appBean.getPackageName())) {
                                i = appBeanList.indexOf(appBean);
                                Log.d(TAG, "removePkg:" + removePkg + ",i:" + i);
                            }
                        }
                        // 把该位置数据设置为空
                        if (i != -1) {
                            appBeanList.set(i, new APPBean());
                        }
                        // 遍历获取该应用是否最后一页的最后一个应用
                        for (int j = appBeanList.size() - 24; j < appBeanList.size(); j++) {
                            if(appBeanList.get(j).getPackageName().length() != 0) {
                                // 标记非最后一个应用
                                isLastALLNone = false;
                            }
                        }
                        Log.e(TAG, "remove:" + appBeanList.size() + ",isLastALLNone:" + isLastALLNone);
                        if (isLastALLNone) {
                            // 是最后一页最后一个应用情况
                            int totalSize = appBeanList.size() - 24;
                            // 把最后一页的所有空数据从集合中删除
                            for (int j = appBeanList.size() - 1; j >= totalSize; j--) {
                                Log.d(TAG, "j: " + j);
                                appBeanList.remove(j);
                            }
                            // 删除最后一页
                            mPageView.remove(copyPageSelectedPosition);
                        }
                        // 刷新viewPager页面
                        pagerAdapter.notifyDataSetChanged();
                        // 同步删除最后一页的底部圆点
                        if (isLastALLNone) {
                            llPoint.removeView(pointViews.get(mPageView.size()));
                        }
                        // 更新数据库
                        appListDataSaveUtils.setDataList("appList", appBeanList);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case HandlerManager.INSTALL_APP_SUCCESS:
                    try {
                        byte[] iconBytes = null;
                        String name = null;
                        int installPosition = -1;
                        // 获取安装的应用包名
                        String obj1 = (String) msg.obj;
                        String installPkg = obj1.split(":")[1];
                        int firstNullPosition = -1;
                        Log.e(TAG, installPkg);
                        // 根据包名获取应用图标和应用名称
                        Map<Drawable, String> iconANDAppName = CustomUtil.getIconANDAppName(installPkg);
                        if (iconANDAppName != null) {
                            Set<Map.Entry<Drawable, String>> entries = iconANDAppName.entrySet();
                            for (Map.Entry<Drawable, String> entry : entries) {
                                Drawable key = entry.getKey();
                                name = entry.getValue();
                                Log.d(TAG, "appName:" + name);
                                Bitmap bitmap = CustomUtil.drawableToBitmap(key);
                                iconBytes = CustomUtil.bitmap2Bytes(bitmap);
                            }
                        }
                        // 根据包名、应用图标、应用名称，新建APP实体类
                        APPBean installAPPBean = new APPBean(name, iconBytes, installPkg);
                        // 判断集合中是否已经存在
                        boolean isAdd = appBeanList.contains(installAPPBean);
                        // 从集合一开始开始寻找，找出集合首个空数据的位置
                        for (int i = 0; i < appBeanList.size(); i++) {
                            if (appBeanList.get(i).getPackageName().length() == 0) {
                                firstNullPosition = i;
                                break;
                            }
                        }
                        Log.e(TAG, "isADD:" + isAdd);
                        // 集合中没有该数据，避免重复添加
                        if (!isAdd) {
                            if (firstNullPosition != -1) {
                                // 集合中有空数据，设置新安装APP到该位置
                                appBeanList.set(firstNullPosition, installAPPBean);
                                // 获取安装的页数
                                installPosition = (firstNullPosition / 24 + 1) + 1;
                                Log.d(TAG, "setInstallAPP:" + installPosition);
                            } else {
                                // 集合中没有空数据，直接添加新数据进集合
                                appBeanList.add(installAPPBean);
                                // 获取安装的页数
                                installPosition = mPageView.size();
                                Log.d(TAG, "addInstallAPP:" + installPosition);
                            }
                            Toast.makeText(MainActivity.this, name + " 安装成功", Toast.LENGTH_SHORT).show();
                        }
                        // 把未满18个数的gridview，用空数据填满
                        while (appBeanList.size() % 24 != 0) {
                            appBeanList.add(new APPBean());
                        }
                        if (firstNullPosition == -1) {
                            Log.e(TAG, "appList:" + appBeanList.size());
                            totalPage = (int) Math.ceil(appBeanList.size() * 1.0 / mPageSize);
                            // 添加新页面到viewPager
                            DragGridView gridView = (DragGridView) LayoutInflater.from(MainActivity.this).inflate(R.layout.grid_view_app_list, null);
                            MyGridViewAdapter myGridViewAdapter = new MyGridViewAdapter(MainActivity.this, appBeanList, totalPage - 1, mPageSize);
                            Configuration mConfiguration = MyApplication.getInstance().getContext().getResources().getConfiguration(); //获取设置的配置信息
                            int ori = mConfiguration.orientation; //获取屏幕方向
                            if (ori == Configuration.ORIENTATION_LANDSCAPE) {
                                // 横屏，列数六个
                                gridView.setNumColumns(6);
                            } else if (ori == Configuration.ORIENTATION_PORTRAIT) {
                                // 竖屏，列数三个
                                gridView.setNumColumns(4);
                            }
                            gridView.setAdapter(myGridViewAdapter);
                            //每一个GridView作为一个View对象添加到ViewPager集合中
                            mPageView.add(gridView);
                        }
                        // 刷新viewPager页面
                        pagerAdapter.notifyDataSetChanged();
                        // 获取底部圆点
                        getPointData();
                        // 更新数据库
                        appListDataSaveUtils.setDataList("appList", appBeanList);
                        // 设置当前页面
                        mViewPager.setCurrentItem(installPosition);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case HandlerManager.CLEAR_RECYCLER_ANIMATION:
                    // 还原拖动时产生的动画和变化
                    // 还原外边距
                    RelativeLayout.LayoutParams marginLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    marginLayoutParams.setMargins(0, 0, 0, 0);
                    mViewPager.setLayoutParams(marginLayoutParams);
                    mViewPager.setClipChildren(false);
                    // 还原透明度
                    mViewPager.setAlpha(1.0f);
                    break;
                case HandlerManager.SKIP_NEXT_PAGE:
                    // 手指移动到边缘，跳转下一页
                    if (copyPageSelectedPosition < mPageView.size() - 1) {
                        // 跳转时，还原填充的背景
                        mPageView.get(copyPageSelectedPosition + 1).setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.selector_gridview_bg_stroke));
                    }
                    // 跳转下一页
                    mViewPager.setCurrentItem(copyPageSelectedPosition + 1, true);
                    // 清除runnable，重新使用
                    handler.removeCallbacks(nextRunnable);
                    break;
                case HandlerManager.SKIP_PREVIOUS_PAGE:
                    if (copyPageSelectedPosition > 2) {
                        // 跳转时，还原填充的背景
                        mPageView.get(copyPageSelectedPosition - 1).setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.selector_gridview_bg_stroke));
                        // 跳转上一页
                        mViewPager.setCurrentItem(copyPageSelectedPosition - 1, true);
                        // 清除runnable，重新使用
                        handler.removeCallbacks(previousRunnable);
                    }
                    break;
                case HandlerManager.NETWORK_NO_CONNECT:
//                    Toast.makeText(MainActivity.this, "网络未连接，请先连接网络", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "网络未连接，请先连接网络");
                    break;
                case HandlerManager.UPDATE_VERSION_DIFFERENT:
                    Log.e(TAG, "版本号不一致");
                    updateDialog = new UpdateDialog(MainActivity.this);
                    updateDialog.setMessage("桌面有新版本！！！");
                    updateDialog.setTitleVisible(View.GONE);
                    updateDialog.setExitOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            updateDialog.dismiss();
                        }
                    });
                    updateDialog.setUpdateOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            updateDialog.setProgressVisible(View.VISIBLE);
                            updateDialog.setButtonVisible(View.GONE);
                            DownBean downBean = CustomUtil.updateAPK(Contact.SERVER_URL + ":8080/test/js_project/launcher/js_launcher.apk");
                            Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    DownProgressBean downProgressBean = CustomUtil.updateProgress(downBean.getDownloadId(), timer);
                                    Log.e(TAG, downProgressBean.getProgress());
                                    float progress = Float.parseFloat(downProgressBean.getProgress());
                                    if (progress == 100.00) {
                                        updateDialog.dismiss();
                                    }
                                    updateDialog.setPbProgress((int) progress);
                                    updateDialog.setTvProgress(downProgressBean.getProgress());
                                }
                            }, 0, 1000);
                        }
                    });
                    updateDialog.setCancelable(false);
                    updateDialog.show();
                    break;
                case HandlerManager.UPDATE_VERSION_SAME:
                    Log.e(TAG, "版本号一致");
                    break;
                default:
                    Log.e(TAG, "It's not send handler message.");
                    break;
            }
        }
    };

    // 设置时间，日期的runnable
    Runnable updateTimeThread = new Runnable() {

        public void run() {
            // TODO Auto-generated method stub
            SimpleDateFormat df = new SimpleDateFormat("HH:mm");
            date = df.format(new java.util.Date());
            tvTime.setText(date);
            calendar = DataUtil.StringCalendar();
            tvCalendar.setText(calendar);
            week = DataUtil.StringWeek();
            tvWeek.setText(week);
        }
    };

    // 设置壁纸的runnable
    Runnable updateWallpaper = new Runnable() {

        public void run() {
            handler.sendEmptyMessageAtTime(HandlerManager.HOME_LONG_CLICK, 500);
        }
    };

    Runnable updateWallpaper2 = new Runnable() {

        public void run() {
            handler.sendEmptyMessageAtTime(HandlerManager.HOME_LONG_CLICK, 500);
        }
    };

    // 还原动画的runnable
    Runnable clearAnimationRunnable = new Runnable() {

        public void run() {
            handler.sendEmptyMessageAtTime(HandlerManager.CLEAR_RECYCLER_ANIMATION, 100);
        }
    };

    // 跳转下一页的runnable
    Runnable nextRunnable = new Runnable() {
        @Override
        public void run() {
            handler.sendEmptyMessageAtTime(HandlerManager.SKIP_NEXT_PAGE, 100);
        }
    };

    // 跳转上一页的runnable
    Runnable previousRunnable = new Runnable() {
        @Override
        public void run() {
            handler.sendEmptyMessageAtTime(HandlerManager.SKIP_PREVIOUS_PAGE, 100);
        }
    };

    // 屏幕翻转时，调用
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState");
        // 把日期、时间、歌名、歌手信息保存起来
        outState.putString("musicName", musicName);
        outState.putString("musicSinger", musicSinger);
        outState.putString("date", date);
        outState.putString("calendar", calendar);
        outState.putString("week", week);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏底部导航栏
//        CustomUtil.hideBottomUIMenu(this);
        setContentView(R.layout.activity_main);
        CustomUtil.hideNavigationBar(this);
//        if (!CustomUtil.isNotificationListenerEnabled(this)) {//是否开启通知使用权
//            CustomUtil.openNotificationListenSettings(this);
//        }
        // 设置全局handler
        HandlerManager.putHandler(handler);
        appBeanList = new ArrayList<>();
        LayoutInflater layoutInflater = getLayoutInflater();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anmi_rotate_view);
        Configuration mConfiguration = this.getResources().getConfiguration(); //获取设置的配置信息
        int ori = mConfiguration.orientation; //获取屏幕方向
        if (ori == Configuration.ORIENTATION_LANDSCAPE) {
            //横屏
            view1 = layoutInflater.inflate(R.layout.one_view_pager_landscape, null);
            view2 = layoutInflater.inflate(R.layout.two_view_pager_landscape, null);
        } else if (ori == Configuration.ORIENTATION_PORTRAIT) {
            //竖屏
            view1 = layoutInflater.inflate(R.layout.one_view_pager_portrait, null);
            view2 = layoutInflater.inflate(R.layout.two_view_pager_portrait, null);
        }
        llBgHome = findViewById(R.id.ll_bg_home);
        mViewPager = findViewById(R.id.view_pager);
        pbLoading = findViewById(R.id.pb_loading);
        tvLoading = findViewById(R.id.tv_loading);
        llPoint = findViewById(R.id.ll_point);
        tvTime = view1.findViewById(R.id.tv_time);
        tvCalendar = view1.findViewById(R.id.tv_calendar);
        tvWeek = view1.findViewById(R.id.tv_week);
        handler.sendEmptyMessageAtTime(HandlerManager.GET_SYSTEM_TIME, 100);
        ivPicture = view1.findViewById(R.id.iv_picture);
        etSource = view1.findViewById(R.id.et_source);
        rlMusic = view1.findViewById(R.id.rl_music);
        ivMusic = view1.findViewById(R.id.iv_music);
        tvMusicName = view1.findViewById(R.id.tv_music_name);
        tvMusicSinger = view1.findViewById(R.id.tv_music_singer);
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
        mPageView = new ArrayList<>();
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
                // 使用object，不使用mPageView.get(position)，避免出现越界
                container.removeView((View) object);
            }

            //返回一个对象，这个对象表明了PagerAdapter适配器选择哪个对象放在当前的ViewPager中
            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                container.addView(mPageView.get(position));
                return mPageView.get(position);
            }

            // 定义该方法，避免无法页面刷新
            @Override
            public int getItemPosition(Object object) {
                return POSITION_NONE;
            }
        };
        // 设置页面间的外边距
        mViewPager.setPageMargin(15);
        // 设置页面缓冲
        mViewPager.setOffscreenPageLimit(10);
        mViewPager.setAdapter(pagerAdapter);

        new Thread() {
            @Override
            public void run() {
                super.run();
                // 使用数据库获取APP信息
                sp = getSharedPreferences("home_save_data", MODE_PRIVATE);
                appListDataSaveUtils = new APPListDataSaveUtils(MainActivity.this, "app_list_data");
                appList = appListDataSaveUtils.getDataList("appList");
                initData();
                // 旋转屏幕时，使用旋转保存的数据重新设置
                if (savedInstanceState != null) {
                    String musicNameSave = savedInstanceState.getString("musicName");
                    String musicSingerSave = savedInstanceState.getString("musicSinger");
                    String dateSave = savedInstanceState.getString("date");
                    String calendarSave = savedInstanceState.getString("calendar");
                    String weekSave = savedInstanceState.getString("week");
                    musicName = musicNameSave;
                    musicSinger = musicSingerSave;
                    date = dateSave;
                    calendar = calendarSave;
                    week = weekSave;
                    tvMusicName.setText(musicNameSave);
                    tvMusicSinger.setText(musicSingerSave);
                    tvTime.setText(dateSave);
                    tvCalendar.setText(calendarSave);
                    tvWeek.setText(weekSave);
                }
                // 使用数据库，设置歌名和歌手
                spMusicName = sp.getString("musicName", "暂无歌名");
                tvMusicName.setText(spMusicName);
                spMusicSinger = sp.getString("musicSinger", "暂无歌手");
                tvMusicSinger.setText(spMusicSinger);
            }
        }.start();

        new Thread() {
            @Override
            public void run() {
                super.run();
                String serverFile = CustomUtil.getServerFile(Contact.SERVER_URL + ":8080/test/js_project/launcher/Version.txt");
                if (serverFile.length() == 0) {
                    handler.sendEmptyMessageAtTime(HandlerManager.NETWORK_NO_CONNECT, 100);
                    return;
                }
                String localVersionName = CustomUtil.getLocalVersionName();
                if (localVersionName.equals(serverFile)) {
                    handler.sendEmptyMessageAtTime(HandlerManager.UPDATE_VERSION_SAME, 100);
                } else {
                    handler.sendEmptyMessageAtTime(HandlerManager.UPDATE_VERSION_DIFFERENT, 100);
                }
            }
        }.start();

    }

    private void initData() {

        initClickListener();

        // 设置个性化字体
        AssetManager mgr = getAssets();
        Typeface tf = Typeface.createFromAsset(mgr, "fonts/Gilroy-Thin-13.otf");
        tvTime.setTypeface(tf);

        // 使用子线程获取APP列表
        new Thread() {
            @Override
            public void run() {
                super.run();
                getAllAppNames();
            }
        }.start();

        mPageView.add(view1);
        mPageView.add(view2);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pagerAdapter.notifyDataSetChanged();
            }
        });
        // 获取数据不为空，发送handler
        if (appList.size() != 0) {
            handler.sendEmptyMessageAtTime(HandlerManager.SHOW_APP_LIST, 100);
            Log.d(TAG, appList.toString());
        }

        // 添加页面滑动监听
        mViewPager.addOnPageChangeListener(onPageChangeListener);
//        mViewPager.setPageTransformer(true, new MyGalleyPageTransformer());
        // 设置默认第一页
        onPageChangeListener.onPageSelected(0);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initClickListener() {
        // View1 listener
        // 相册跳转
        ivPicture.setOnClickListener(view -> {
            Log.e("ivPicture=====>", "onClick");
            try {
                Intent intent = new Intent();
                ComponentName componentNameGallery = new ComponentName("com.js.photoalbum", "com.js.photoalbum.activity.SlideActivity");
                intent.setComponent(componentNameGallery);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_from_bottom, 0);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "系统中暂无该应用", Toast.LENGTH_SHORT).show();
            }
        });
        // 浏览器搜索跳转
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
                return false;
            }
        });
        // 相册跳转
        btnPicture.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_btn_small);
                btnPicture.startAnimation(animation);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP && CustomUtil.isTouchPointInView(btnPicture, (int) motionEvent.getRawX(), (int) motionEvent.getRawY()) && mViewPager.getCurrentItem() == 0) {
                Log.e(TAG, "picture:ACTION_UP");
                btnPicture.clearAnimation();
                try {
                    Intent intent = new Intent();
                    ComponentName componentNameGallery = new ComponentName("com.js.photoalbum", "com.js.photoalbum.activity.CheckPermissionActivity");
                    intent.setComponent(componentNameGallery);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "系统中暂无该应用", Toast.LENGTH_SHORT).show();
                }
            } else if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                Log.e(TAG, "picture:ACTION_CANCEL");
                btnPicture.clearAnimation();
            } else if (!CustomUtil.isTouchPointInView(btnPicture, (int) motionEvent.getRawX(), (int) motionEvent.getRawY())) {
                Log.e(TAG, "picture:x:" + motionEvent.getX());
                btnPicture.clearAnimation();
            }
            return false;
        });
        // 音乐跳转
        rlMusic.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_btn_small);
                rlMusic.startAnimation(animation);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP && CustomUtil.isTouchPointInView(rlMusic, (int) motionEvent.getRawX(), (int) motionEvent.getRawY()) && mViewPager.getCurrentItem() == 0) {
                rlMusic.clearAnimation();
                try {
                    Intent intent = new Intent();
                    ComponentName componentNameGallery = new ComponentName("com.tencent.qqmusicpad", "com.tencent.qqmusicpad.activity.AppStarterActivity");
                    intent.setComponent(componentNameGallery);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "系统中暂无该应用", Toast.LENGTH_SHORT).show();
                }
            } else if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                rlMusic.clearAnimation();
            } else if (!CustomUtil.isTouchPointInView(rlMusic, (int) motionEvent.getRawX(), (int) motionEvent.getRawY())) {
                Log.e(TAG, "picture:x:" + motionEvent.getX());
                rlMusic.clearAnimation();
            }
            return false;
        });
        // 投屏跳转
        btnProjectionScreen.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_btn_small);
                btnProjectionScreen.startAnimation(animation);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP && CustomUtil.isTouchPointInView(btnProjectionScreen, (int) motionEvent.getRawX(), (int) motionEvent.getRawY()) && mViewPager.getCurrentItem() == 0) {
                btnProjectionScreen.clearAnimation();
                try {
                    Intent intent = new Intent();
                    ComponentName componentNameGallery = new ComponentName("com.apowersoft.mirror.tv", "com.apowersoft.mirror.tv.ui.activity.WelcomeActivity");
                    intent.setComponent(componentNameGallery);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "系统中暂无该应用", Toast.LENGTH_SHORT).show();
                }
            } else if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                btnProjectionScreen.clearAnimation();
            } else if (!CustomUtil.isTouchPointInView(btnProjectionScreen, (int) motionEvent.getRawX(), (int) motionEvent.getRawY())) {
                Log.e(TAG, "picture:x:" + motionEvent.getX());
                btnProjectionScreen.clearAnimation();
            }
            return false;
        });
        // 时钟跳转
        btnAlarm.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_btn_small);
                btnAlarm.startAnimation(animation);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP && CustomUtil.isTouchPointInView(btnAlarm, (int) motionEvent.getRawX(), (int) motionEvent.getRawY()) && mViewPager.getCurrentItem() == 0) {
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
            } else if (!CustomUtil.isTouchPointInView(btnAlarm, (int) motionEvent.getRawX(), (int) motionEvent.getRawY())) {
                Log.e(TAG, "picture:x:" + motionEvent.getX());
                btnAlarm.clearAnimation();
            }
            return false;
        });
        // 浏览器跳转
        btnBrowser.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_btn_small);
                btnBrowser.startAnimation(animation);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP && CustomUtil.isTouchPointInView(btnBrowser, (int) motionEvent.getRawX(), (int) motionEvent.getRawY()) && mViewPager.getCurrentItem() == 0) {
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
            } else if (!CustomUtil.isTouchPointInView(btnBrowser, (int) motionEvent.getRawX(), (int) motionEvent.getRawY())) {
                Log.e(TAG, "picture:x:" + motionEvent.getX());
                btnBrowser.clearAnimation();
            }
            return false;
        });
        // 音乐播放暂停控制
        btnControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        // 通过发送键值实现
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
        // 音乐上一首
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
        // 音乐下一首
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

        //View2 listener
        // 爱奇艺跳转
        ibVideo.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_btn_small);
                ibVideo.startAnimation(animation);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP && CustomUtil.isTouchPointInView(ibVideo, (int) motionEvent.getRawX(), (int) motionEvent.getRawY()) && mViewPager.getCurrentItem() == 1) {
                ibVideo.clearAnimation();
                try {
                    Intent intent = new Intent();
                    ComponentName componentNameGallery = new ComponentName("com.qiyi.video.pad", "com.qiyi.video.WelcomeActivity");
                    intent.setComponent(componentNameGallery);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "系统中暂无该应用", Toast.LENGTH_SHORT).show();
                }
            } else if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                ibVideo.clearAnimation();
            } else if (!CustomUtil.isTouchPointInView(ibVideo, (int) motionEvent.getRawX(), (int) motionEvent.getRawY())) {
                Log.e(TAG, "picture:x:" + motionEvent.getX());
                ibVideo.clearAnimation();
            }
            return false;
        });
        // 音乐跳转
        ibMusic.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_btn_small);
                ibMusic.startAnimation(animation);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP && CustomUtil.isTouchPointInView(ibMusic, (int) motionEvent.getRawX(), (int) motionEvent.getRawY()) && mViewPager.getCurrentItem() == 1) {
                ibMusic.clearAnimation();
                try {
                    Intent intent = new Intent();
                    ComponentName componentNameGallery = new ComponentName("com.tencent.qqmusicpad", "com.tencent.qqmusicpad.activity.AppStarterActivity");
                    intent.setComponent(componentNameGallery);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "系统中暂无该应用", Toast.LENGTH_SHORT).show();
                }
            } else if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                ibMusic.clearAnimation();
            } else if (!CustomUtil.isTouchPointInView(ibMusic, (int) motionEvent.getRawX(), (int) motionEvent.getRawY())) {
                Log.e(TAG, "picture:x:" + motionEvent.getX());
                ibMusic.clearAnimation();
            }
            return false;
        });
        // 直播抖音跳转
        ibTiktok.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_btn_small);
                ibTiktok.startAnimation(animation);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP && CustomUtil.isTouchPointInView(ibTiktok, (int) motionEvent.getRawX(), (int) motionEvent.getRawY()) && mViewPager.getCurrentItem() == 1) {
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
            } else if (!CustomUtil.isTouchPointInView(ibTiktok, (int) motionEvent.getRawX(), (int) motionEvent.getRawY())) {
                Log.e(TAG, "picture:x:" + motionEvent.getX());
                ibTiktok.clearAnimation();
            }
            return false;
        });
        // 办公跳转
        ibOffice.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_btn_small);
                ibOffice.startAnimation(animation);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP && CustomUtil.isTouchPointInView(ibOffice, (int) motionEvent.getRawX(), (int) motionEvent.getRawY()) && mViewPager.getCurrentItem() == 1) {
                ibOffice.clearAnimation();
                try {
                    Intent intent = new Intent();
                    ComponentName componentNameGallery = new ComponentName("com.js.appstore", "com.js.appstore.activity.NetworkCheckActivity");
                    intent.setComponent(componentNameGallery);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("position", 3);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "系统中暂无该应用", Toast.LENGTH_SHORT).show();
                }
            } else if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                ibOffice.clearAnimation();
            } else if (!CustomUtil.isTouchPointInView(ibOffice, (int) motionEvent.getRawX(), (int) motionEvent.getRawY())) {
                Log.e(TAG, "picture:x:" + motionEvent.getX());
                ibOffice.clearAnimation();
            }
            return false;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 设置主屏幕背景
        setBackground();
//        registerForContextMenu(rvAPPList);//为RecyclerviewView注册上下文菜单
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 设置内容为空
        etSource.setText("");
        if (audioManager.isMusicActive()) {
            handler.sendEmptyMessageAtTime(HandlerManager.MUSIC_PAUSE_UI, 100);
            ivMusic.startAnimation(animation);//開始动画
        } else {
            handler.sendEmptyMessageAtTime(HandlerManager.MUSIC_PLAY_UI, 100);
            ivMusic.clearAnimation();
        }
    }

    /**
     * 获取数据
     */
    private void getPointData() {
        pointViews = new ArrayList<>();
        View view;
        if (llPoint != null) {
            llPoint.removeAllViews();
        }
        pointViews.clear();
        for (View view1 : mPageView) {

            //创建底部指示器(小圆点)
            view = new View(MainActivity.this);
            view.setBackgroundResource(R.drawable.background_point);
            view.setEnabled(false);
            //设置宽高
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(20, 20);
            //设置间隔
//            if (pic != mPics[0]) {
                layoutParams.leftMargin = 10;
//            }
            //添加到LinearLayout
            llPoint.addView(view, layoutParams);
            pointViews.add(view);
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 禁止返回
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {

            int rawX = (int) ev.getRawX();
            int rawY = (int) ev.getRawY();

            parentDownX = rawX;
            parentDownY = rawY;

            // 获取当前聚焦
            View v = getCurrentFocus();

            Log.e(TAG, "rawX：" + rawX + ", rawY：" + rawY);
            Log.e(TAG, "copyPageSelectedPosition:dispatch:" + copyPageSelectedPosition);
            // 获取是否点击在gridview
            if (copyPageSelectedPosition > 1) {
                DragGridView gridView = (DragGridView) mPageView.get(copyPageSelectedPosition);
                isGridView = CustomUtil.isTouchPointInView(gridView, rawX, rawY);
                Log.e(TAG, "gridview:" + isGridView);
                gridView.removeLongClick();
            } else {
                isGridView = false;
            }
            // 当没点击在控件上时再触发设置壁纸
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
                handler.postDelayed(updateWallpaper, 800);
            }
            if (CustomUtil.isShouldHideInput(v, ev)) {
                //点击editText控件外部
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    assert v != null;
                    //软键盘工具类关闭软键盘
                    CustomUtil.hideKeyBoard(MainActivity.this);
                    //使输入框失去焦点
                    v.clearFocus();
                }
            }
            return super.dispatchTouchEvent(ev);
        } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            int rawX = (int) ev.getRawX();
            int rawY = (int) ev.getRawY();

            parentMoveX = rawX;
            parentMoveY = rawY;

            int disX = Math.abs(parentMoveX - parentDownX);
            int disY = Math.abs(parentMoveY - parentDownY);

            if (disX < 10 && disY < 10) {
            } else {
                // 移动时，取消发送设置壁纸runnable
                handler.removeCallbacks(updateWallpaper);
            }

        } else if (ev.getAction() == MotionEvent.ACTION_UP) {
            Log.e(TAG, "ACTION_UP");
            // 抬起时，取消发送设置壁纸runnable
            handler.removeCallbacks(updateWallpaper);
        } else if (ev.getAction() == MotionEvent.ACTION_CANCEL) {
            Log.e(TAG, "ACTION_CANCEL");
            // 取消时，取消发送设置壁纸runnable
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
            if ("com.js.launcher".equals(packageName)) {
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
                // 设置壁纸成功时，回调更新背景
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
        ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
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
            Drawable drawable =new BitmapDrawable(getResources(), getLockWallpaper());//将Bitmap类型转换为Drawable类型
            llBgHome.setBackground(drawable);//设置背景
//            drawable.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);//设置背景灰度
        } catch(Exception e) {
//            android.util.Log.d(TAG,"set Background fail");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (updateDialog != null) {
            updateDialog.dismiss();
        }
        if (wallDialog != null) {
            wallDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清空集合
        appBeanList.clear();
    }
}