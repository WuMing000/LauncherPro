package com.example.launchproject.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.launchproject.R;
import com.example.launchproject.bean.APPBean;

import java.util.List;

public class ListViewAPPAdapter extends BaseAdapter {

    private Context mContext;
    private List<APPBean> mAPPList;

    public ListViewAPPAdapter(Context mContext, List appList) {
        this.mContext = mContext;
        this.mAPPList = appList;
    }

    @Override
    public int getCount() {
        return mAPPList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        APPListViewHolder appListViewHolder = new APPListViewHolder();
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_list_view_app, null);
            view.setTag(appListViewHolder);
            appListViewHolder.appIcon = view.findViewById(R.id.iv_app_icon);
//            appListViewHolder.appName = view.findViewById(R.id.tv_app_name);
            appListViewHolder.appIcon.setImageBitmap(mAPPList.get(i).getAppIcon());
//            appListViewHolder.appName.setText(mAPPList.get(i).getAppName());
        } else {
            appListViewHolder = (APPListViewHolder) view.getTag();
        }
        return view;
    }

    private class APPListViewHolder {

        private int position;
        private ImageView appIcon;
//        private TextView appName;

        public APPListViewHolder() {
        }

        public APPListViewHolder(int i) {
            this.position = i;
        }

    }
}
