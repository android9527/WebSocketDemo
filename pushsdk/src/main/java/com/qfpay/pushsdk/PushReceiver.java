package com.qfpay.pushsdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by chenfeiyue on 16/8/1.
 * PushReceiver
 */
public class PushReceiver extends BroadcastReceiver {

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