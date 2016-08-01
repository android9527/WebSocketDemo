package com.qfpay.push;

import android.app.Application;
//import com.squareup.leakcanary.LeakCanary;

/**
 * Created by chenfeiyue on 16/7/22.
 * MyApplication
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
//        LeakCanary.install(MyApplication.this);
    }
}
