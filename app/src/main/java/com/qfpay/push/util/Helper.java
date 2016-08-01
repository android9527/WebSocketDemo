package com.qfpay.push.util;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

public class Helper {

    public static ActivityManager.RunningServiceInfo isServiceRunning(Context context, String className) {
        boolean isRunning = false;
        LogUtil.w(className);
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(Integer.MAX_VALUE);
        if (serviceList == null || serviceList.isEmpty())
            return null;
        ActivityManager.RunningServiceInfo serviceInfo = null;
        for (int i = 0; i < serviceList.size(); i++) {
            LogUtil.w(serviceList.get(i).service.getPackageName() + "---->" + serviceList.get(i).service.getClassName());
            // 过滤自己
            if(serviceList.get(i).service.getPackageName().equals(context.getPackageName())){
                continue;
            }

            if (serviceList.get(i).service.getClassName().equals(className)/* && TextUtils.equals(
                    serviceList.get(i).service.getPackageName(), context.getPackageName())*/) {
                serviceInfo = serviceList.get(i);
                break;
            }
        }
        return serviceInfo;
    }

}
