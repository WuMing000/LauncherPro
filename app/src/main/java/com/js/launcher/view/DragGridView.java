package com.js.launcher.view;
 
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.js.launcher.R;
import com.js.launcher.utils.CustomUtil;

import androidx.core.content.ContextCompat;

/**
 * @explain 长按移动的gridView
 */
public class DragGridView extends GridView {

    private static final String TAG = "DragRecyclerView======>";

    //拖拽响应的时间 默认为1s
    private long mDragResponseMs = 500;
    //是否支持拖拽，默认不支持
    private boolean isDrag = false;
    //振动器，用于提示替换
    private final Vibrator mVibrator;
    //拖拽的item的position
    private int mDragPosition;
    //拖拽的item对应的View
    private View mDragView;

    //窗口管理器，用于为Activity上添加拖拽的View
    private final WindowManager mWindowManager;
    //item镜像的布局参数
    private WindowManager.LayoutParams mLayoutParams;

    //item镜像的 显示镜像，这里用ImageView显示
    private ImageView mDragMirrorView;
    //item镜像的bitmap
    private Bitmap mDragBitmap;

    //按下时x,y
    private int mDownX;
    private int mDownY;
    //移动的时x，y
    private int mMoveX;
    private int mMoveY;

    //状态栏高度
    private final int mStatusHeight;

    //item发生变化的回调接口
    private OnItemMoveListener itemMoveListener;

    //移动到的item的position
    private int mMovePosition;
    //移动到的item对应的View
    private View mMoveView;

    //是否可以移动
    private boolean isMove = false;
    //手指抬起前的position
    private int actionUpPosition;
    // 初始化position，用于管理清除平移动画
    private int firstMovePosition;
    // 初始化view，用于管理清除平移动画
    private View firstMoveView;
//    private ObjectAnimator translationAnimator;
    //平移动画
    private Animation translateAnimation;
//    private boolean isCanDrag = false;

    //卸载点击
    private OnUninstallClick onUninstallClick;
    //应用信息点击
    private OnInformationClick onInformationClick;
    //父布局，用于展示长按窗口
    LinearLayout linearLayoutParent;
    //是否是系统APP
    private boolean isSystemAPP;

    private final Handler mHandler;

    private boolean isFingerMove;

    /**
     * 长按的Runnable
     */
    private final Runnable mLongClickRunnable = new Runnable() {
        @Override
        public void run() {
            // 创建长按窗口
//            onCreateLongButton(mDownX, mDownY - mStatusHeight);
            // 标记可拖动
//            isDrag = true;
            // 添加振动
//            mVibrator.vibrate(100);
        }
    };

    public DragGridView(Context context) {
        this(context, null);
    }

    public DragGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mHandler = new Handler();
        mStatusHeight = CustomUtil.getStatusHeight(context);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.e(TAG, "onActionDown");
                getParent().requestDisallowInterceptTouchEvent(true);//告诉viewGroup不要去拦截我
                // 获取点击的位置，相对于屏幕左上角
                mDownX = (int) ev.getRawX();
                mDownY = (int) ev.getRawY();
                // 获取点击位置的position
                mDragPosition = pointToPosition(mDownX - 50, mDownY - mStatusHeight);
                // 获取点击位置的view
                mDragView = getChildAt(mDragPosition - getFirstVisiblePosition());
                // 无效就返回
                if(mDragPosition == AdapterView.INVALID_POSITION){
                    return super.dispatchTouchEvent(ev);
                }

                // 移除长按窗口，避免每次按下都创建窗口未清除
                removeLongClick();
                // 延时长按执行mLongClickRunnable
//                mHandler.postDelayed(mLongClickRunnable, mDragResponseMs);

                // 开启视图缓存
                mDragView.setDrawingCacheEnabled(true);
                // 获取缓存的中的bitmap镜像 包含了item中的ImageView和TextView
                mDragBitmap = Bitmap.createBitmap(mDragView.getDrawingCache());
                // 释放视图缓存 避免出现重复的镜像
                mDragView.destroyDrawingCache();

                // 添加点击接口
                if (itemMoveListener != null) {
                    itemMoveListener.onDown(mDownX, mDownY - mStatusHeight, mDragPosition, mDragView, mHandler, mLongClickRunnable);
                }

                break;
            case MotionEvent.ACTION_MOVE:

//                Log.e(TAG, "onActionMove");
                // 获取移动的位置
                mMoveX = (int) ev.getRawX();
                mMoveY = (int) ev.getRawY();

                // 计算移动位置和点击位置的绝对值，用于判断是否移动
                int disX = Math.abs(mMoveX - mDownX);
                int disY = Math.abs(mMoveY - mDownY);

                if (disX < 10 && disY < 10) {
                    // 没有移动，标记没有移动
                    isMove = false;
                    isFingerMove = false;
                } else {
                    isFingerMove = true;
                    // 移动时去除长按runnable，避免长按不够时间也创建长按窗口
                    mHandler.removeCallbacks(mLongClickRunnable);
                    // 拖动时隐藏长按窗口
                    removeLongClick();
                    //告诉viewGroup不要去拦截我
                    getParent().requestDisallowInterceptTouchEvent(true);
                    // 可拖动和可移动
                    if (isDrag && !isMove) {
                        // 隐藏该item
                        mDragView.setVisibility(INVISIBLE);
                        removeDragImage();
                        // 在点击的地方创建并显示item镜像
                        createDragView(mDragBitmap, mDownX, mDownY);
                        // 标记已创建item镜像，避免重复创建
                        isMove = true;
                    }
                }

                if (itemMoveListener != null) {
                    itemMoveListener.isMove(isFingerMove);
                }

                // 获取新位置的position
                mMovePosition = pointToPosition(mMoveX - 70, mMoveY - 120 - mStatusHeight);
                if(mMovePosition == AdapterView.INVALID_POSITION  && isDrag){
                    // 创建item镜像，原位置position变成-1，当可拖动时设置原位置为按下的position
                    mMovePosition = mDragPosition;
                } else if (mMovePosition == AdapterView.INVALID_POSITION) {
                    // 无效返回
                    return super.dispatchTouchEvent(ev);
                }
                // 获取新位置的view
                mMoveView = getChildAt(mMovePosition - getFirstVisiblePosition());

                // 设置最后移动的位置的position
                setActionUpPosition(mMovePosition);
//                Log.e(TAG, "1111111111:" + mMovePosition + ",actionUP:" + actionUpPosition);

//                if (mMovePosition == mDragPosition) {
//                    // 初始化
//                    firstMovePosition = 0;
//                }
//                if (mMovePosition != firstMovePosition && firstMoveView != null) {
//                    // 移动到新的position，去除上一个平移动画
//                    firstMoveView.clearAnimation();
//                }
//
//                // 按下和移动两个position不一致才执行平移动画
//                if (mMovePosition != mDragPosition && mMovePosition != firstMovePosition && mMoveView != null && isDrag) {
//                    // 每次平移重新赋值
//                    firstMovePosition = mMovePosition;
//                    firstMoveView = mMoveView;
////                    Log.d(TAG, "update:moveX " + mMoveView.getX() + ", moveY " + mMoveView.getY() + "frontX " + mDragView.getX() + ", frontY " + mDragView.getY());
//                    if (mMoveView.getX() == mDragView.getX()) {
//                        translateAnimation = new TranslateAnimation(0, 0, 0, mDragView.getY() - mMoveView.getY());//Y平移动画
//                        translateAnimation.setDuration(300);
//                        translateAnimation.setFillAfter(true);
//                        firstMoveView.startAnimation(translateAnimation);//给imageView添加的动画效果
//                    } else if (mMoveView.getY() == mDragView.getY()) {
//                        translateAnimation = new TranslateAnimation(0, mDragView.getX() - mMoveView.getX(), 0, 0);//X平移动画
//                        translateAnimation.setDuration(300);
//                        translateAnimation.setFillAfter(true);
//                        firstMoveView.startAnimation(translateAnimation);//给imageView添加的动画效果
//                    } else {
//                        translateAnimation = new TranslateAnimation(0, mDragView.getX() - mMoveView.getX(), 0, mDragView.getY() - mMoveView.getY());//X、Y平移动画
//                        translateAnimation.setDuration(300);
//                        translateAnimation.setFillAfter(true);
//                        firstMoveView.startAnimation(translateAnimation);//给imageView添加的动画效果
//                    }
//                }

                // 告诉viewGroup不要去拦截我
                getParent().requestDisallowInterceptTouchEvent(isDrag);

                //如果只在按下的item上移动，未超过边界，就不移除mLongClickRunnable
                if (!isTouchInItem(mDragView, mMoveX, mMoveY)) {
                    mHandler.removeCallbacks(mLongClickRunnable);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                    Log.e(TAG, "onActionCANCEL");
                    // 告诉viewGroup不要去拦截我
                    getParent().requestDisallowInterceptTouchEvent(true);
                    mHandler.removeCallbacks(mLongClickRunnable);
                    // 添加cancel接口
                    if (itemMoveListener != null) {
                        itemMoveListener.onCancel();
                    }
                    // 拖动异常时调用去除镜像
                    if (isDrag) {
                        onStopDrag();
                        isDrag = false;
                        return true;
                    }
                    break;
            case MotionEvent.ACTION_UP:
                Log.e(TAG, "onActionUP");
                // 抬起清除长按runnable，避免长按不够时间依然展示长按窗口
                mHandler.removeCallbacks(mLongClickRunnable);
                //告诉viewGroup不要去拦截我
                getParent().requestDisallowInterceptTouchEvent(true);
                // 判断是否是点击
                if (!isMove && !isDrag && mDragMirrorView == null && itemMoveListener != null && Math.abs(mMoveX - mDownX) <20 && Math.abs(mMoveY - mDownY) <20 && mDragPosition != AdapterView.INVALID_POSITION){
                    Log.e(TAG, "onItemOnClick");
                    itemMoveListener.onItemClick(mDragPosition);
                }
                // 添加抬起接口
                if (itemMoveListener != null && actionUpPosition != AdapterView.INVALID_POSITION) {
                    itemMoveListener.onUp(actionUpPosition, translateAnimation);
                }
                // 可拖动时，手指抬起去除镜像和更改标记状态
                if (isDrag) {
                    onStopDrag();
                    isDrag = false;
                    return true;
                }
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isDrag && mDragMirrorView != null && ev.getAction() == MotionEvent.ACTION_MOVE) {
            // 获取移动的位置
            mMoveX = (int) ev.getRawX();
            mMoveY = (int) ev.getRawY();
//            Log.e(TAG, "mMoveX:" + mMoveX + ",mMoveY:" + mMoveY);
            // 更新镜像位置
            onDragItem(mMoveX, mMoveY);
            return true;
        }
        return super.onTouchEvent(ev);
    }

    // 获取抬起的position
    private void setActionUpPosition(int position) {
        this.actionUpPosition = position;
    }

    /************************对外提供的接口***************************************/

    // 获取是否拖动
    public boolean isDrag() {
        return isDrag;
    }

    public boolean isMove() {
        return isMove;
    }

    // 获取振动的时间
    public long getDragResponseMs() {
        return mDragResponseMs;
    }

    // 设置振动的时间
    public void setDragResponseMs(long mDragResponseMs) {
        this.mDragResponseMs = mDragResponseMs;
    }

    public void setOnItemMoveListener(OnItemMoveListener itemMoveListener) {
        this.itemMoveListener = itemMoveListener;
    }
    /******************************************************************************/


    /**
     * 点是否在该View上面
     *
     * @param view
     * @param x
     * @param y
     * @return
     */
    private boolean isTouchInItem(View view, int x, int y) {
        if (view == null) {
            return false;
        }
        if (view.getLeft() < x && x < view.getRight()
                && view.getTop() < y && y < view.getBottom()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 停止拖动
     */
    private void onStopDrag() {
        if (mDragView != null) {
            mDragView.setVisibility(VISIBLE);
        }
        removeDragImage();
//        if (itemMoveListener!=null){
//            itemMoveListener.onUp(mDragPosition);
//        }
    }

    /**
     * WindowManager 移除镜像
     */
    private void removeDragImage() {
        if (mDragMirrorView != null) {
            mWindowManager.removeView(mDragMirrorView);
            mDragMirrorView = null;
        }
    }

    /**
     * WindowManager 移除长按窗口
     */
    public void removeLongClick() {
        if (linearLayoutParent != null) {
            mWindowManager.removeView(linearLayoutParent);
            linearLayoutParent = null;
        }
    }

    public void createLongClick() {
        onCreateLongButton(mDownX, mDownY - mStatusHeight);
        // 标记可拖动
        isDrag = true;
        // 添加振动
        mVibrator.vibrate(100);
    }

    /**
     * 拖动item到指定位置
     *
     * @param x
     * @param y
     */
    private void onDragItem(int x, int y) {
        // 定义位置
        // 减去viewpager的内边距和外边距
        mLayoutParams.x = x - 70 * 2;
//        mLayoutParams.y = y - mPoint2ItemTop + mOffset2Top - mStatusHeight;
        // 减去外边距和状态栏
        mLayoutParams.y = y - 100 * 2 - mStatusHeight;
        //更新镜像位置
        mWindowManager.updateViewLayout(mDragMirrorView, mLayoutParams);
//        int[] location = new int[2];
//        getLocationOnScreen(location);
//        int pY = location[1];
        if (itemMoveListener != null){
//            View childViewUnder = getChildAt(pointToPosition(x, y - 100) - getFirstVisiblePosition());
            itemMoveListener.onMove(x, y, isMove, mMovePosition, mMoveView);
        }
    }

    /**
     * 创建拖动的镜像
     *
     * @param bitmap
     * @param downX
     * @param downY
     */
    @SuppressLint("RtlHardcoded")
    public void createDragView(Bitmap bitmap, int downX, int downY) {
        mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.format = PixelFormat.TRANSLUCENT; // 图片之外其他地方透明
        mLayoutParams.gravity = Gravity.TOP | Gravity.LEFT; // 左 上
        // 指定位置 其实就是 该 item 对应的 rawX rawY 因为Window 添加View是需要知道 raw x ,y的
        // 减去viewpager的内边距和外边距
        mLayoutParams.x = downX - 70 * 2;
//        mLayoutParams.y = mOffset2Top + (downY - mPoint2ItemTop) + mStatusHeight;
//        mLayoutParams.y = mOffset2Top + (downY - 2 * mPoint2ItemTop);
        // 减去外边距和状态栏
        mLayoutParams.y = downY - 100 * 2 - mStatusHeight;
        // 指定布局大小
        mLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
//        mLayoutParams.alpha = 0.5f;   // 透明度
        // 指定标志 不能获取焦点和触摸,允许拖动到窗口外
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;

        mDragMirrorView = new ImageView(getContext());
        // 设置镜像背景
        mDragMirrorView.setImageBitmap(bitmap);
        // 添加View到窗口中
        mWindowManager.addView(mDragMirrorView, mLayoutParams);
    }

    /** 创建长按应用图标展示应用信息和卸载 */
    private void onCreateLongButton(int downX, int downY) {
        LinearLayout linearLayout1 = null;
        linearLayoutParent = new LinearLayout(getContext());
        // 垂直线性布局
        linearLayoutParent.setOrientation(LinearLayout.VERTICAL);
        // 添加弧度背景
        linearLayoutParent.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.selector_layout_bg));
        // 添加内边距
        linearLayoutParent.setPadding(15, 15, 15, 15);

        // 不是系统应用才添加该子布局
        if (!isSystemAPP) {
            // 默认横向线性布局
            linearLayout1 = new LinearLayout(getContext());
            // 添加ImageView和卸载按钮
            ImageView uninstallImg = new ImageView(getContext());
            Button uninstallButton = new Button(getContext());
            // 添加ImageView背景
            uninstallImg.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.uninstall));
            // 设置卸载按钮背景为透明，去除默认按钮背景
            uninstallButton.setBackgroundColor(getResources().getColor(R.color.transparent));
            // 设置卸载按钮内文字
            uninstallButton.setText("卸载");
            // 设置卸载按钮点击监听事件
            uninstallButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeLongClick();
                    onUninstallClick.OnClick();
                }
            });
            // 将ImageView和卸载按钮添加到线性布局
            linearLayout1.addView(uninstallImg);
            linearLayout1.addView(uninstallButton);
        }

        // 应用信息布局，同卸载布局定义
        LinearLayout linearLayout2 = new LinearLayout(getContext());
        ImageView informationImg = new ImageView(getContext());
        Button informationButton = new Button(getContext());
        informationImg.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.information));
        informationButton.setId(R.id.btn_information);
        informationButton.setBackgroundColor(getResources().getColor(R.color.transparent));
        informationButton.setText("应用信息");
        informationButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                removeLongClick();
                onInformationClick.OnClick();
            }
        });
        linearLayout2.addView(informationImg);
        linearLayout2.addView(informationButton);

        // 不是系统应用才把卸载布局添加到父布局
        if (!isSystemAPP) {
            linearLayoutParent.addView(linearLayout1);
        }
        // 将应用信息布局添加到父布局
        linearLayoutParent.addView(linearLayout2);

        // 创建WindowManager嵌套类，用于管理View的一些参数
        mLayoutParams = new WindowManager.LayoutParams();
        // 设置窗口之外半透明，初始窗口之外为黑色
        mLayoutParams.format = PixelFormat.TRANSLUCENT;
        // 设置窗口位置为左上
        mLayoutParams.gravity = Gravity.TOP | Gravity.START; //左 上
        // 设置窗口不聚焦
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        // 定义窗口位置
        mLayoutParams.x = downX;
        mLayoutParams.y = downY;
        // 定义窗口宽度和高度
        mLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        // 把新定义的布局添加到WindowManager
        mWindowManager.addView(linearLayoutParent, mLayoutParams);
    }

    /**
     * item 交换时的回调接口
     */
    public interface OnItemMoveListener {
        void onDown(int x, int y, int position, View downView, Handler handler, Runnable runnable);
        void isMove(boolean isFingerMove);
        void onMove(int x, int y, boolean isMove, int position, View moveView);
        void onCancel();
        void onUp(int position, Animation translateAnimation);
        void onItemClick(int position);
    }

    //是系统APP则不添加卸载子布局
    public void isUninstallVisible(boolean isVisible) {
        isSystemAPP = isVisible;
    }

    //暴露点击卸载外部方法
    public void setOnUninstallClick(OnUninstallClick onUninstallClick) {
        this.onUninstallClick = onUninstallClick;
    }

    //点击卸载接口
    public interface OnUninstallClick {
        void OnClick();
    }

    //暴露点击应用信息外部方法
    public void setOnInformationClick(OnInformationClick onInformationClick) {
        this.onInformationClick = onInformationClick;
    }

    //点击应用信息接口
    public interface OnInformationClick {
        void OnClick();
    }
}