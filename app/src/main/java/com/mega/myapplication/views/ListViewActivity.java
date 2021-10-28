package com.mega.myapplication.views;


import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mega.myapplication.R;

import java.util.ArrayList;

public class ListViewActivity  extends Activity {
    private RecyclerView mRecyclerView;
    private ArrayList<String> mDataList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_view);
        mRecyclerView = findViewById(R.id.recycler_view);
        for (int i = 0; i< 20; i ++) {
            mDataList.add("i=" + i);
        }
        // LinearLayoutManager 构造函数可以添加横向还是竖向滚动或者manager.setOrientation
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(new CommonAdapter(R.layout.activity_list_item_layout, this, mDataList) {
            @Override
            void bingData(Object data, CommonViewHolder holder) {
                holder.setText(R.id.text_view, (String) data);
            }
        });
    }
}
