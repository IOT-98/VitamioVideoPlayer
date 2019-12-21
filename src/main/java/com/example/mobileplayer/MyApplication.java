package com.example.mobileplayer;

import android.app.Application;

import org.xutils.x;

// 在application的onCreate中初始化(注意要在功能清单文件中使用 android:name=".MyApplication")
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);
        x.Ext.setDebug(BuildConfig.DEBUG); // 是否输出debug日志, 开启debug会影响性能.
    }
}
