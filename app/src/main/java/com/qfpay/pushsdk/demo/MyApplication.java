package com.qfpay.pushsdk.demo;

import android.app.Application;
import android.content.Intent;
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
        Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
