package com.example.launchproject.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;

import com.example.launchproject.MyApplication;
import com.example.launchproject.R;
import com.example.launchproject.adapter.LinearRecyclerViewAdapter;
import com.example.launchproject.adapter.MyGridViewAdapter;
import com.example.launchproject.adapter.RecyclerViewAPPAdapter;
import com.example.launchproject.bean.APPBean;
import com.example.launchproject.helper.ViewPagerScroller;
import com.example.launchproject.manager.HandlerManager;
import com.example.launchproject.utils.CustomUtil;
import com.example.launchproject.view.DragGridView2;
import com.example.launchproject.view.DragRecyclerView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import static android.content.pm.PackageManager.GET_URI_PERMISSION_PATTERNS;

public class TestActivity extends AppCompatActivity {

    private static final String TAG = "TestActivity========>";

    private ViewPager viewPager;
    private int totalPage;//总的页数
    private int mPageSize = 18;//每页显示的最大数量
    private List<APPBean> listDatas;//总的数据源
    private List<View> viewPagerList;//GridView作为一个View对象添加到ViewPager集合中
    private int currentPage;//当前页
    private APPBean saveFrontAPPContent;
    private int downPosition;
    ScaleAnimation scaleAnimation;
    private boolean isScale;
    private boolean isDown;
    int savePosition = 0;

    ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            Log.e(TAG, position + ":onPageSelected");
            savePosition = position;
//                Log.e(TAG, position + ":onPageScrolled");
            View view = viewPagerList.get(position);
            DragGridView2 gridView = (DragGridView2) view;
//            MyGridViewAdapter adapter = (MyGridViewAdapter) gridView.getAdapter();
            gridView.setOnItemMoveListener(new DragGridView2.OnItemMoveListener() {
                @Override
                public void onDown(int p, Handler handler, Runnable runnable) {
                    Log.d("TAG", "onDown");
//                Log.e(TAG, "childCount:" + rvAPPList.getChildCount());
                    int newPosition = p + savePosition * mPageSize;
                    if (listDatas.get(newPosition).getPackageName().length() == 0) {
                        handler.removeCallbacks(runnable);
                    }
                    gridView.isUninstallVisible(CustomUtil.isSystemApplication(TestActivity.this, listDatas.get(newPosition).getPackageName()));
                    Log.d("wu", "onDown====>" + newPosition + "");
//                if (rvAPPList.isDrag()) {
                    saveFrontAPPContent = new APPBean(listDatas.get(newPosition).getAppName(), listDatas.get(newPosition).getAppIcon(), listDatas.get(newPosition).getPackageName());
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
                        handler.postDelayed(parentRunnable, 1500);
                    } else {
                        handler.removeCallbacks(nextRunnable);
                        handler.removeCallbacks(parentRunnable);
                    }
                    if (isMove && !isScale) {
                        isDown = true;
                        isScale = true;
                        viewPager.setAlpha(0.8f);
                        scaleAnimation = new ScaleAnimation(1.0f, 0.8f, 1.0f, 0.8f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                        scaleAnimation.setDuration(100);//设置动画持续时间
                        viewPager.startAnimation(scaleAnimation);
                        scaleAnimation.setFillAfter(true);
                        for (View view1 : viewPagerList) {
                            DragGridView2 view2 = (DragGridView2) view1;
                            view2.setBackground(getResources().getDrawable(R.drawable.selector_recyclerview_bg));
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
                    Log.d("TAG", "onUp:downPosition:" + downPosition + ",position:" + newPosition + ",saveFrontAPPContent:" + saveFrontAPPContent + ",moveData:" + listDatas.get(newPosition));
                    if (isScale) {
                        isScale = false;
                        for (View view1 : viewPagerList) {
                            DragGridView2 view2 = (DragGridView2) view1;
                            view2.setBackgroundColor(getResources().getColor(R.color.transparent));
                        }
//                            gridView.setBackgroundColor(getResources().getColor(R.color.transparent));
                        viewPager.setAlpha(1.0f);
                        scaleAnimation = new ScaleAnimation(0.8f, 1.0f, 0.8f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                        scaleAnimation.setDuration(300);//设置动画持续时间
                        viewPager.startAnimation(scaleAnimation);
                        scaleAnimation.setFillAfter(true);
//                            viewPager.setClipToPadding(true);
//                            LinearLayout.LayoutParams marginLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//                            marginLayoutParams.setMargins(0, 0, 0, 0);
//                            viewPager.setLayoutParams(marginLayoutParams);
                    }
//                Log.e(TAG, "getStartPageIndex:" + scrollHelper.getStartPageIndex());
//                scrollHelper.scrollToPosition(scrollHelper.getStartPageIndex() + 1);
                    if (newPosition != downPosition && gridView.isDrag()) {
                        Log.e("TAG", "我进来了");
                        listDatas.set(downPosition, listDatas.get(newPosition));
                        listDatas.set(newPosition, saveFrontAPPContent);
                        viewPager.getAdapter().notifyDataSetChanged();
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
                    int newPosition = p + position * mPageSize;
                    if (listDatas.get(newPosition).getPackageName().length() == 0) {
                        return;
                    }
                    Log.d("TAG", "onItemClick====>" + newPosition + "");
                    Log.d("TAG", "click recyclerview item " + newPosition);
                    //查询这个应用程序的入口activity。把他开启起来
                    try {
                        PackageManager pm = getPackageManager();
                        Intent intent = pm.getLaunchIntentForPackage(listDatas.get(newPosition).getPackageName());
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void dispatchMessage(@NonNull Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what) {
                case 0x001:
                    viewPager.setCurrentItem(savePosition + 1, true);
                    handler.removeCallbacks(nextRunnable);
                    break;
                case 0x002 :
//                    --savePosition;
                    //                    ++savePosition;
                    viewPager.setCurrentItem(savePosition - 1, true);
                    handler.removeCallbacks(parentRunnable);
                    break;
            }
        }
    };

    Runnable nextRunnable = new Runnable() {
        @Override
        public void run() {
            handler.sendEmptyMessageAtTime(0x001, 100);
        }
    };

    Runnable parentRunnable = new Runnable() {
        @Override
        public void run() {
            handler.sendEmptyMessageAtTime(0x002, 100);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        viewPager = findViewById(R.id.view_pager);
        setDatas();
        LayoutInflater inflater = LayoutInflater.from(this);
        //总的页数，取整（这里有三种类型：Math.ceil(3.5)=4:向上取整，只要有小数都+1  Math.floor(3.5)=3：向下取整  Math.round(3.5)=4:四舍五入）
        totalPage = (int) Math.ceil(listDatas.size() * 1.0 / mPageSize);
        viewPagerList = new ArrayList<>();
        for(int i = 0; i < totalPage; i++){
            //每个页面都是inflate出一个新实例
            DragGridView2 gridView = (DragGridView2) inflater.inflate(R.layout.grid_view_app_list, null);
            MyGridViewAdapter myGridViewAdapter = new MyGridViewAdapter(this, listDatas, i, mPageSize);
            gridView.setAdapter(myGridViewAdapter);
//            gridView.setBackground(getResources().getDrawable(R.drawable.selector_recyclerview_bg));
            //每一个GridView作为一个View对象添加到ViewPager集合中
            viewPagerList.add(gridView);
        }
//        for (int i = 0; i < viewPagerList.size(); i++) {
//            final int j = i;
//
//        }

        viewPager.setPageMargin(15);
//        viewPager.setPageTransformer(true, new MyGallyPageTransformer());
        viewPager.setOffscreenPageLimit(3);
        ViewPagerScroller scroller =  new ViewPagerScroller(this);
//        scroller.setScrollDuration(2000);
//        scroller.initViewPagerScroll(viewPager); //这个是设置切换过渡时间为2秒
        //设置ViewPager适配器
        viewPager.setAdapter(new MyViewPagerAdapter(viewPagerList));

        viewPager.addOnPageChangeListener(onPageChangeListener);

        onPageChangeListener.onPageSelected(0);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
//        int widthPixels = getResources().getDisplayMetrics().widthPixels;
//        float rawX = ev.getRawX();
//        float rawY = ev.getRawY();
//        Log.d("TAG", "x:" + rawX + ",y:" + rawY + "widthPixels:" + widthPixels);
//        if (rawX >= widthPixels - 200) {
//            viewPager.setCurrentItem(1);
//        }
//        if (rawX <= 200) {
//            viewPager.setCurrentItem(0);
//        }
        return super.dispatchTouchEvent(ev);
    }

    private void setDatas() {
        listDatas = new ArrayList<>();
        getAllAppNames();
        while (listDatas.size() % 18 != 0) {
            listDatas.add(new APPBean());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        new Thread() {
//            @Override
//            public void run() {
//                super.run();
//                getAllAppNames();
//            }
//        }.start();
    }

    /**
     *ViewPager的adapter
     */
    public class MyViewPagerAdapter extends PagerAdapter {

        private List<View> viewLists;//View就是GridView

        public MyViewPagerAdapter(List<View> viewLists) {
            this.viewLists = viewLists;
        }

//        @Override
//        public float getPageWidth(int position) {
//            return 1.0f;
//        }

        /**
         *这个方法，是从ViewGroup中移出当前View
         */
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(viewLists.get(position));
        }

        /**
         * 将当前View添加到ViewGroup容器中
         * 这个方法，return一个对象，这个对象表明了PagerAdapter适配器选择哪个对象放在当前的ViewPager中
         */
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(viewLists.get(position));
            return viewLists.get(position);
        }

        /**
         *这个方法，是获取当前窗体界面数
         */
        @Override
        public int getCount() {
            return viewLists != null ? viewLists.size() : 0;
        }

        /**
         *用于判断是否由对象生成界面
         */
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;//官方文档要求这样写
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
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

            listDatas.add(new APPBean(appName, drawableToBitmap(drawable), packageName));

            j++;
        }
        Log.e("========ccc", "应用的总个数:" + j);
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

    /**
     * 判断app能不能主动启动 否就隐藏
     * */
    public static boolean NotActiveApp(Context context, String packageName){
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent == null)
            return true;
        return false;
    }

    public class ScrollOffsetTransformer implements ViewPager.PageTransformer {
        /**
         * position参数指明给定页面相对于屏幕中心的位置。它是一个动态属性，会随着页面的滚动而改变。
         * 当一个页面（page)填充整个屏幕时，positoin值为0；
         * 当一个页面（page)刚刚离开屏幕右(左）侧时，position值为1（-1）；
         * 当两个页面分别滚动到一半时，其中一个页面是-0.5，另一个页面是0.5。
         * 基于屏幕上页面的位置，通过诸如setAlpha()、setTranslationX()或setScaleY()方法来设置页面的属性，创建自定义的滑动动画。
         */
        @Override
        public void transformPage(View page, float position) {
            if (position > 0) {
                //右侧的缓存页往左偏移100
                page.setTranslationX(-100 * position);
            }
        }
    }

    public class MyGallyPageTransformer implements ViewPager.PageTransformer {
//        private static final float min_scale = 0.85f;
//        @Override
//        public void transformPage(View page, float position) {
//            Log.e("111111111", position + "");
//            float scaleFactor = Math.max(min_scale, 1 - Math.abs(position));
//            if (position < 0 && position >= -1) {
//                page.setScaleX(scaleFactor);
//                page.setScaleY(scaleFactor);
//            } else if (position == 0) {
//                page.setScaleX(1.0f);
//                page.setScaleY(1.0f);
//            } else if (position <= 1 && position > 0) {
//                page.setScaleX(scaleFactor);
//                page.setScaleY(scaleFactor);
//            } else {
//                page.setScaleX(scaleFactor);
//                page.setScaleY(scaleFactor);
//            }
//        }
        public static final float DEFAULT_MAX_ROTATION = 60f;
        public static final float DEF_MIN_SCALE = 0.86f;

        /**
         * 最大旋转角度
         */
        private float mMaxRotation = DEFAULT_MAX_ROTATION;

        /**
         * 最小缩放
         */
        private float mMinScale = DEF_MIN_SCALE;

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        public void transformPage(@NonNull View page, float position) {
            page.setPivotY(page.getHeight() / 2f);

            float distance = getCameraDistance();
            page.setCameraDistance(distance);//设置 View 的镜头距离，可以防止旋转大角度时出现图像失真或不显示。
            if (position < -1) { // [-Infinity,-1)
                page.setRotationY(-mMaxRotation);
                page.setPivotX(page.getWidth());
            } else if (position <= 1) { // [-1,1]

                page.setRotationY(position * mMaxRotation);
                if (position < 0) {//[0,-1]
                    page.setPivotX(page.getWidth());
                    float scale = DEF_MIN_SCALE + 4f * (1f - DEF_MIN_SCALE) * (position + 0.5f) * (position + 0.5f);
                    page.setScaleX(scale);
                    page.setScaleY(scale);
                } else {//[1,0]
                    page.setPivotX(0);
                    float scale = DEF_MIN_SCALE + 4f * (1f - DEF_MIN_SCALE) * (position - 0.5f) * (position - 0.5f);
                    page.setScaleX(scale);
                    page.setScaleY(scale);
                }
            } else { // (1,+Infinity]
                page.setRotationY(mMaxRotation);
                page.setPivotX(0);
            }
        }

        /**
         * 获得镜头距离（图像与屏幕距离）。参考{@link View#setCameraDistance(float)}，小距离表示小视角，
         * 大距离表示大视角。这个距离较小时，在 3D 变换（如围绕X和Y轴的旋转）时，会导致更大的失真。
         * 如果改变 rotationX 或 rotationY 属性，使得此 View 很大 （超过屏幕尺寸的一半），则建议始终使用
         * 大于此时图高度 （X 轴旋转）或 宽度（Y 轴旋转）的镜头距离。
         * @return  镜头距离 distance
         *
         * @see {@link View#setCameraDistance(float)}
         */
        private float getCameraDistance() {
            DisplayMetrics displayMetrics = MyApplication.getContext().getResources().getDisplayMetrics();
            float density = displayMetrics.density;
            int widthPixels = displayMetrics.widthPixels;
            int heightPixels = displayMetrics.heightPixels;
            return 1.5f*Math.max(widthPixels, heightPixels)*density;
        }
    }
}