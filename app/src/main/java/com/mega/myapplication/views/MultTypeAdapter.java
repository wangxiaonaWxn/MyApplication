package com.mega.myapplication.views;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public abstract class MultTypeAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private List<T> mDataList;
    private int mLayoutId;

    public MultTypeAdapter(int layoutId, Context context, List<T> data) {
        mContext = context;
        mLayoutId = layoutId;
        mDataList = data;
    }

    abstract void bingData(T data, RecyclerView.ViewHolder holder);

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return CommonViewHolder.getHolder(mContext, mLayoutId, parent);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        bingData(mDataList.get(position), holder);
    }

    @Override
    public int getItemCount() {
        return mDataList == null ? 0 : mDataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

}
