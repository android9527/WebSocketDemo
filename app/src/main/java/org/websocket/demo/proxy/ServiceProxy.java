package org.websocket.demo.proxy;

import android.content.Context;
import android.net.NetworkInfo;
import android.os.Message;
import android.text.TextUtils;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import org.websocket.demo.proxy.connection.IConnection;
import org.websocket.demo.proxy.connection.OkHttpWebSocketConnection;
import org.websocket.demo.request.BaseRequest;
import org.websocket.demo.request.BindRequest;
import org.websocket.demo.request.HeartbeatRequest;
import org.websocket.demo.request.PushResponse;
import org.websocket.demo.util.DeviceUtil;
import org.websocket.demo.util.LogUtil;
import org.websocket.demo.util.SPUtil;
import org.websocket.demo.request.Constant;
import org.websocket.demo.scheduletask.ScheduleTask;
import org.websocket.demo.scheduletask.ScheduleTaskService;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

public class ServiceProxy implements ScheduleTask.Callback, ImpsConnection {
    private TimeoutHandler timeoutHandler = new TimeoutHandler();

    private static final String TAG = "serviceProxy";

    private Context mContext;

    private int reBindCount = 0;

    /**
     * 网络状态监听器
     */
    private NetworkConnectivityListener mNetworkConnectivityListener;

    /**
     * 网络切换事件
     */
    private static final int EVENT_NETWORK_STATE_CHANGED = 200;

    private ServiceHandler mServiceHandler;

    private IConnection connection;

    private Gson gson = new Gson();

    private static ServiceProxy serviceProxy;

    /**
     * 消息请求队列
     */
    private static Hashtable<String, SocketRequest> reqQueue = new Hashtable<>();

    public ServiceProxy(Context context) {
        mContext = context.getApplicationContext();
        connection = OkHttpWebSocketConnection.instance(mContext);
        initListener(mContext);
    }

    private void initListener(Context context) {
//        mServiceHandler = new ServiceHandler(ServiceProxy.this);
//        mNetworkConnectivityListener = new NetworkConnectivityListener();
//        mNetworkConnectivityListener.registerHandler(mServiceHandler,
//                EVENT_NETWORK_STATE_CHANGED);
//        mNetworkConnectivityListener.startListening(context);
        connection.addImpsConnection(this);
    }

    public static ServiceProxy getInstance(Context context) {
        if (serviceProxy == null) {
            synchronized (ServiceProxy.class) {
                if (serviceProxy == null) {
                    serviceProxy = new ServiceProxy(context);
                }
            }
        }
        return serviceProxy;
    }

    /**
     * 是否已连接到Server
     *
     * @return 是否连接
     */
    public boolean isConnected() {
        return connection.isConnected();
    }

    /**
     * 类描述：监听网络连接回调Handler 修改时间：
     */
    private static final class ServiceHandler extends BaseHandler<ServiceProxy> {
        public ServiceHandler(ServiceProxy proxy) {
            super(proxy);
        }

        @Override
        public void onHandleMessage(Message msg, ServiceProxy proxy) {
            switch (msg.what) {
                case EVENT_NETWORK_STATE_CHANGED:
                    proxy.networkStateChanged();
                    break;
                default:
            }
        }
    }

    /**
     * 方法名称：networkStateChanged 方法描述：网络状态改变时回被调用 输入参数： 返回类型：void： 备注：
     */
    private void networkStateChanged() {
        if (mNetworkConnectivityListener == null) {
            LogUtil.d(TAG, "networkStateChanged: return");

            return;
        }
        NetworkInfo networkInfo = mNetworkConnectivityListener.getNetworkInfo();
        NetworkInfo.State state = networkInfo.getState();

        switch (state) {
            case CONNECTED:
                if (!isConnected()) {
                    connection.connect(Constant.URL);
                }
                break;
            case SUSPENDED:
            case DISCONNECTED:
                LogUtil.d(TAG, "DISCONNECTED");
                connectedNotify(false);
                break;
            default:
                break;
        }
    }

    @Override
    public void receiveMsg(TcpMessage msg) {
        processRsp(msg);
    }

    @Override
    public void receiveMsg(String msg) {
//        parseMessage(msg);
    }

    /**
     * 当连接到服务器会回调该方法
     */
    @Override
    public void connectedNotify(boolean status) {
        LogUtil.d(TAG, "connectedNotify:" + "ConnectStatus:" + status);
        if (status) {
            startBindClient();
        } else {
            stopHeartBeat();
            cancelAllRequest();
        }
    }

    @Override
    public void sendedMessage(String msg) {

    }


    /**
     * 绑定打印机
     */
    public void startBindClient() {
        BindRequest request = new BindRequest();
        request.setDeviceid(DeviceUtil.getUniqueId(mContext));
        request.setSign(request.getSign());
        sendRequest(request, true/*, true*/);
    }

    /**
     * 取消重连
     */
    public void stopReBindClient() {
        reBindCount = 0;
        ScheduleTaskService.getInstance()
                .getScheduleTaskManager()
                .stopSchedule(reBindCallback);
        LogUtil.d(TAG, "Stop stopReBindClient...leave");
    }

    /**
     * 重新绑定打印机
     */
    private void startReBindClient() {
        LogUtil.d(TAG, "startReBindClient()");
        int count = SPUtil.getInstance(mContext).getInt(Constant.SPKey.KEY_RESEND_COUNT, Constant.DEFAULT_RESEND_COUNT);
        if (reBindCount >= count) {
            return;
        }
        ScheduleTaskService.getInstance()
                .getScheduleTaskManager()
                .startSchedule(reBindCallback, getReconnectInterval());
    }

    /**
     * 重新绑定打印机Callback
     */
    ScheduleTask.Callback reBindCallback = new ScheduleTask.Callback() {
        @Override
        public long doSchedule() {
            reBindCount++;
            LogUtil.d(TAG, "重新绑定打印机次数 " + reBindCount);
            startBindClient();
            int count = SPUtil.getInstance(mContext).getInt(Constant.SPKey.KEY_RECONNECT_COUNT,
                    Constant.DEFAULT_RECONNECT_COUNT);
            if (reBindCount >= count) {
                return 0;
            }
            return getReconnectInterval();
        }
    };

    /**
     * 获取重连间隔
     *
     * @return 重连间隔
     */
    public long getReconnectInterval() {
        return SPUtil.getInstance(mContext).getLong(Constant.SPKey.KEY_MSG_RESENT_INTERVAL,
                Constant.DEFAULT_RECONNECT_INTERVAL);
    }

    /**
     * 发送请求
     *
     * @param tcpMessage     tcpMessage
     * @param isNeedResponse 是否需要响应
     *                       //     * @param isNeedResend   是否需要重发
     * @return Http
     */
    public Http sendRequest(TcpMessage tcpMessage, boolean isNeedResponse/*,
                            boolean isNeedResend*/) {
        if (!isConnected()) {
            return null;
        }
        Http http;
        try {
            RequestParam param = new RequestParam();
            param.setTimeout(SPUtil.getInstance(mContext).getInt(Constant.SPKey.KEY_MSG_TIMEOUT, Constant.DEFAULT_TIMEOUT));
            param.setTimeHandler(timeoutHandler);
            SocketRequest socketRequest = new SocketRequest(param, tcpMessage);
            socketRequest.setNeedRsp(isNeedResponse);
//            socketRequest.setNeedResend(isNeedResend);
            socketRequest.setNeedResend(isNeedResponse);
            http = Http.sendRequest(socketRequest);
        } catch (Exception e) {
            e.printStackTrace();
            http = null;
        }

        return http;
    }


    /**
     * 发送请求
     *
     * @param request        request
     * @param isNeedResponse 是否需要响应
     *                       //     * @param isNeedResend   是否需要重发
     * @return Http
     */
    public Http sendRequest(BaseRequest request, boolean isNeedResponse/*,
                            boolean isNeedResend*/) {
        if (!isConnected()) {
            return null;
        }

        TcpMessage tcpMessage = new TcpMessage();
        tcpMessage.setRequest(request);

        return sendRequest(tcpMessage, isNeedResponse);
    }

    /**
     * 方法描述：处理响应消息 输入参数：@param socketMessage
     * 返回类型：void： 备注：
     */
    private void processRsp(TcpMessage tcpMessage) {
        LogUtil.d(TAG, "processRsp enter");
//        SocketRequest request = getSocketRequest(tcpMessage.getAnswerSequenceId());

        SocketRequest request = getSocketRequest(tcpMessage.getSequenceId());

        if (null == request) {
            LogUtil.d(TAG, "processRsp request == null");
        } else {
            request.getHttp().cancel();
        }
        parseMessage(tcpMessage.getBody());

    }

    /**
     * 方法描述：获取包含指定消息的请求 输入参数：@param
     * socketMessage 输入参数：@return 返回类型：SocketRequest： 备注：
     */
    public SocketRequest getSocketRequest(String sequenceId) {
        if (isEmptyReqQueue() || TextUtils.isEmpty(sequenceId)) {
            return null;
        }
        return reqQueue.get(sequenceId);
    }


    /**
     * 方法名称：startHeartBeat 方法描述：启动心跳 输入参数： 返回类型：void： 备注：
     */
    public void startHeartBeat() {
        // 启动心跳
        ScheduleTaskService.getInstance()
                .getScheduleTaskManager()
                .startSchedule(this, getHeartBeatInterval());
    }

    /**
     * 心跳时间间隔
     *
     * @return 时间间隔
     */
    private long getHeartBeatInterval() {
        return SPUtil.getInstance(mContext).getLong(Constant.SPKey.KEY_HEARTBEAT_INTERVAL,
                Constant.DEFAULT_HEARTBEAT_INTERVAL);
    }

    /**
     * 停止心跳数据
     */
    public void stopHeartBeat() {
        ScheduleTaskService.getInstance()
                .getScheduleTaskManager()
                .stopSchedule(this);
        LogUtil.d(TAG, "Stop HeartBeat...leave");
    }

    /**
     * 设置心跳时间间隔
     */
    public void setHeartbeatInterval(long value) {
        SPUtil.getInstance(mContext).save(Constant.SPKey.KEY_HEARTBEAT_INTERVAL, value);
    }


    /**
     * 方法描述：发送心跳请求 输入参数： 返回类型：void： 备注：
     */
    public void sendHeartBeatRequest() {
        LogUtil.d(TAG, "------->sendHeartBeatRequest");
        try {
            // 构造心跳请求
            HeartbeatRequest heartbeatRequest = new HeartbeatRequest();
            heartbeatRequest.setSign(heartbeatRequest.getSign());
            sendRequest(heartbeatRequest, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 取消所以请求
     */
    public void cancelAllRequest() {
        if (this.isEmptyReqQueue()) {
            LogUtil.d(TAG, "cancelAllRequest return");
            return;
        }
        Enumeration<SocketRequest> elements = reqQueue.elements();
        for (Enumeration<SocketRequest> element = elements; elements.hasMoreElements(); ) {
            SocketRequest request = element.nextElement();

            if (null != request) {
                Http http = request.getHttp();

                if (http != null) {
                    http.cancel();
                }
            }
        }
        delAllSocketRequest();
    }

    /**
     * 方法描述：删除所有请求 输入参数: 返回类型：void 备注：
     */
    private void delAllSocketRequest() {
        if (isEmptyReqQueue()) {
            return;
        }

        reqQueue.clear();
    }

    private boolean isEmptyReqQueue() {
        return null == reqQueue || reqQueue.isEmpty();
    }

    /**
     * 方法名称：stopService 方法描述：停止服务 输入参数： 返回类型：void： 备注：
     */
    public void stopService() {
        clearAll();
//        VtdService.getService().stopService();
        connection.removeImpsConnection(this);
    }

    /**
     * 方法描述：清除内存数据 输入参数: 返回类型：void 备注：
     */
    private void clearAll() {
        // 清除缓存数据

        cancelAllRequest();
        ScheduleTaskService.getInstance().shutdown();
//        stopHeartBeat();

        if (null != mNetworkConnectivityListener) {
            LogUtil.d(TAG, "unregisterHandler");
            mNetworkConnectivityListener.unregisterHandler(mServiceHandler);
            mNetworkConnectivityListener.stopListening();
            mNetworkConnectivityListener = null;
        }
        // TODO
//        close();

    }

    @Override
    public long doSchedule() {

        if (!isConnected()) {
            return 0;
        }

        sendHeartBeatRequest();

        return getHeartBeatInterval();
    }

    /**
     * 方法描述：从请求队列中删除一个请求 输入参数:@param
     * socketRequest 返回类型：void 备注：
     */
    public void delSocketRequest(SocketRequest request) {
        if (null == request) {
            return;
        }
        delSocketRequest(request.getSequenceNumber());
    }

    /**
     * 方法描述：删除一个流水号的请求 输入参数：@param
     * sequenceNumber 返回类型：void： 备注：
     */
    private void delSocketRequest(String sequenceNumber) {
        if (isEmptyReqQueue() || TextUtils.isEmpty(sequenceNumber)) {
            return;
        }

        LogUtil.d(TAG, "delete form requestQueue where sequenceNumber(pkg_id) = " + sequenceNumber);
        reqQueue.remove(sequenceNumber);
    }


    /**
     * 方法描述：发送消息 输入参数：@param socketRequest 返回类型：void： 备注：
     */
    public boolean send(SocketRequest request) {

        if (!isConnected()) {
            return false;
        }
        boolean result = false;
        try {

            // TODO
            result = sendMessage(request);
            if (request.isNeedRsp()) {
                addSocketRequest(request);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 方法描述：加入请求到队列中 输入参数：@param socketRequest
     * 返回类型：void： 备注：
     */
    private void addSocketRequest(SocketRequest request) {
        if (null == request || null == reqQueue) {
            return;
        }
        reqQueue.put(request.getSequenceNumber(), request);
    }

    /**
     * 方法描述：发送消息 输入参数：@param message 输入参数：@throws
     * IOException 返回类型：void： 备注：
     */
    private boolean sendMessage(SocketRequest request) throws IOException {
        if (null == request) {
            throw new IOException();
        }
        // TODO
        return sendMessage(TcpMessageParser.socketRequest2Message(request));
    }

    /**
     * 发送消息
     *
     * @param message message
     */
    private boolean sendMessage(String message) {
        return connection.sendMessage(message);
    }

    /**
     * 消息解析
     *
     * @param message message
     */
    private void parseMessage(String message) {
        try {
            JSONObject object = new JSONObject(message);
            if (!object.has("pkg_type")) {
                return;
            }
            String pkg_type = object.getString("pkg_type");

            switch (pkg_type) {

                // 绑定不成功
                case MessageId.PKG_DEVICE_BIND:
                    startReBindClient();
                    break;

                // 绑定成功
                case MessageId.PKG_DEVICE_BIND_ACK:
                    if (object.has("respcd")) {
                        stopReBindClient();
                        startHeartBeat();
                    }
                    break;
                // 收到推送
                case MessageId.PKG_DEVICE_PUSH:
                    if (object.has("msg_id")) {
                        PushResponse response = new PushResponse();
                        response.setMsgid(object.getString("msg_id"));
                        response.setSign(response.getSign());
                        sendMessage(gson.toJson(response));
                    }
                    break;
                default:
                    break;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
