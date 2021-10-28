package com.mega.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.media.tv.TvContract;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.mega.myapplication.camera.EncoderActivity;
import com.mega.myapplication.camera.OpenFourCameraActivity;
import com.mega.myapplication.camera.PreviewActivity;
import com.mega.myapplication.views.GridViewActivity;
import com.mega.myapplication.views.ListViewActivity;
import com.mega.myapplication.views.StaggeredGridViewActivity;

import org.w3c.dom.Text;

public class MainActivity extends Activity {
    private LinearLayout mContainer;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContainer = findViewById(R.id.list_container);
        addItemToContainer();
    }

    private void addItemToContainer() {
        mContainer.addView(getItem("Four camera preview", OpenFourCameraActivity.class));
        mContainer.addView(getItem("encode data from camera", EncoderActivity.class));
        mContainer.addView(getItem("RecyclerView list view", ListViewActivity.class));
        mContainer.addView(getItem("RecyclerView grid view", GridViewActivity.class));
        mContainer.addView(getItem("RecyclerView Stagger view", StaggeredGridViewActivity.class));
    }

    private TextView getItem(String text, final Class activity) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setGravity(Gravity.CENTER);
        textView.setHeight(50);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, activity);
                startActivity(intent);
            }
        });
        return textView;
    }
}
