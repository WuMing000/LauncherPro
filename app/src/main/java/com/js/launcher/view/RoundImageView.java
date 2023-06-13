package com.js.launcher.view;
 
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

@SuppressLint("AppCompatCustomView")
public class RoundImageView extends ImageView {
 
    //圆角弧度
    float radius = 15.0f;//15.0f；// 画出圆角效果，圆角（180度为正圆）
 
    public RoundImageView(Context context) {
        super(context);
    }
 
    public RoundImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
 
    public RoundImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
 
    protected void onDraw(Canvas canvas) {
        Path path = new Path();
        int w = this.getWidth();
        int h = this.getHeight();
 
        //绘制圆角imageview
        path.addRoundRect(new RectF(0,0,w,h), radius, radius, Path.Direction.CW);
        canvas.clipPath(path);
        super.onDraw(canvas);
    }
}
 