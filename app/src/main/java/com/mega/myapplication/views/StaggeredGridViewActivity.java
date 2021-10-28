package com.mega.myapplication.views;


import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.mega.myapplication.R;

public class StaggeredGridViewActivity extends Activity {
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_view);
        mRecyclerView = findViewById(R.id.recycler_view);
    }
}
