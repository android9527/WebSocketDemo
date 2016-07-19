package org.websocket.demo;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import org.websocket.demo.proxy.Http;
import org.websocket.demo.proxy.Request;
import org.websocket.demo.proxy.ServiceProxy;
import org.websocket.demo.proxy.SocketRequest;


/**
 * 类名称：VtdService 类描述：通讯服务服务 修改时间：
 *
 */
public class VtdService extends Service
{

    static final String TAG = "VtdService";

    /**
     * mBinder
     */
    private ServiceBinder mBinder = new ServiceBinder();


    private static VtdService instance = null;

    private ServiceProxy serviceProxy;

    public static VtdService getService()
    {
        return instance;
    }


    /**
     * 方法名称：onCreate 作者：lining 方法描述： 输入参数： 返回类型：
     *
     * @see android.app.Service#onCreate() 备注:
     */
    @Override
    public void onCreate()
    {
        super.onCreate();
        serviceProxy = ServiceProxy.getInstance(this);
        instance = this;
    }

    /**
     * 方法名称：onStart 作者：lining 方法描述： 输入参数：@param intent 输入参数：@param startId 返回类型：
     *
     * @see android.app.Service#onStart(android.content.Intent, int) 备注：
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onStart(Intent intent, int startId)
    {
        super.onStart(intent, startId);
    }

    /**
     * 方法名称：onBind 作者：lining 方法描述： 输入参数：@param intent 输入参数：@return 返回类型：
     *
     * @see android.app.Service#onBind(android.content.Intent) 备注：
     */
    public IBinder onBind(Intent intent)
    {
        return mBinder;
    }

    public void delSocketRequest(Request currentRequest) {
        serviceProxy.delSocketRequest(currentRequest);
    }

    public SocketRequest getSocketRequest(short sequenceNumber) {
        return serviceProxy.getSocketRequest(sequenceNumber);
    }

    /**
     * 类名称：ServiceBinder 作者： lining 类描述： 修改时间：
     *
     */
    public class ServiceBinder extends Binder
    {
        public ServiceProxy getService()
        {
            return serviceProxy;
        }
    }

    /**
     * (non-Javadoc) 方法名称：onUnbind 作者：lining 方法描述： 输入参数：@param intent 输入参数：@return
     * 返回类型：
     *
     * @see android.app.Service#onUnbind(android.content.Intent) 备注：
     */
    @Override
    public boolean onUnbind(Intent intent)
    {
        LogUtil.d(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    /**
     * (non-Javadoc) 方法名称：onDestroy 作者：lining 方法描述： 输入参数： 返回类型：
     *
     * @see android.app.Service#onDestroy() 备注：
     */
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        LogUtil.d(TAG, "onDestroy ...... ");
        // TODO
    }

    /**
     * 方法名称：onStartCommand 作者：lining 方法描述： 输入参数：@param intent 输入参数：@param flags
     * 输入参数：@param startId 输入参数：@return 返回类型：
     *
     * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
     *      备注：
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        LogUtil.d(TAG, "onStartCommand");
        Http.setMService(this);
        if (null != intent)
        {
            LogUtil.d(TAG, "onStartCommand intent != null");
        }
        else
        {
            LogUtil.d(TAG, "onStartCommand service restart by System");
        }
        // TODO
//        connect(false);
        return super.onStartCommand(intent, flags, startId);
    }


    /**
     * 方法名称：send 作者：lining 方法描述：发送消息 输入参数：@param socketRequest 返回类型：void： 备注：
     */
    public boolean send(SocketRequest socketRequest)
    {
        return serviceProxy.send(socketRequest);
    }


    public boolean isShutdown()
    {
        return true;
    }

    /**
     * 重定向
     */
    public void forwardService()
    {
//        close();
        serviceProxy.cancelAllRequest();
//        connect(true);
    }

}
