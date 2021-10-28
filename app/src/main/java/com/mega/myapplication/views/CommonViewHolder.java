package com.mega.myapplication.views;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class CommonViewHolder extends RecyclerView.ViewHolder {
    private SparseArray<View> mViews;
    private Context mContext;
    private View mConvertView;

    public CommonViewHolder(Context context, View itemView, ViewGroup parent) {
        super(itemView);
        mContext = context;
        mConvertView = itemView;
        mViews = new SparseArray<>();
    }

    public static CommonViewHolder getHolder(Context context, int layoutId, ViewGroup parent) {
        View itemView = LayoutInflater.from(context).inflate(layoutId, parent, false);
        return new CommonViewHolder(context, itemView, parent);
    }

    public <T extends View> T getViewById(int id) {
        View view = mViews.get(id);
        if (view == null) {
            view = mConvertView.findViewById(id);
            mViews.put(id, view);
        }
        return (T)view;
    }

    public void setText(int viewId, String text) {
        TextView textView = (TextView) mViews.get(viewId);
        textView.setText(text);
    }

    public void setImage(int viewId, int imageId) {
        ImageView imageView = (ImageView) mViews.get(viewId);
        imageView.setImageResource(imageId);
    }
}
