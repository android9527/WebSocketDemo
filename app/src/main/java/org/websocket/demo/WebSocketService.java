package org.websocket.demo;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import org.websocket.demo.proxy.Http;
import org.websocket.demo.proxy.SocketRequest;
import org.websocket.demo.proxy.ServiceProxy;
import org.websocket.demo.util.LogUtil;


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
     * @see android.app.Service#onCreate() 备注:
     */
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        if (Build.VERSION.SDK_INT < 18) {
            //API < 18 ，此方法能有效隐藏Notification上的图标
            startForeground(GRAY_SERVICE_ID, new Notification());
        } else {
            Intent innerIntent = new Intent(this, DaemonInnerService.class);
            startService(innerIntent);
            startForeground(GRAY_SERVICE_ID, new Notification());
        }
        LogUtil.d(TAG, "WebSocketService onCreate()");
        serviceProxy = ServiceProxy.getInstance();
        serviceProxy.init(instance);
    }

    /**
     * 方法描述： 输入参数：@param intent 输入参数：@return 返回类型：
     *
     * @see android.app.Service#onBind(android.content.Intent) 备注：
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
     * @see android.app.Service#onUnbind(android.content.Intent) 备注：
     */
    @Override
    public boolean onUnbind(Intent intent) {
        LogUtil.d(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    /**
     * 方法描述： 输入参数： 返回类型：
     *
     * @see android.app.Service#onDestroy() 备注：
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.d(TAG, "onDestroy ...... ");
        // TODO
        if (null != serviceProxy) {
            serviceProxy.stopService();
        }

        startService(new Intent(WebSocketService.this, WebSocketService.class));
    }

    /**
     * 方法描述： 输入参数：@param intent 输入参数：@param flags
     * 输入参数：@param startId 输入参数：@return 返回类型：
     *
     * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
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
        // TODO
//        connect(false);
//        return super.onStartCommand(intent, flags, startId);

//        serviceProxy.connect();
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
            LogUtil.d(TAG, "InnerService -> onCreate");
            super.onCreate();
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            LogUtil.d(TAG, "InnerService -> onStartCommand");
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
            LogUtil.d(TAG, "InnerService -> onDestroy");
            super.onDestroy();
        }
    }

}
