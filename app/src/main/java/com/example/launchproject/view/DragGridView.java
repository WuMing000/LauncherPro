package com.example.launchproject.view;
 
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.launchproject.R;

/**
 * @explain 长按移动的recyclerView
 */
public class DragGridView extends GridView {

    private static final String TAG = "DragRecyclerView======>";

    private static final int CREATE_LONG_CLICK_WINDOW = 0;
    private static final int CANCEL_LONG_CLICK_WINDOW = 1;

    //拖拽响应的时间 默认为1s
    private long mDragResponseMs = 1000;
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

    //按下的点到所在item的左边缘距离
    private int mPoint2ItemLeft;
    private int mPoint2ItemTop;
    private int mPoint2ItemBottom;

    //DragView到上边缘的距离
    private int mOffset2Top;
    private int mOffset2Left;

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

    private boolean isMove = false;
    private int actionUpPosition;
    private int firstMovePosition;
    private View firstMoveView;
//    private ObjectAnimator translationAnimator;
    private Animation translateAnimation;
//    private boolean isCanDrag = false;

    private OnUninstallClick onUninstallClick;
    private OnInformationClick onInformationClick;
    LinearLayout linearLayoutParent;
    private LinearLayout linearLayout1;
    private boolean isSystemAPP;

    private final Handler mHandler;
//            = new Handler(Looper.myLooper()) {
//        @Override
//        public void dispatchMessage(@NonNull Message msg) {
//            super.dispatchMessage(msg);
//            switch (msg.what) {
//                case CREATE_LONG_CLICK_WINDOW:
//                    onCreateLongButton(mDownX, mDownY);
//                    break;
//                case CANCEL_LONG_CLICK_WINDOW:
//                    removeLongClick();
//                    break;
//            }
//        }
//    };

    /**
     * 长按的Runnable
     */
    private final Runnable mLongClickRunnable = new Runnable() {
        @Override
        public void run() {

//            isCanDrag = true;
            onCreateLongButton(mDownX, mDownY);
            isDrag = true;
            mVibrator.vibrate(100);
//            //隐藏该item
//            mDragView.setVisibility(INVISIBLE);
//            //在点击的地方创建并显示item镜像
//            createDragView(mDragBitmap, mDownX, mDownY);
//            if (itemMoveListener != null) {
//                itemMoveListener.onDown(mDragPosition);
//            }
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
        mStatusHeight = getStatusHeight(context);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.e(TAG, "onActionDown");
                getParent().requestDisallowInterceptTouchEvent(true);//告诉viewGroup不要去拦截我
                mDownX = (int) ev.getRawX();
                mDownY = (int) ev.getRawY() - mStatusHeight;
 
//                mDragView = findChildViewUnder(mDownX, mDownY);
                mDragPosition = pointToPosition(mDownX, mDownY - 100);
//                if (mDragView == null) {
//                    return super.dispatchTouchEvent(ev);
//                }
                //获取按下的position
//                mDragPosition = getChildAdapterPosition(mDragView);
                mDragView = getChildAt(mDragPosition - getFirstVisiblePosition());
//                if (mDragPosition == NO_POSITION) {     //无效就返回
//                    return super.dispatchTouchEvent(ev);
//                }
                if(mDragPosition == AdapterView.INVALID_POSITION){
                    return super.dispatchTouchEvent(ev);
                }

                removeLongClick();
                //延时长按执行mLongClickRunnable
                mHandler.postDelayed(mLongClickRunnable, mDragResponseMs);

                //获取按下的item对应的View 由于存在复用机制，所以需要处理FirstVisiblePosition
                //计算按下的点到所在item的left top 距离
                mPoint2ItemLeft = mDownX - mDragView.getLeft();
                mPoint2ItemTop = mDownY - mDragView.getTop();
                mPoint2ItemBottom = mDownY - mDragView.getBottom();
                //计算RecyclerView的left top 偏移量：原始距离 - 相对距离就是偏移量
                mOffset2Left = (int) ev.getRawX() - mDownX;
                mOffset2Top = (int) ev.getRawY() - mDownY;
                //开启视图缓存
                mDragView.setDrawingCacheEnabled(true);
                //获取缓存的中的bitmap镜像 包含了item中的ImageView和TextView
                mDragBitmap = Bitmap.createBitmap(mDragView.getDrawingCache());
                //释放视图缓存 避免出现重复的镜像
                mDragView.destroyDrawingCache();

                Log.e(TAG, "view:mDownX " + mDownX + ", mDownY " + mDownY);
                Log.e(TAG, "view:left " + mDragView.getLeft() + ", right " + mDragView.getRight());
                Log.e(TAG, "view:top " + mDragView.getTop() + ",bottom " + mDragView.getBottom());
                if (itemMoveListener != null) {
                    itemMoveListener.onDown(mDownX, mDownY, mDragPosition, mHandler, mLongClickRunnable);
                }

                break;
            case MotionEvent.ACTION_MOVE:

                Log.e(TAG, "onActionMove");
                mMoveX = (int) ev.getRawX();
                mMoveY = (int) ev.getRawY() - mStatusHeight;

                int disX = Math.abs(mMoveX - mDownX);
                int disY = Math.abs(mMoveY - mDownY);

                if (disX == 0 && disY == 0) {
                    isMove = false;
//                    onCreateLongButton(mDownX, mDownY);
                } else {
                    mHandler.removeCallbacks(mLongClickRunnable);
                    removeLongClick();
                    getParent().requestDisallowInterceptTouchEvent(true);
//                    mHandler.sendEmptyMessageAtTime(CANCEL_LONG_CLICK_WINDOW, 100);
                    if (isDrag) {
//                        isDrag = true;
//                        isCanDrag = false;
//                        Log.e(TAG, "removeCallbacks");
//                        mVibrator.vibrate(200);
                        if (!isMove) {
                            //隐藏该item
                            mDragView.setVisibility(GONE);
                            //在点击的地方创建并显示item镜像
                            createDragView(mDragBitmap, mDownX, mDownY);
                        }
                        isMove = true;
                    }
                }

//                mMoveView = findChildViewUnder(mMoveX, mMoveY);
                mMovePosition = pointToPosition(mMoveX, mMoveY - 100);
                if(mMovePosition == AdapterView.INVALID_POSITION){
                    return super.dispatchTouchEvent(ev);
                }
//                if (mMoveView == null) {
//                    return super.dispatchTouchEvent(ev);
//                }
                mMoveView = getChildAt(mMovePosition - getFirstVisiblePosition());
                //获取移动的position
//                mMovePosition = getChildAdapterPosition(mMoveView);
                if (mMovePosition == mDragPosition) {
                    firstMovePosition = 0;
                }
                if (mMovePosition != firstMovePosition) {
//                    firstMovePosition = mMovePosition;
                    if (firstMoveView != null) {
//                        translateAnimation.cancel();
                        firstMoveView.clearAnimation();
//                        translationAnimator.cancel();
                    }
//                    Log.e(TAG, "firstPosition" + firstMovePosition);
                }
//                if (mMovePosition == NO_POSITION) {     //无效就返回
//                    return super.dispatchTouchEvent(ev);
//                }
                setActionUpPosition(mMovePosition);
                if ((mMovePosition != mDragPosition) && (mMovePosition != firstMovePosition) && mMoveView != null && isDrag) {
                    firstMovePosition = mMovePosition;
                    firstMoveView = mMoveView;
//                    mMoveView.setVisibility(INVISIBLE);
//                    mDragView.setVisibility(VISIBLE);
                    Log.d(TAG, "update:moveX " + mMoveView.getX() + ", moveY " + mMoveView.getY() + "frontX " + mDragView.getX() + ", frontY " + mDragView.getY());
//                    mMoveView.setTranslationX();
                    if (mMoveView.getX() == mDragView.getX()) {
                        translateAnimation = new TranslateAnimation(0, 0, 0, mDragView.getY() - mMoveView.getY());//Y平移动画
                        translateAnimation.setDuration(500);
                        translateAnimation.setFillAfter(true);
                        firstMoveView.startAnimation(translateAnimation);//给imageView添加的动画效果
//                        ObjectAnimator.ofFloat(mMoveView, "translationX", 0, 300).setDuration(1000).start();
//                        translationAnimator = ObjectAnimator.ofFloat(mMoveView, "translationY", 0, mDragView.getY() - mMoveView.getY());
//                        translationAnimator.setDuration(200);
//                        translationAnimator.start();
//                        mMoveView.setTranslationY(mDragView.getY() - mMoveView.getY());
                    } else if (mMoveView.getY() == mDragView.getY()) {
                        translateAnimation = new TranslateAnimation(0, mDragView.getX() - mMoveView.getX(), 0, 0);//X平移动画
                        translateAnimation.setDuration(500);
                        translateAnimation.setFillAfter(true);
                        firstMoveView.startAnimation(translateAnimation);//给imageView添加的动画效果
                    } else {
                        translateAnimation = new TranslateAnimation(0, mDragView.getX() - mMoveView.getX(), 0, mDragView.getY() - mMoveView.getY());//X、Y平移动画
                        translateAnimation.setDuration(500);
                        translateAnimation.setFillAfter(true);
                        firstMoveView.startAnimation(translateAnimation);//给imageView添加的动画效果
                    }
                }

//                Log.e(TAG, "disX " + disX + ",disY " + disY);

//                if (disX > disY) {
//                    getParent().requestDisallowInterceptTouchEvent(false);
//                } else {
                if (isDrag) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                } else {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
//                }

                //如果只在按下的item上移动，未超过边界，就不移除mLongClickRunnable

                if (!isTouchInItem(mDragView, mMoveX, mMoveY)) {
                    mHandler.removeCallbacks(mLongClickRunnable);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                    Log.e(TAG, "onActionCANCEL");
//                    if (itemMoveListener != null) {
//                        itemMoveListener.onCancel();
//                    }
                    getParent().requestDisallowInterceptTouchEvent(true);
                    mHandler.removeCallbacks(mLongClickRunnable);
                    break;
            case MotionEvent.ACTION_UP:
                Log.e(TAG, "onActionUP");
                mHandler.removeCallbacks(mLongClickRunnable);
                Log.e(TAG, "ACTIONUP_removeCallbacks");
//                mHandler.sendEmptyMessageAtTime(CANCEL_LONG_CLICK_WINDOW, 2000);
                getParent().requestDisallowInterceptTouchEvent(true);
                //判断是否是点击
                if (!isMove && !isDrag && mDragMirrorView == null && itemMoveListener != null && Math.abs(mMoveX - mDownX) <20 && Math.abs(mMoveY - mDownY) <20){
                    Log.e(TAG, "onItemOnClick");
                    itemMoveListener.onItemClick(mDragPosition);
                }
                if (itemMoveListener != null) {
                    itemMoveListener.onUp(actionUpPosition, translateAnimation);
                }
//                isCanDrag = false;
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

    private boolean isFrontCoordinate(View view) {
        if (view == null) {
            return false;
        }

        int left = view.getLeft();
        int right = view.getRight();
        int top = view.getTop();
        int bottom = view.getBottom();
        int x = (right + left) / 2;
        int y = (top + bottom) / 2;

        Log.e(TAG, "left: " + left + ", right: " + right + ", x: " + x);
        Log.e(TAG, "top: " + top + ", bottom: " + bottom + ", y: " + y);

        return true;

    }
 

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isDrag && mDragMirrorView != null) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    mMoveX = (int) ev.getRawX();
                    mMoveY = (int) ev.getRawY();
                    Log.e(TAG, "mMoveX:" + mMoveX + ",mMoveY:" + mMoveY);
                    onDragItem(mMoveX, mMoveY);
//                    Log.e("MOVE", "onMovePosition====>" + mMovePosition + "");
//                    Log.e(TAG, "view:MOVE:mDownX " + mMoveX + ", mDownY " + mMoveY);
//                    Log.e(TAG, "view:MOVE:x " + mLayoutParams.x + ", y " + mLayoutParams.y);
//                    Log.e(TAG, "view:onMove:top " + mDragMirrorView.getTop() + ",bottom " + mDragMirrorView.getBottom());
//                    isFrontCoordinate(mDragMirrorView);
                    break;
//                case MotionEvent.ACTION_UP:
//                    onStopDrag();
//                    isDrag = false;
//                    break;
                default:
                    break;
            }
            return true;
        }
        return super.onTouchEvent(ev);
    }

    private void setActionUpPosition(int position) {
        this.actionUpPosition = position;
    }
 
    /************************对外提供的接口***************************************/
 
    public boolean isDrag() {
        return isDrag;
    }
 
    public void setDrag(boolean drag) {
        isDrag = drag;
    }
 
    public long getDragResponseMs() {
        return mDragResponseMs;
    }
 
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
     * 获取状态栏的高度
     *
     * @param context
     * @return
     */
    @SuppressLint("PrivateApi")
    private static int getStatusHeight(Context context) {
        int statusHeight;
        Rect localRect = new Rect();
        ((Activity) context).getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);
        statusHeight = localRect.top;
        if (0 == statusHeight) {
            Class<?> localClass;
            try {
//                localClass = Class.forName("com.android.internal.R$dimen");
//                Object localObject = localClass.newInstance();
//                int height = Integer.parseInt(Objects.requireNonNull(localClass.getField("status_bar_height").get(localObject)).toString());
//                statusHeight = context.getResources().getDimensionPixelSize(height);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return statusHeight;
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
     * WindowManager 移除镜像
     */
    public void removeLongClick() {
        if (linearLayoutParent != null) {
            mWindowManager.removeView(linearLayoutParent);
            linearLayoutParent = null;
        }
    }
 
    /**
     * 拖动item到指定位置
     *
     * @param x
     * @param y
     */
    private void onDragItem(int x, int y) {
        mLayoutParams.x = x - mPoint2ItemLeft + mOffset2Left;
//        mLayoutParams.y = y - mPoint2ItemTop + mOffset2Top - mStatusHeight;
        mLayoutParams.y = y - mPoint2ItemTop + mOffset2Top;
        //更新镜像位置
        mWindowManager.updateViewLayout(mDragMirrorView, mLayoutParams);
        int[] location = new int[2];
        getLocationOnScreen(location);
        int pY = location[1];
        if (itemMoveListener != null){
            View childViewUnder = getChildAt(pointToPosition(x, y - 100) - getFirstVisiblePosition());
            itemMoveListener.onMove(x,y + pY, childViewUnder, isMove, mMovePosition);
        }
//        Log.e(TAG, "view:onDragItem:mDownX " + x + ", mDownY " + y);
//        Log.e(TAG, "view:onDragItem:mDragMirrorViewX left " + mDragMirrorView.getLeft() + ", mDragMirrorViewX right " + mDragMirrorView.getRight());
//        Log.e(TAG, "view:onDragItem:mDragMirrorViewX top " + mDragMirrorView.getTop() + ", mDragMirrorViewX bottom " + mDragMirrorView.getBottom());
//        Log.e(TAG, "view:onDragItem:x " + mLayoutParams.x + ", y " + mLayoutParams.y);
    }
 
    /**
     * 创建拖动的镜像
     *
     * @param bitmap
     * @param downX
     * @param downY
     */
    @SuppressLint("RtlHardcoded")
    private void createDragView(Bitmap bitmap, int downX, int downY) {
        mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.format = PixelFormat.TRANSLUCENT; //图片之外其他地方透明
        mLayoutParams.gravity = Gravity.TOP | Gravity.LEFT; //左 上
        //指定位置 其实就是 该 item 对应的 rawX rawY 因为Window 添加View是需要知道 raw x ,y的
        mLayoutParams.x = mOffset2Left + downX - mPoint2ItemLeft;
//        mLayoutParams.y = mOffset2Top + (downY - mPoint2ItemTop) + mStatusHeight;
//        mLayoutParams.y = mOffset2Top + (downY - 2 * mPoint2ItemTop);
        mLayoutParams.y = downY - mPoint2ItemTop;
        //指定布局大小
        mLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        //透明度
//        mLayoutParams.alpha = 0.5f;
        //指定标志 不能获取焦点和触摸,允许拖动到窗口外
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE|WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
 
        mDragMirrorView = new ImageView(getContext());
        mDragMirrorView.setImageBitmap(bitmap);
        //添加View到窗口中
        mWindowManager.addView(mDragMirrorView, mLayoutParams);
    }

    private void onCreateLongButton(int downX, int downY) {
        linearLayoutParent = new LinearLayout(getContext());
        linearLayoutParent.setOrientation(LinearLayout.VERTICAL);
        linearLayoutParent.setBackground(getResources().getDrawable(R.drawable.selector_layout_bg));
        linearLayoutParent.setPadding(15, 15, 15, 15);

        if (!isSystemAPP) {
            linearLayout1 = new LinearLayout(getContext());
            ImageView uninstallImg = new ImageView(getContext());
            Button uninstallButton = new Button(getContext());
            uninstallImg.setBackground(getResources().getDrawable(R.drawable.uninstall));
            uninstallButton.setId(R.id.btn_uninstall);
            uninstallButton.setBackgroundColor(getResources().getColor(R.color.transparent));
            uninstallButton.setText("卸载");
//            uninstallButton.setGravity(VERIC);
            uninstallButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeLongClick();
                    onUninstallClick.OnClick();
                }
            });
            linearLayout1.addView(uninstallImg);
            linearLayout1.addView(uninstallButton);
        }

        LinearLayout linearLayout2 = new LinearLayout(getContext());
        ImageView informationImg = new ImageView(getContext());
        Button informationButton = new Button(getContext());
        informationImg.setBackground(getResources().getDrawable(R.drawable.information));
        informationButton.setId(R.id.btn_information);
        informationButton.setBackgroundColor(getResources().getColor(R.color.transparent));
        informationButton.setText("应用信息");
//        informationButton.setGravity( VERTICAL);
        informationButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                removeLongClick();
                onInformationClick.OnClick();
            }
        });
        linearLayout2.addView(informationImg);
        linearLayout2.addView(informationButton);

        if (!isSystemAPP) {
            linearLayoutParent.addView(linearLayout1);
        }
        linearLayoutParent.addView(linearLayout2);

        mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.format = PixelFormat.TRANSLUCENT;
        mLayoutParams.gravity = Gravity.TOP | Gravity.LEFT; //左 上
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mLayoutParams.x = mOffset2Left + (downX - mPoint2ItemLeft);
        mLayoutParams.y = (int) (mOffset2Top + mDragView.getY());
        mLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowManager.addView(linearLayoutParent, mLayoutParams);
    }
 
    /**
     * item 交换时的回调接口
     */
    public interface OnItemMoveListener {
        void onDown(int x, int y, int position, Handler handler, Runnable runnable);
        void onMove(int x, int y, View view, boolean isMove, int position);
//        void onCancel();
        void onUp(int position, Animation translateAnimation);
        void onItemClick(int position);
    }

    public void isUninstallVisible(boolean isVisible) {
        isSystemAPP = isVisible;
    }

    public void setOnUninstallClick(OnUninstallClick onUninstallClick) {
        this.onUninstallClick = onUninstallClick;
    }

    public interface OnUninstallClick {
        void OnClick();
    }

    public void setOnInformationClick(OnInformationClick onInformationClick) {
        this.onInformationClick = onInformationClick;
    }

    public interface OnInformationClick {
        void OnClick();
    }
}