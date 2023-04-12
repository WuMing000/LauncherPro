package com.example.launchproject.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.launchproject.R;
import com.example.launchproject.bean.APPBean;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewAPPAdapter extends RecyclerView.Adapter<RecyclerViewAPPAdapter.APPViewHolder> {

    private Context mContext;
    private List<APPBean> mAPPList;
    protected int along;
    private int mPosition = -1;

    public RecyclerViewAPPAdapter(Context mContext, List<APPBean> mAPPList) {
        this.mContext = mContext;
        this.mAPPList = mAPPList;
    }

    @NonNull
    @Override
    public APPViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_list_view_app, parent, false);
        APPViewHolder appViewHolder = new APPViewHolder(view);

        //设置高显示个数，4个
//        int parentHeight = parent.getHeight();
//        ViewGroup.LayoutParams layoutParams = appViewHolder.itemView.getLayoutParams();
//        layoutParams.height = (parentHeight / 2);
        return appViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull APPViewHolder holder, @SuppressLint("RecyclerView") int position) {

        holder.appIcon.setImageBitmap(mAPPList.get(position).getAppIcon());
        holder.appName.setText(mAPPList.get(position).getAppName());
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mPosition = position;
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
//        return Math.min(mAPPList.size(), 16);

        return mAPPList.size();
    }

    public class APPViewHolder extends RecyclerView.ViewHolder {

        private ImageView appIcon;
        private TextView appName;
//        private LinearLayout llLongClick;

        public APPViewHolder(@NonNull View itemView) {
            super(itemView);
            appIcon = itemView.findViewById(R.id.iv_app_icon);
            appName = itemView.findViewById(R.id.tv_app_name);
//            llLongClick = itemView.findViewById(R.id.ll_long_click);
        }
    }

    public int getPosition() {
        return mPosition;
    }
}
