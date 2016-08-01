package com.qfpay.pushsdk.scheduletask;

import android.content.Context;

public class ScheduleTaskService {
    private static ScheduleTaskService sInstance;

    private ScheduleTaskService() {
    }

    public static ScheduleTaskService getInstance() {
        if (sInstance == null) {
            sInstance = new ScheduleTaskService();
        }
        return sInstance;
    }

    private Context mContext;
    private ScheduleTaskManager mScheduleTaskManager;

    public void init(Context context) {
        mContext = context.getApplicationContext();
    }

    public void shutdown() {
        if (mScheduleTaskManager != null) {
            mScheduleTaskManager.stopAll();
        }

    }

    public ScheduleTask getScheduleTaskManager() {
        if (mContext == null) {
            throw new IllegalStateException("Hasn't been initialized yet");
        }
        if (mScheduleTaskManager == null) {
            mScheduleTaskManager = new ScheduleTaskManager(mContext);
        }
        return mScheduleTaskManager;
    }

}
