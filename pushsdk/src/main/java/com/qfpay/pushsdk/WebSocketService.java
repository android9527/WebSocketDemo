package com.qfpay.pushsdk;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import com.qfpay.pushsdk.proxy.ServiceProxy;
import com.qfpay.pushsdk.request.Http;
import com.qfpay.pushsdk.request.SocketRequest;
import com.qfpay.pushsdk.util.LogUtil;


/**
 * 类名称：WebSocketService 类描述：通讯服务服务 修改时间：
 */
public class WebSocketService extends Service {

    static final String TAG = "WebSocketService";

    private static final int GRAY_SERVICE_ID = 0;

    /**
     * mBinder
     */
    private ServiceBinder mBinder = new ServiceBinder();


    private static WebSocketService instance = null;

    private ServiceProxy serviceProxy;

    public static WebSocketService getService() {
        return instance;
    }


    /**
     * 方法描述： 输入参数： 返回类型：
     *
     * @see Service#onCreate() 备注:
     */
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        if (Build.VERSION.SDK_INT >= 18) {
            Intent innerIntent = new Intent(this, DaemonInnerService.class);
            startService(innerIntent);
        }
        // foreground
        startForeground(GRAY_SERVICE_ID, new Notification());
        LogUtil.d(TAG, "WebSocketService onCreate()");
        serviceProxy = ServiceProxy.getInstance();
        serviceProxy.init(instance);
    }

    /**
     * 方法描述： 输入参数：@param intent 输入参数：@return 返回类型：
     *
     * @see Service#onBind(Intent) 备注：
     */
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void delSocketRequest(SocketRequest currentRequest) {
        serviceProxy.delSocketRequest(currentRequest);
    }

    public SocketRequest getSocketRequest(String sequenceNumber) {
        return serviceProxy.getSocketRequest(sequenceNumber);
    }

    /**
     * 类描述： 修改时间：
     */
    public class ServiceBinder extends Binder {
        public ServiceProxy getService() {
            return serviceProxy;
        }
    }

    /**
     * 方法描述： 输入参数：@param intent 输入参数：@return
     * 返回类型：
     *
     * @see Service#onUnbind(Intent) 备注：
     */
    @Override
    public boolean onUnbind(Intent intent) {
        LogUtil.d(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    /**
     * 方法描述： 输入参数： 返回类型：
     *
     * @see Service#onDestroy() 备注：
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.d(TAG, "onDestroy ...... ");
        if (null != serviceProxy) {
            serviceProxy.shutdown();
        }

        startService(new Intent(WebSocketService.this, WebSocketService.class));
    }


    boolean needConnect = true;

    /**
     * 方法描述： 输入参数：@param intent 输入参数：@param flags
     * 输入参数：@param startId 输入参数：@return 返回类型：
     *
     * @see Service#onStartCommand(Intent, int, int)
     * 备注：
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.d(TAG, "onStartCommand");
        Http.setMService(this);
        if (null != intent) {
            LogUtil.d(TAG, "onStartCommand intent != null");
        } else {
            LogUtil.d(TAG, "onStartCommand service restart by System");
        }
//        Bundle bundle = null;
//        String packageName = "";
//        if (intent != null) {
//            bundle = intent.getExtras();
//            if(bundle != null){
//                packageName = bundle.getString("package");
//                LogUtil.w(TAG, "这是由 " + packageName + " 启动的service");
//            }
//        }
//
//        if (!TextUtils.isEmpty(packageName) && !packageName.equals(getPackageName())) {
//            serviceProxy.connect();
//        }else {
//            ActivityManager.RunningServiceInfo serviceInfo = Helper.isServiceRunning(getApplicationContext(), WebSocketService.class.getName());
//            if (null == serviceInfo) {
//                serviceProxy.connect();
//            } else {
//                Intent startIntent = new Intent();
//                startIntent.setPackage(serviceInfo.service.getPackageName());
//                startIntent.setClassName(serviceInfo.service.getPackageName(), serviceInfo.service.getClassName());
//                if (bundle != null) {
//                    startIntent.putExtras(bundle);
//                }
//                startService(startIntent);
//            }
//        }

        serviceProxy.connect();
        return START_REDELIVER_INTENT;
    }

    /**
     * 方法描述：发送消息 输入参数：@param socketRequest 返回类型：void： 备注：
     */
    public boolean send(SocketRequest request) {
        return serviceProxy.send(request);
    }


    /**
     * 重定向
     */
    public void forwardService() {
//        close();
        serviceProxy.cancelAllRequest();
//        connect(true);
    }


    /**
     * 给 API >= 18 的平台上用的灰色保活手段
     */
    public static class DaemonInnerService extends Service {

        @Override
        public void onCreate() {
            super.onCreate();
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(GRAY_SERVICE_ID, new Notification());
            //stopForeground(true);
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

        @Override
        public IBinder onBind(Intent intent) {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
        }
    }

}
