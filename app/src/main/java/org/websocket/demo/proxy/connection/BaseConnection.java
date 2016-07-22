package org.websocket.demo.proxy.connection;

import android.content.Context;
import android.text.TextUtils;

import org.websocket.demo.proxy.ImpsConnection;
import org.websocket.demo.request.Constant;
import org.websocket.demo.scheduletask.ScheduleTask;
import org.websocket.demo.scheduletask.ScheduleTaskService;
import org.websocket.demo.util.LogUtil;
import org.websocket.demo.util.SPUtil;

import java.util.ArrayList;

/**
 * Created by chenfeiyue on 16/7/21.
 * BaseConnection
 */
public abstract class BaseConnection implements IConnection, ScheduleTask.Callback {
    private static final String TAG = "BaseConnection";
    public Context mContext;

    public boolean connecting = false;

    public String address;

    public ArrayList<ImpsConnection> impsConnections = new ArrayList<>();

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
    public void sendMessage(String message) {

    }

    @Override
    public void connect(String url) {
        address = url;
        if (connecting) {
            LogUtil.w(TAG, "TCP is connecting return !");
            return;
        }

        if (isConnected()) {
            LogUtil.w(TAG, "TCP is connected !");
            return;
        }
        realConnect(address);
    }

    @Override
    public void disConnect() {
        close();
    }

    public abstract void realConnect(String address);

    protected abstract void close();


    @Override
    public long doSchedule() {
        reConnectCount++;
        LogUtil.d(TAG, "重连次数 " + reConnectCount);
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
        address = url;
        ScheduleTaskService.getInstance()
                .getScheduleTaskManager()
                .startSchedule(this, getReconnectInterval());
    }


    /**
     * 取消重连
     */
    @Override
    public void stopReConnect() {
        try {
            ScheduleTaskService.getInstance()
                    .getScheduleTaskManager()
                    .stopSchedule(this);
            LogUtil.d(TAG, "Stop stopReConnect...leave");
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                impsConnection.connectedNotify(isConnected);
            }
        }

        /**
         * 连接成功，置0
         */
        if (isConnected) {
            reConnectCount = 0;
            stopReConnect();
        }
        // 连接断开
        else {
            reConnect(address);
        }

    }

    /**
     * 读取到消息回调
     *
     * @param response response
     */
    public void notifyGetMessage(String response) {
        reConnectCount = 0;
        if (impsConnections != null && impsConnections.size() > 0) {
            for (ImpsConnection impsConnection : impsConnections) {
//                            impsConnection.receiveMsg(getMessage(response));
                if (null == impsConnection)
                    continue;
                impsConnection.receiveMsg(response);
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
                impsConnection.sendedMessage(message);
            }
        }
    }

}
