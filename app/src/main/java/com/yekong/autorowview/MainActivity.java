package com.yekong.autorowview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.yekong.autorowview.entity.TabEntity;
import com.yekong.autorowview.view.AutoLineLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private AutoLineLayout view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view = (AutoLineLayout) findViewById(R.id.view);
        List<TabEntity> list = new ArrayList<>();
        TabEntity entity;
        entity = new TabEntity("[这是第一条]");
        list.add(entity);
        for (int i = 0; i < 10; i++) {
            entity = new TabEntity("[in the end]");
            list.add(entity);
        }
        entity =new TabEntity("[这是最后一条]");
        list.add(entity);
        view.setOneLock(true);
        view.setMaxItem(12);
        view.setChild(list, R.layout.item_text, new AutoLineLayout.OnItemClickListener<TabEntity>() {
            @Override
            public void onItemClick(View view, TabEntity o, int position) {
                if (view.getTag().equals(AutoLineLayout.SELECT_ON)) {

                }
            }
        });
    }
}
