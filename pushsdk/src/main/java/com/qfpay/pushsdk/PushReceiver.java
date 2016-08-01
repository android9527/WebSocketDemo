package com.qfpay.pushsdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by chenfeiyue on 16/8/1.
 * PushReceiver
 */
public class PushReceiver extends BroadcastReceiver {

    /**
     * 应用未启动, 个推service已经被唤醒,保存在该时间段内离线消息(此时 GetuiSdkDemoActivity.tLogView == null)
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case Intent.ACTION_BOOT_COMPLETED:
                Intent bootIntent = new Intent(context, WebSocketService.class);
                context.startService(bootIntent);
                break;
        }
    }
}