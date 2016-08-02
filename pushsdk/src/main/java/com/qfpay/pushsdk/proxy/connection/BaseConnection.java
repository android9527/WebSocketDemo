package com.qfpay.pushsdk.proxy.connection;

import android.content.Context;
import android.text.TextUtils;

import com.qfpay.pushsdk.proxy.TcpMessageParser;
import com.qfpay.pushsdk.request.ImpsConnection;
import com.qfpay.pushsdk.request.TcpMessage;
import com.qfpay.pushsdk.scheduletask.ScheduleTask;
import com.qfpay.pushsdk.scheduletask.ScheduleTaskService;
import com.qfpay.pushsdk.util.Constant;
import com.qfpay.pushsdk.util.LogUtil;
import com.qfpay.pushsdk.util.SPUtil;
import com.qfpay.pushsdk.util.TelephonyUtils;

import java.util.ArrayList;

/**
 * Created by chenfeiyue on 16/7/21.
 * BaseConnection
 */
public abstract class BaseConnection implements IConnection, ScheduleTask.Callback {
    private static final String TAG = "BaseConnection";
    public Context mContext;

    public boolean connecting = false;

    public ArrayList<ImpsConnection> impsConnections = new ArrayList<>();

    public boolean needReConnect = true;

    /**
     * 标记重连次数
     */
    public int reConnectCount = 0;

    public BaseConnection(Context context) {
        this.mContext = context.getApplicationContext();
        ScheduleTaskService.getInstance().init(mContext);
    }

    @Override
    public void addImpsConnection(ImpsConnection impsConnection) {
        if (!impsConnections.contains(impsConnection)) {
            impsConnections.add(impsConnection);
        }
    }

    @Override
    public void removeImpsConnection(ImpsConnection impsConnection) {
        impsConnections.remove(impsConnection);
    }

    @Override
    public void removeAllImpsConnection() {
        if (impsConnections != null && impsConnections.size() > 0) {
            impsConnections.clear();
        }
    }

    @Override
    public boolean sendMessage(String message) {
        return false;
    }

    @Override
    public void connect(String url) {
        if (TelephonyUtils.isWifiAvailable(mContext)) {
            needReConnect = true;
            if (connecting) {
                LogUtil.w(TAG, "TCP is connecting return !");
                return;
            }
            if (isConnected()) {
                LogUtil.w(TAG, "TCP is connected !");
                return;
            }
            realConnect(url);
        } else {
            // 断开不重连
            LogUtil.d(TAG, "wifi 不可用，断开连接");
            stopReConnect();
            disConnect(false);
        }
    }

    @Override
    public void disConnect(boolean needReConnect) {
        this.needReConnect = needReConnect;
        close();
    }

    public abstract void realConnect(String address);

    protected abstract void close();


    @Override
    public long doSchedule() {
        reConnectCount++;
        LogUtil.d(TAG, "重连次数 " + reConnectCount);
        String address = SPUtil.getInstance(mContext).getString(Constant.SPKey.KEY_PUSH_URL, Constant.URL);
        connect(address);
        int count = SPUtil.getInstance(mContext).getInt(Constant.SPKey.KEY_RECONNECT_COUNT,
                Constant.DEFAULT_RECONNECT_COUNT);
        if (reConnectCount >= count) {
            return 0;
        }
        return getReconnectInterval();
    }

    @Override
    public void reConnect(String url) {
        int count = SPUtil.getInstance(mContext).getInt(Constant.SPKey.KEY_RECONNECT_COUNT,
                Constant.DEFAULT_RECONNECT_COUNT);
        if (reConnectCount >= count) {
            return;
        }
        if (TextUtils.isEmpty(url)) {
            return;
        }
        LogUtil.w(TAG, "reConnect ---> " + url);
        ScheduleTaskService.getInstance()
                .getScheduleTaskManager()
                .startSchedule(this, getReconnectInterval());
    }


    /**
     * 取消重连
     */
    @Override
    public void stopReConnect() {
        reConnectCount = 0;
        ScheduleTaskService.getInstance()
                .getScheduleTaskManager()
                .stopSchedule(this);
        LogUtil.d(TAG, "Stop stopReConnect...leave");
    }

    /**
     * 获取重连间隔
     *
     * @return 间隔
     */
    public long getReconnectInterval() {
        return SPUtil.getInstance(mContext).getLong(Constant.SPKey.KEY_RECONNECT_INTERVAL,
                Constant.DEFAULT_RECONNECT_INTERVAL);
    }


    /**
     * 连接成功失败回调
     *
     * @param isConnected，是否连接上服务器
     */
    public void notifyListener(boolean isConnected) {
        if (impsConnections != null && impsConnections.size() > 0) {
            for (ImpsConnection impsConnection : impsConnections) {
                if (null == impsConnection)
                    continue;
                impsConnection.connectedNotify(isConnected, needReConnect);
            }
        }

        /**
         * 连接成功，stopReConnect
         */
        if (isConnected) {
            stopReConnect();
        }
        // 连接断开
        else if (this.needReConnect) {
            String address = SPUtil.getInstance(mContext).getString(Constant.SPKey.KEY_PUSH_URL, Constant.URL);
            reConnect(address);
        } else if (!needReConnect) {
            stopReConnect();
        }

    }

    /**
     * 读取到消息回调
     *
     * @param response response
     */
    public void notifyGetMessage(String response) {
        reConnectCount = 0;
        final TcpMessage message = TcpMessageParser.string2TcpMsg(response);
        if (null == message) {
            LogUtil.w(TAG, "notifyGetMessage TcpMessage is null");
        }

        if (impsConnections != null && impsConnections.size() > 0) {
            for (ImpsConnection impsConnection : impsConnections) {
                if (null == impsConnection)
                    continue;
//                impsConnection.receiveMsg(response);
                impsConnection.receiveMsg(message);
            }
        }
    }

    /**
     * 已发送消息回调
     *
     * @param message message
     */
    public void notifySendMessage(String message) {
        if (impsConnections != null && impsConnections.size() > 0) {
            for (ImpsConnection impsConnection : impsConnections) {
                if (null == impsConnection)
                    continue;
                impsConnection.sentMessage(message);
            }
        }
    }

}
