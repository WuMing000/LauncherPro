package com.example.launchproject.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.launchproject.MyApplication;
import com.example.launchproject.R;
import com.example.launchproject.bean.APPBean;
import com.example.launchproject.utils.CustomUtil;

import java.util.Arrays;
import java.util.List;

/**
 * GridView加载数据adapter
 */
public class MyGridViewAdapter extends BaseAdapter {

    private List<APPBean> listData;
    private LayoutInflater inflater;
    private Context context;
    private int mIndex;//页数下标，表示第几页，从0开始
    private int mPagerSize;//每页显示的最大数量

    public MyGridViewAdapter(Context context,List<APPBean> listData,int mIndex,int mPagerSize) {
        this.context = context;
        this.listData = listData;
        this.mIndex = mIndex;
        this.mPagerSize = mPagerSize;
        inflater = LayoutInflater.from(context);
    }

    /**
     * 先判断数据集的大小是否足够显示满本页？listData.size() > (mIndex + 1)*mPagerSize
     * 如果满足，则此页就显示最大数量mPagerSize的个数
     * 如果不够显示每页的最大数量，那么剩下几个就显示几个 (listData.size() - mIndex*mPagerSize)
     */
    @Override
    public int getCount() {
        return listData.size() > (mIndex + 1)*mPagerSize ? mPagerSize : (listData.size() - mIndex*mPagerSize);
    }

    @Override
    public Object getItem(int position) {
        return listData.get(position + mIndex * mPagerSize);
    }

    @Override
    public long getItemId(int position) {
        return position + (long) mIndex * mPagerSize;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            convertView = inflater.inflate(R.layout.item_list_view_app,parent,false);
            holder = new ViewHolder();
            holder.proName = convertView.findViewById(R.id.tv_app_name);
            holder.imgUrl = convertView.findViewById(R.id.iv_app_icon);

            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        Configuration mConfiguration = MyApplication.getInstance().getContext().getResources().getConfiguration(); //获取设置的配置信息
        int ori = mConfiguration.orientation; //获取屏幕方向
        if (ori == Configuration.ORIENTATION_LANDSCAPE) {
            // 横屏时，高度显示三个item
            int parentHeight = parent.getHeight();
            ViewGroup.LayoutParams layoutParams = convertView.getLayoutParams();
            layoutParams.height = parentHeight / 4;
        } else if (ori == Configuration.ORIENTATION_PORTRAIT) {
            // 竖屏时，高度显示六个item
            int parentHeight = parent.getHeight();
            ViewGroup.LayoutParams layoutParams = convertView.getLayoutParams();
            layoutParams.height = parentHeight / 6;
        }

        //重新确定position（因为拿到的是总的数据源，数据源是分页加载到每页的GridView上的，为了确保能正确的点对不同页上的item）
        final int pos = position + mIndex * mPagerSize;//假设mPagerSize=18，假如点击的是第二页（即mIndex=1）上的第二个位置item(position=1),那么这个item的实际位置就是pos=19
        APPBean bean = listData.get(pos);
//        if (bean.getPackageName().length() == 0) {
//            holder.imgUrl.setBackgroundColor(MyApplication.getContext().getResources().getColor(R.color.teal_700));
//            holder.imgUrl.setVisibility(View.INVISIBLE);
//        }
        holder.proName.setText(bean.getAppName());
        Bitmap bitmap = null;
        if (bean.getAppIconBytes() != null) {
            bitmap = BitmapFactory.decodeByteArray(bean.getAppIconBytes(), 0, bean.getAppIconBytes().length);
        }
        holder.imgUrl.setImageBitmap(bitmap);
        holder.imgUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position == -1 || listData.get(pos).getPackageName().length() == 0) {
                    return;
                }
                try {
                    PackageManager pm = context.getPackageManager();
                    Intent intent = pm.getLaunchIntentForPackage(listData.get(pos).getPackageName());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        //添加item监听
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(context,"你点击了 "+listData.get(pos).getAppName(),Toast.LENGTH_SHORT).show();
            }
        });
        return convertView;
    }

    class ViewHolder{
        private TextView proName;
        private ImageView imgUrl;
    }
}