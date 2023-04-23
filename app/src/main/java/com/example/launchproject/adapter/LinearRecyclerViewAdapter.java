package com.example.launchproject.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.example.launchproject.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class LinearRecyclerViewAdapter extends RecyclerView.Adapter<LinearRecyclerViewAdapter.LinearViewHolder> {

    private Context mContext;
    private List<View> mList;

    public LinearRecyclerViewAdapter(Context mContext, List<View> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    @NonNull
    @Override
    public LinearRecyclerViewAdapter.LinearViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_linear_recycler_view, parent, false);
        LinearViewHolder appViewHolder = new LinearViewHolder(view);
        return appViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull LinearRecyclerViewAdapter.LinearViewHolder holder, int position) {
        holder.relativeLayout.addView(mList.get(position));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class LinearViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout relativeLayout;

        public LinearViewHolder(@NonNull View itemView) {
            super(itemView);
            relativeLayout = itemView.findViewById(R.id.item_layout);
        }
    }
}
