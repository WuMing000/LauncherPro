package com.js.launcher.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.js.launcher.MyApplication;
import com.js.launcher.R;
import com.js.launcher.bean.APPBean;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewAPPAdapter extends RecyclerView.Adapter<RecyclerViewAPPAdapter.APPViewHolder> {

    private Context mContext;
    private List<APPBean> mAPPList;
    private int mPosition = -1;

    OnClickListener onClickListener;
    OnTouchListener onTouchListener;

    private boolean isShowFrame;

    public RecyclerViewAPPAdapter(Context mContext, List<APPBean> mAPPList) {
        this.mContext = mContext;
        this.mAPPList = mAPPList;
    }

    @NonNull
    @Override
    public APPViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_list_view_app, parent, false);
        APPViewHolder appViewHolder = new APPViewHolder(view);
        Configuration mConfiguration = MyApplication.getInstance().getContext().getResources().getConfiguration(); //获取设置的配置信息
        int ori = mConfiguration.orientation; //获取屏幕方向
        if (ori == mConfiguration.ORIENTATION_LANDSCAPE) {
            //横屏
            int parentWidth = parent.getWidth();
            ViewGroup.LayoutParams layoutParams = appViewHolder.itemView.getLayoutParams();
            layoutParams.width = parentWidth / 6;
        } else if (ori == mConfiguration.ORIENTATION_PORTRAIT) {
            //竖屏
            int parentWidth = parent.getWidth();
            ViewGroup.LayoutParams layoutParams = appViewHolder.itemView.getLayoutParams();
            layoutParams.width = parentWidth / 3;
        }
        return appViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull APPViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (isShowFrame) {
            holder.appIcon.setBackground(MyApplication.getInstance().getContext().getResources().getDrawable(R.drawable.selector_iv_bg));
        }
        holder.appIcon.setImageBitmap(mAPPList.get(position).getAppIcon());
//        holder.appIcon.setBackground(mAPPList.get(position).getAppIconDrawable());
        holder.appName.setText(mAPPList.get(position).getAppName());
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mPosition = position;
                return false;
            }
        });
//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                onClickListener.OnClick(position);
//            }
//        });
//        holder.itemView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                onTouchListener.OnTouch(view, motionEvent, position);
//                return false;
//            }
//        });
    }

    @Override
    public int getItemCount() {
//        return Math.min(mAPPList.size(), 16);

        return mAPPList.size();
    }

    public class APPViewHolder extends RecyclerView.ViewHolder {

        private ImageView appIcon;
        private TextView appName;

        public APPViewHolder(@NonNull View itemView) {
            super(itemView);
            appIcon = itemView.findViewById(R.id.iv_app_icon);
            appName = itemView.findViewById(R.id.tv_app_name);
        }
    }

    public interface OnClickListener {
        void OnClick(int position);
    }

    public interface OnTouchListener {
        void OnTouch(View view, MotionEvent motionEvent, int position);
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setOnTouchListener(OnTouchListener onTouchListener) {
        this.onTouchListener = onTouchListener;
    }

    public int getPosition() {
        return mPosition;
    }

    public void isSetFrameVisible(boolean visible) {
        isShowFrame = visible;
    }
}
